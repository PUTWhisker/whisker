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

	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

type SoundServer struct {
	SoundFileStoragePath string
	pb.UnimplementedSoundServiceServer
}

var whisperPort string = "127.0.0.1:7070"
var WhisperServer pb.SoundServiceClient

func ConnectToWhisperServer() error {
	conn, err := grpc.NewClient(whisperPort, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		log.Fatalf("%v", err)
		return err
	}
	WhisperServer = pb.NewSoundServiceClient(conn)
	res, err := WhisperServer.TestConnection(context.TODO(), &pb.TextMessage{
		Text: "Hello server",
	})
	if err != nil {
		log.Fatalf("%v", err)
		return err
	}
	fmt.Println(res)
	return nil
}

func (s *SoundServer) TestConnection(ctx context.Context, in *pb.TextMessage) (*pb.TextMessage, error) {
	log.Printf("Received: %v", in.GetText())
	return &pb.TextMessage{Text: in.GetText()}, nil
}

func (s *SoundServer) SendSoundFile(ctx context.Context, in *pb.SoundRequest) (*pb.SoundResponse, error) {
	log.Printf("Received: sound file")
	res, err := WhisperServer.SendSoundFile(context.TODO(), in)
	if err != nil {
		return res, err
	}
	return res, nil
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
	whisperStream, _ := WhisperServer.StreamSoundFile(context.TODO())
	errChannel := make(chan error)

	go func(whisperStream pb.SoundService_StreamSoundFileClient, errChannel chan error) {
		for {
			whisperTranscription, err := whisperStream.Recv()
			if err == io.EOF {
				close(errChannel)
				return // End of streaming requests
			}
			if err != nil {
				errChannel <- err
				panic("Whisper server error")
			}
			stream.Send(whisperTranscription)
			//fmt.Println("goroutine send message to channel")
		}
	}(whisperStream, errChannel)

	for {
		in, err := stream.Recv()
		if err == io.EOF { // End of streaming requests
			whisperStream.CloseSend()
			res, ok := <-errChannel
			if ok {
				return res
			}
			return nil
		}
		if err != nil {
			return err
		}
		if err := whisperStream.Send(in); err != nil {
			return err
		}
		//fmt.Println("Main function sent message to whisper")
		select {
		case err = <-errChannel:
			return (err)
		default:
		}
	}
}
