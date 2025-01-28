package services

import (
	"context"
	"crypto/ecdsa"
	"crypto/rand"
	"crypto/x509"
	"encoding/base64"
	"encoding/pem"
	"fmt"
	pb "inzynierka/server/proto/authentication" // TODO bruh tu jest ścieżka lokalna, do poprawki przed szóstym?
	"log"
	"os"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"golang.org/x/crypto/bcrypt"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/metadata"
	"google.golang.org/grpc/status"
	"google.golang.org/protobuf/types/known/emptypb"
	"google.golang.org/protobuf/types/known/timestamppb"
)

type AuthenticationServer struct {
	Db           UserDbModel
	JwtGenerator TokenGenerator
	pb.UnimplementedClientServiceServer
}

type UserClaims struct {
	User_id string `json:"user_id"`
	jwt.RegisteredClaims
}

type TokenGenerator interface {
	generate(string) (string, error)
}

type JWTGenerator struct {
	PrivateKeyPath string
}

var (
	timestampFormat     = time.StampNano
	errUserRegistered   = status.Errorf(codes.AlreadyExists, "User already exist")
	errWrongCredentials = status.Errorf(codes.PermissionDenied, "Password or login incorrect")
)

func (g *JWTGenerator) generate(database_id string) (string, error) {
	var t *jwt.Token

	key, err := loadPrivateECDSAKeyFromFile(g.PrivateKeyPath)
	if err != nil {
		log.Panicf("Failed to load private JWT key %v", err)
	}

	claims := UserClaims{
		database_id,
		jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(time.Now().Add(time.Minute)),
			Issuer:    "krzysztof",
		},
	}
	t = jwt.NewWithClaims(jwt.SigningMethodES256, claims)
	return t.SignedString(key)
}

func loadPublicECDSAKeyFromFile(filepath string) (*ecdsa.PublicKey, error) {
	keyData, err := os.ReadFile(filepath)
	if err != nil {
		return nil, err
	}

	block, _ := pem.Decode(keyData)

	if block == nil || block.Type != "PUBLIC KEY" {
		return nil, fmt.Errorf("failed to decode PEM block containing ECDSA public key")
	}

	publicKey, err := x509.ParsePKIXPublicKey(block.Bytes)
	if err != nil {
		return nil, err
	}

	ecdsaPubKey, ok := publicKey.(*ecdsa.PublicKey)
	if !ok {
		return nil, fmt.Errorf("not an ECDSA public key")
	}

	return ecdsaPubKey, nil
}

func loadPrivateECDSAKeyFromFile(filepath string) (*ecdsa.PrivateKey, error) {
	keyData, err := os.ReadFile(filepath)
	if err != nil {
		return nil, fmt.Errorf("failed to open key file")
	}

	block, _ := pem.Decode(keyData)
	if block == nil || block.Type != "EC PRIVATE KEY" {
		return nil, fmt.Errorf("failed to decode PEM block containing ECDSA private key")
	}

	privateKey, err := x509.ParseECPrivateKey(block.Bytes)
	if err != nil {
		return nil, fmt.Errorf("failed to parse key")
	}

	return privateKey, nil
}

func comparePasswords(hashedPassword string, plainPassword string) bool {
	err := bcrypt.CompareHashAndPassword([]byte(hashedPassword), []byte(plainPassword))
	return err == nil
}

func (s *AuthenticationServer) checkUserCredentials(username string, password string) (string, bool) {
	database_id, password_hash, err := s.Db.getUserInfo(username)
	if err != nil {
		return "", false
	}
	return database_id, comparePasswords(password_hash, password)
}

func (s *AuthenticationServer) Login(ctx context.Context, in *pb.UserCredits) (*pb.LoginResponse, error) {
	userId, isLoginSuccesfull := s.checkUserCredentials(in.Username, in.Password)
	if isLoginSuccesfull {
		token, _ := s.JwtGenerator.generate(userId)
		newRefreshToken, _ := generateOpaqueToken()
		s.Db.addRefreshToken(ctx, userId, newRefreshToken)
		return &pb.LoginResponse{JWT: token, RefreshToken: newRefreshToken}, nil

	}
	return nil, errWrongCredentials
}

func HashPassword(password string) (string, error) {
	bytes, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	return string(bytes), err
}

func (s *AuthenticationServer) Register(ctx context.Context, in *pb.UserCredits) (*emptypb.Empty, error) {

	is_in_database, err := s.Db.isUserInDatabase(in.Username)
	if err != nil {
		return &emptypb.Empty{}, err
	}
	if is_in_database {
		return &emptypb.Empty{}, errUserRegistered
	}
	err = s.Db.addUserToDatabase(in.Username, in.Password)
	if err != nil {
		return &emptypb.Empty{}, err
	}
	return &emptypb.Empty{}, nil
}

