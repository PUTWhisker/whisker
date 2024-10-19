package main

import (
	"context"
	"flag"
	"fmt"
	AuthenticationProto "inzynierka/server/proto/authentication"
	SoundTransferProto "inzynierka/server/proto/sound_transfer"
	"log"
	"net"
	"os"
	"strconv"

	"inzynierka/server/services"

	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/joho/godotenv"
	"google.golang.org/grpc"
	"google.golang.org/grpc/reflection"
)

func createTCPListener() net.Listener {
	portNumber, err := strconv.Atoi(os.Getenv("SERVER_PORT"))
	if err != nil {
		log.Fatalf("Invalid port number: %v", err)
	}
	port := flag.Int("port", portNumber, "The server port")
	lis, err := net.Listen("tcp", fmt.Sprintf(":%d", *port))
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}
	return lis
}

func registerGrpcServices(server *grpc.Server) *pgxpool.Pool {
	if os.Getenv("USE_DATABASE") == "True" {
		pool, err := pgxpool.New(context.Background(), os.Getenv("DATABASE_URL"))
		if err != nil {
			log.Fatalf("Unable to initialize pool: %v\n", err)
		}
		err = pool.Ping(context.Background())
		if err != nil {
			log.Fatalf("Unable to connect to database: %v\n", err)
		}
		AuthenticationProto.RegisterClientServiceServer(
			server,
			&services.AuthenticationServer{
				Db:           &services.UserDb{Pool: pool},
				JwtGenerator: &services.JWTGenerator{PrivateKeyPath: os.Getenv("JWT_PRIVATE_KEY_PATH")},
			},
		)
		SoundTransferProto.RegisterSoundServiceServer(server, &services.SoundServer{SoundFileStoragePath: os.Getenv("SOUND_FILES_FOLDER_PATH"), DbPool: pool})
		return pool
	} else {
		SoundTransferProto.RegisterSoundServiceServer(server, &services.SoundServer{SoundFileStoragePath: os.Getenv("SOUND_FILES_FOLDER_PATH"), DbPool: nil})
	}
	return nil
}

func main() {
	godotenv.Load()
	lis := createTCPListener()
	grpcServer := grpc.NewServer()
	dbPool := registerGrpcServices(grpcServer)
	if dbPool != nil {
		defer dbPool.Close()
	}
	reflection.Register(grpcServer)
	log.Printf("server listening at %v", lis.Addr())
	if err := grpcServer.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}
