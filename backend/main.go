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

	"inzynierka/server/services"

	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/joho/godotenv"
	"google.golang.org/grpc"
)

var (
	port = flag.Int("port", 50051, "The server port")
)

func main() {
	godotenv.Load()
	flag.Parse()
	pool, err := pgxpool.New(context.Background(), os.Getenv("DATABASE_URL"))
	if err != nil {
		fmt.Fprintf(os.Stderr, "Unable to connect to database: %v\n", err)
		os.Exit(1)
	}
	defer pool.Close()

	lis, err := net.Listen("tcp", fmt.Sprintf(":%d", *port))
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}
	s := grpc.NewServer()
	SoundTransferProto.RegisterSoundServiceServer(s, &services.SoundServer{SoundFileStoragePath: os.Getenv("SOUND_FILES_FOLDER_PATH")})

	AuthenticationProto.RegisterClientServiceServer(s, &services.AuthenticationServer{DbPool: pool})
	log.Printf("server listening at %v", lis.Addr())
	if err := s.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}