func sendHeader(stream grpc.ServerStream) {
	header := metadata.New(map[string]string{"location": "MTV", "timestamp": time.Now().Format(timestampFormat)})
	stream.SendHeader(header)
}

func sendTrailer(stream grpc.ServerStream) {
	trailer := metadata.Pairs("timestamp", time.Now().Format(timestampFormat))
	stream.SetTrailer(trailer)
}

func (s *AuthenticationServer) GetTranscription(in *pb.QueryParamethers, stream pb.ClientService_GetTranscriptionServer) error {
	sendHeader(stream)
	defer sendTrailer(stream)
	rows, err := s.Db.getTranscriptions(stream.Context(), stream.Context().Value("user_id").(string), in)
	if err != nil {
		return err
	}
	defer rows.Close()
	for rows.Next() {
		var transcirption_text string
		var time_added time.Time
		var id int32
		var language string
		var title string
		err = rows.Scan(&id, &transcirption_text, &time_added, &language, &title)
		if err != nil {
			return err
		}

		err := stream.Send(&pb.TranscriptionHistory{Transcription: transcirption_text, CreatedAt: timestamppb.New(time_added), Id: id, Language: language, Title: title})
		if err != nil {
			return err
		}
	}
	return nil
}

func (s *AuthenticationServer) EditTranscription(ctx context.Context, in *pb.NewTranscription) (*emptypb.Empty, error) {
	err := s.Db.editTranscription(ctx, int(in.Id), ctx.Value("user_id").(string), in.Content)
	return &emptypb.Empty{}, err
}

func (s *AuthenticationServer) DeleteTranscription(ctx context.Context, in *pb.Id) (*emptypb.Empty, error) {
	err := s.Db.deleteTranscription(ctx, int(in.Id), ctx.Value("user_id").(string))
	return &emptypb.Empty{}, err
}

func (s *AuthenticationServer) GetTranslation(in *pb.QueryParamethers, stream pb.ClientService_GetTranslationServer) error {
	sendHeader(stream)
	defer sendTrailer(stream)
	rows, err := s.Db.getTranslations(stream.Context(), stream.Context().Value("user_id").(string), in)
	if err != nil {
		return err
	}
	defer rows.Close()
	for rows.Next() {
		var transcirption_text string
		var translation_text string
		var time_added time.Time
		var id int32
		var translation_language string
		var transcription_language string
		var title string
		err = rows.Scan(&id, &transcirption_text, &translation_text, &time_added, &transcription_language, &translation_language, &title)
		if err != nil {
			return err
		}

		err := stream.Send(&pb.TranslationHistory{Transcription: transcirption_text, Translation: translation_text, CreatedAt: timestamppb.New(time_added), Id: id, TranscriptionLangauge: transcription_language, TranslationLangauge: translation_language, Title: title})
		if err != nil {
			return err
		}
	}
	return nil
}

func (s *AuthenticationServer) EditTranslation(ctx context.Context, in *pb.NewTranslation) (*emptypb.Empty, error) {
	err := s.Db.editTranslation(
		in.EditTranscription,
		in.EditTranslation,
		int(in.Id),
		in.Transcription,
		in.Translation,
		ctx.Value("user_id").(string))
	return &emptypb.Empty{}, err
}

func (s *AuthenticationServer) DeleteTranslation(ctx context.Context, in *pb.Id) (*emptypb.Empty, error) {
	err := s.Db.deleteTranscription(ctx, int(in.Id), ctx.Value("user_id").(string))
	return &emptypb.Empty{}, err
}

func (s *AuthenticationServer) SaveOnlyTranslation(ctx context.Context, in *pb.TranslationText) (*emptypb.Empty, error) {
	return &emptypb.Empty{}, s.Db.insertOnlyTranslation(ctx, in)
}

