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

func main() {
	godotenv.Load()
	portNumber, err := strconv.Atoi(os.Getenv("SERVER_PORT"))
	if err != nil {
		log.Fatalf("Invalid port number: %v", err)
	}
	port := flag.Int("port", portNumber, "The server port")
	lis, err := net.Listen("tcp", fmt.Sprintf(":%d", *port))
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}

	services.ConnectToWhisperServer()

	s := grpc.NewServer()
	SoundTransferProto.RegisterSoundServiceServer(s, &services.SoundServer{SoundFileStoragePath: os.Getenv("SOUND_FILES_FOLDER_PATH")})
	if os.Getenv("USE_DATABASE") == "True" {
		pool, err := pgxpool.New(context.Background(), os.Getenv("DATABASE_URL"))
		if err != nil {
			fmt.Fprintf(os.Stderr, "Unable to connect to database: %v\n", err)
			os.Exit(1)
		}
		AuthenticationProto.RegisterClientServiceServer(s, &services.AuthenticationServer{DbPool: pool})
		log.Println("Database connected")
		defer pool.Close()
	}

	reflection.Register(s)
	log.Printf("server listening at %v", lis.Addr())
	if err := s.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}

}
