package services

import (
	"context"
	"fmt"
	pb "inzynierka/server/proto/sound_transfer"
	"io"
	"log"
	"os"
	"os/exec"
	"strings"

	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgxpool"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/metadata"
	"google.golang.org/grpc/status"
)

type SoundServer struct {
	SoundFileStoragePath string
	DbPool               *pgxpool.Pool
	pb.UnimplementedSoundServiceServer
}

func (s *SoundServer) TestConnection(ctx context.Context, in *pb.TextMessage) (*pb.TextMessage, error) {
	log.Printf("Received: %v", in.GetText())
	return &pb.TextMessage{Text: in.GetText()}, nil
}

func SaveTextToHistory(text string, username string, pool *pgxpool.Pool) {
	fmt.Println("Here")
	_, err := pool.Exec(context.Background(), `
    INSERT INTO transcription(app_user_id, content) 
    VALUES ((SELECT id FROM app_user WHERE email = $1), $2);
	`, username, text)
	if err != nil {
		log.Fatal(err)
	}
}

func (s *SoundServer) SendSoundFile(ctx context.Context, in *pb.SoundRequest) (*pb.SoundResponse, error) {
	bytes := in.GetSoundData()
	log.Printf("Received: sound file")
	soundFilePath := s.SoundFileStoragePath + "/" + uuid.New().String() + ".mp3"
	os.WriteFile(soundFilePath, bytes, 0666)
	response := transcribe(soundFilePath)
	fmt.Println(soundFilePath)
	os.Remove(soundFilePath)

	md, ok := metadata.FromIncomingContext(ctx)
	if !ok {
		return nil, status.Errorf(codes.DataLoss, "Failed to get metadata")
	}
	username, err := GetUserNameFromMetadata(md)
	if err != nil {
		return nil, err
	}

	if username != "" {
		SaveTextToHistory(response, username, s.DbPool)
	}

	return &pb.SoundResponse{Text: response}, nil
}

func transcribe(soundFilePath string) string {
	modelPath := os.Getenv("MODEL_PATH")
	soundFiles := os.Getenv("SOUND_FILES_FOLDER_PATH")
	cmd := exec.Command(modelPath, soundFilePath, "-f", "txt", "--language", "Polish", "--output_dir", soundFiles)
	var out strings.Builder
	cmd.Stdout = &out
	err := cmd.Run()
	if err != nil {
		log.Fatal(err)
	}
	textOutputPath := soundFilePath[:len(soundFilePath)-4] + ".txt"
	bytes, _ := os.ReadFile(textOutputPath)
	os.Remove(textOutputPath)
	return string(bytes)
}

func (s *SoundServer) StreamSoundFile(stream pb.SoundService_StreamSoundFileServer) error {
	for {
		in, err := stream.Recv()
		if err == io.EOF {
			return nil
		}
		if err != nil {
			return err
		}
		fmt.Println(in)
	}
}
