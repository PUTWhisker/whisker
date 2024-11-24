package services

import (
	"context"
	"crypto/ecdsa"
	"crypto/x509"
	"encoding/pem"
	"fmt"
	pb "inzynierka/server/proto/authentication"
	"log"
	"os"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"golang.org/x/crypto/bcrypt"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/metadata"
	"google.golang.org/grpc/status"
	"google.golang.org/protobuf/types/known/timestamppb"
)

type AuthenticationServer struct {
	Db           UserDbModel
	JwtGenerator TokenGenerator
	pb.UnimplementedClientServiceServer
}

type UserClaims struct {
	Username string `json:"username"`
	jwt.RegisteredClaims
}

type TokenGenerator interface {
	generate(string) (string, error)
}

type JWTGenerator struct {
	PrivateKeyPath string
}

func (g *JWTGenerator) generate(username string) (string, error) {
	var t *jwt.Token

	key, err := loadPrivateECDSAKeyFromFile(g.PrivateKeyPath)
	if err != nil {
		log.Panicf("Failed to load private JWT key %v", err)
	}

	claims := UserClaims{
		username,
		jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(time.Now().Add(time.Hour)),
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

func (s *AuthenticationServer) checkUserCredentials(email string, password string) bool {
	password_hash, err := s.Db.getUserPassword(email)
	if err != nil {
		return false
	}
	return comparePasswords(password_hash, password)
}

func (s *AuthenticationServer) Login(ctx context.Context, in *pb.UserCredits) (*pb.LoginResponse, error) {
	if s.checkUserCredentials(in.Username, in.Password) {
		token, _ := s.JwtGenerator.generate(in.Username)
		return &pb.LoginResponse{Successful: true, JWT: token}, nil

	}
	return &pb.LoginResponse{Successful: false, JWT: ""}, nil
}

func HashPassword(password string) (string, error) {
	bytes, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	return string(bytes), err
}

func (s *AuthenticationServer) Register(ctx context.Context, in *pb.UserCredits) (*pb.StatusResponse, error) {
	if successful, err := s.Db.isUserInDatabase(in.Username); successful {
		if err != nil {
			return &pb.StatusResponse{Successful: false, Error: "Unknown error"}, nil
		}
		return &pb.StatusResponse{Successful: false, Error: "User already registered"}, nil
	}
	err := s.Db.addUserToDatabase(in.Username, in.Password)
	if err != nil {
		return &pb.StatusResponse{Successful: false, Error: "Database error"}, nil
	}
	return &pb.StatusResponse{Successful: true}, nil
}

func verifyJWT(tokenString string) (*jwt.Token, error) {
	publicKey, err := loadPublicECDSAKeyFromFile(os.Getenv("JWT_PUBLIC_KEY_PATH"))

	if err != nil {
		log.Panicf("Failed to load public key %v", err)
	}

	token, err := jwt.ParseWithClaims(tokenString, &UserClaims{}, func(token *jwt.Token) (interface{}, error) {
		return publicKey, nil
	})
	if err != nil {
		return nil, err
	} else if _, ok := token.Claims.(*UserClaims); ok {
	} else {
		return nil, status.Error(1, "unknown claims type, cannot proceed")
	}

	// Check if the token is valid
	if !token.Valid {
		return nil, fmt.Errorf("invalid token")
	}

	// Return the verified token
	return token, nil
}

func GetUserNameFromMetadata(metadata metadata.MD) (string, error) {
	// no metadata attached
	if metadata["jwt"] == nil {
		return "", nil
	}
	unverifiedToken := metadata["jwt"][0]
	token, err := verifyJWT(unverifiedToken)
	if err != nil {
		return "", err
	}
	claims := token.Claims.(*UserClaims)
	return claims.Username, nil
}

func (s *AuthenticationServer) GetTranslation(_ *pb.Empty, stream pb.ClientService_GetTranslationServer) error {
	timestampFormat := time.StampNano
	defer func() {
		trailer := metadata.Pairs("timestamp", time.Now().Format(timestampFormat))
		stream.SetTrailer(trailer)
	}()

	md, ok := metadata.FromIncomingContext(stream.Context())
	if !ok {
		return status.Errorf(codes.DataLoss, "Failed to get metadata")
	}
	username, err := GetUserNameFromMetadata(md)

	if err != nil {
		return err
	}

	header := metadata.New(map[string]string{"location": "MTV", "timestamp": time.Now().Format(timestampFormat)})
	stream.SendHeader(header)

	rows, err := s.Db.getUserTranscriptionHistory(username)
	if err != nil {
		log.Fatal(err)
		return err
	}
	defer rows.Close()
	for rows.Next() {
		var transcirptionText string
		var time_added time.Time
		var id int32
		err = rows.Scan(&id, &transcirptionText, &time_added)
		if err != nil {
			log.Fatal(err)
		}

		err := stream.Send(&pb.TextHistory{Transcription: transcirptionText, CreatedAt: timestamppb.New(time_added), Id: id})
		if err != nil {
			return err
		}
	}
	return nil
}

func (s *AuthenticationServer) EditTranscription(ctx context.Context, in *pb.NewContent) (*pb.StatusResponse, error) {
	md, ok := metadata.FromIncomingContext(ctx)
	if !ok {
		return nil, status.Errorf(codes.DataLoss, "Failed to get metadata")
	}

	username, err := GetUserNameFromMetadata(md)

	err = s.Db.editTranscription(int(in.Id), username, in.Content)

	if err == nil {
		return &pb.StatusResponse{Successful: true, Error: ""}, err
	} else {
		log.Panic(err)
	}
	return &pb.StatusResponse{Successful: false, Error: "Error while editing"}, err
}

func (s *AuthenticationServer) DeleteTranscription(ctx context.Context, in *pb.Id) (*pb.StatusResponse, error) {
	md, ok := metadata.FromIncomingContext(ctx)
	if !ok {
		return nil, status.Errorf(codes.DataLoss, "Failed to get metadata")
	}

	username, err := GetUserNameFromMetadata(md)

	err = s.Db.deleteTranscription(int(in.Id), username)
	if err == nil {
		return &pb.StatusResponse{Successful: true, Error: ""}, err
	}
	return &pb.StatusResponse{Successful: false, Error: "Error while deleting"}, nil
}
