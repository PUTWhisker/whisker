package services

import (
	"context"
	"fmt"
	pb "inzynierka/server/proto/sound_transfer"
	"io"
	"log"
	"os"

	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/credentials/insecure"
	"google.golang.org/grpc/metadata"
	"google.golang.org/grpc/status"
)

type SoundServer struct {
	SoundFileStoragePath string
	pb.UnimplementedSoundServiceServer
	Db UserDbModel
}

var whisperPort string = os.Getenv("WHISPER_SERVER") + ":7070"
var WhisperServer pb.SoundServiceClient

func ConnectToWhisperServer() error {
	conn, err := grpc.NewClient(whisperPort, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		return err
	}
	WhisperServer = pb.NewSoundServiceClient(conn)
	res, err := WhisperServer.TestConnection(context.TODO(), &pb.TextMessage{
		Text: "Hello server",
	})
	if err != nil {
		return err
	}
	fmt.Println(res)
	return nil
}

func (s *SoundServer) TestConnection(ctx context.Context, in *pb.TextMessage) (*pb.TextMessage, error) {
	log.Printf("Received new connection")
	return &pb.TextMessage{Text: in.GetText()}, nil
}

func (s *SoundServer) TranscribeFile(ctx context.Context, in *pb.TranscriptionRequest) (*pb.SoundResponse, error) {
	log.Printf("Received: sound file transcription")

	md, ok := metadata.FromIncomingContext(ctx)
	if !ok {
		return nil, status.Errorf(codes.DataLoss, "Failed to get metadata")
	}
	log.Print(md) // JWT token for saving
	newCtx := metadata.NewOutgoingContext(context.Background(), nil)
	res, err := WhisperServer.TranscribeFile(newCtx, in)
	if err != nil {
		return res, err
	}
	username, _ := GetUserNameFromMetadata(md)
	if username != "" && s.Db != nil {
		err = s.Db.saveTranscription(res.Text, username, false, in.SourceLanguage)
		if err != nil {
			fmt.Println(err)
		}
	}

	return res, nil
}

func (s *SoundServer) DiarizateFile(ctx context.Context, in *pb.TranscriptionRequest) (*pb.SpeakerAndLineResponse, error) {
	log.Printf("Received: sound file diarization")
	md, ok := metadata.FromIncomingContext(ctx)
	if !ok {
		return nil, status.Errorf(codes.DataLoss, "Failed to get metadata")
	}
	log.Print(md) // JWT token for saving
	newCtx := metadata.NewOutgoingContext(context.Background(), nil)
	res, err := WhisperServer.DiarizateFile(newCtx, in)
	if err != nil {
		return res, err
	}

	username, err := GetUserNameFromMetadata(md)
	if err != nil {
		return nil, err
	}

	if username != "" && s.Db != nil {
		s.Db.saveDiarization(res.Text, res.SpeakerName, username, in.SourceLanguage)
	}

	return res, nil
}

func (s *SoundServer) TranslateFile(in *pb.TranslationRequest, stream pb.SoundService_TranslateFileServer) error {
	log.Printf("Received: sound file translation")
	md, ok := metadata.FromIncomingContext(stream.Context())
	if !ok {
		return status.Errorf(codes.DataLoss, "Failed to get metadata")
	}
	log.Print(md) // JWT token for saving
	newCtx := metadata.NewOutgoingContext(context.Background(), nil)
	whisperStream, _ := WhisperServer.TranslateFile(newCtx, in)
	transcription, err := whisperStream.Recv()
	if err != nil {
		return err
	}
	stream.Send(transcription)
	translation, err := whisperStream.Recv()
	if err != nil {
		return err
	}
	stream.Send(translation)
	return nil
}

func (s *SoundServer) TranscribeLive(stream pb.SoundService_TranscribeLiveServer) error {
	md, ok := metadata.FromIncomingContext(stream.Context())
	if !ok {
		return status.Errorf(codes.DataLoss, "Failed to get metadata")
	}
	language := md["source_language"]

	whisperMd := metadata.New(map[string]string{})
	if language != nil {
		whisperMd = metadata.New(map[string]string{
			"source_language": language[0],
		})
	}

	newCtx := metadata.NewOutgoingContext(context.Background(), whisperMd)
	whisperStream, _ := WhisperServer.TranscribeLive(newCtx)
	errChannel := make(chan error)

	go func(whisperStream pb.SoundService_TranscribeLiveClient, errChannel chan error) {
		for {
			whisperTranscription, err := whisperStream.Recv()
			if err == io.EOF {
				close(errChannel)
				return // End of streaming requests
			}
			if err != nil {
				errChannel <- err
			}
			stream.Send(whisperTranscription)
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
			log.Println(err)
			return err
		}
		select {
		case err = <-errChannel:
			return (err)
		default:
		}
	}
}