func (s *AuthenticationServer) GetDiarization(in *pb.QueryParamethers, stream pb.ClientService_GetDiarizationServer) error {
	sendHeader(stream)
	defer sendTrailer(stream)
	rows, err := s.Db.getDiarizations(stream.Context(), stream.Context().Value("user_id").(string), in)
	if err != nil {
		return err
	}
	defer rows.Close()
	var oldId int32 = -1
	var oldCreatedAt time.Time
	var diarizationId int32
	var speaker string
	var line string
	var createdAt time.Time
	var language string
	var title string

	speakers := []string{}
	lines := []string{}

	if rows.Next() {
		err = rows.Scan(&diarizationId, &speaker, &line, &createdAt, &language, &title)
		if err != nil {
			return err
		}
		speakers = append(speakers, speaker)
		lines = append(lines, line)
		oldId = diarizationId
		oldCreatedAt = createdAt
	}

	for rows.Next() {
		err = rows.Scan(&diarizationId, &speaker, &line, &createdAt, &language, &title)
		if err != nil {
			return err
		}
		if oldId != diarizationId {
			err = stream.Send(&pb.DiarizationHistory{DiarizationId: oldId, Speaker: speakers, Line: lines, CreatedAt: timestamppb.New(oldCreatedAt), Language: language, Title: title})
			if err != nil {
				return err
			}
			speakers = []string{}
			lines = []string{}
			oldId = diarizationId
			oldCreatedAt = createdAt
		}
		speakers = append(speakers, speaker)
		lines = append(lines, line)
	}
	err = stream.Send(&pb.DiarizationHistory{DiarizationId: oldId, Speaker: speakers, Line: lines, CreatedAt: timestamppb.New(oldCreatedAt), Language: language})
	if err != nil {
		return err
	}
	return nil
}

func (s *AuthenticationServer) EditDiarization(ctx context.Context, in *pb.NewDiarization) (*emptypb.Empty, error) {
	err := s.Db.editDiarization(ctx, in.Line, in.Speaker, int(in.Id), ctx.Value("user_id").(string))
	return &emptypb.Empty{}, err
}

func (s *AuthenticationServer) DeleteDiarization(ctx context.Context, in *pb.Id) (*emptypb.Empty, error) {
	err := s.Db.deleteDiarization(ctx, int(in.Id), ctx.Value("user_id").(string))
	return &emptypb.Empty{}, err
}

func (s *AuthenticationServer) GetTranscriptionAndDiarization(in *pb.QueryParamethers, stream pb.ClientService_GetTranscriptionAndDiarizationServer) error {
	rows, err := s.Db.getDiarizationsAndTranscriptions(stream.Context(), stream.Context().Value("user_id").(string), in)
	if err != nil {
		return err
	}
	defer rows.Close()
	speakers := []string{}
	lines := []string{}
	var rowType string
	var id int
	var title string
	var lang string
	var createdAt time.Time
	var content string
	var speaker string
	counter := 0
	for rows.Next() {
		counter++
		err = rows.Scan(&rowType, &id, &title, &lang, &createdAt, &content, &speaker)
		if err != nil {
			return err
		}
		if rowType == "transcription" {
			if len(speakers) != 0 {
				stream.Send(&pb.Combined{
					Transcription: nil,
					Diarization: &pb.DiarizationHistory{
						DiarizationId: int32(id),
						Speaker:       speakers,
						Line:          lines,
						CreatedAt:     timestamppb.New(createdAt),
						Language:      lang,
						Title:         title,
					},
				})
				speakers = []string{}
				lines = []string{}
			}
			stream.Send(&pb.Combined{
				Diarization: nil,
				Transcription: &pb.TranscriptionHistory{
					Transcription: content,
					CreatedAt:     timestamppb.New(createdAt),
					Id:            int32(id),
					Language:      lang,
					Title:         title,
				},
			})
		} else {
			speakers = append(speakers, speaker)
			lines = append(lines, content)
		}
	}
	if len(speakers) != 0 {
		stream.Send(&pb.Combined{
			Transcription: nil,
			Diarization: &pb.DiarizationHistory{
				DiarizationId: int32(id),
				Speaker:       speakers,
				Line:          lines,
				CreatedAt:     timestamppb.New(createdAt),
				Language:      lang,
				Title:         title,
			},
		})
	}
	fmt.Println(counter)
	return nil
}
func generateOpaqueToken() (string, error) {
	bytes := make([]byte, 32) // 256 bits
	if _, err := rand.Read(bytes); err != nil {
		return "", err
	}
	return base64.URLEncoding.EncodeToString(bytes), nil
}

func (s *AuthenticationServer) RefreshToken(ctx context.Context, in *pb.RefreshTokenRequest) (*pb.RefreshTokenResponse, error) {
	userId, expiresAt, err := s.Db.getDataFromRefreshToken(ctx, in.RefreshToken)
	if err != nil {
		return nil, err
	}
	fmt.Println(expiresAt)
	if time.Now().After(expiresAt) {
		return nil, fmt.Errorf("refresh token expired")
	}
	var newRefreshToken string
	var newAccessToken string
	newRefreshToken, err = generateOpaqueToken()
	if err != nil {
		return nil, err
	}

	s.Db.addRefreshToken(ctx, userId, newRefreshToken)

	newAccessToken, err = s.JwtGenerator.generate(userId)
	if err != nil {
		return nil, err
	}
	return &pb.RefreshTokenResponse{RefreshToken: newRefreshToken, AccessToken: newAccessToken}, nil
}
