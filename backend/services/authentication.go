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
	"github.com/jackc/pgx/v5/pgxpool"
	"golang.org/x/crypto/bcrypt"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/metadata"
	"google.golang.org/grpc/status"
)

type AuthenticationServer struct {
	DbPool *pgxpool.Pool
	pb.UnimplementedClientServiceServer
}

func hashPassword(password string) (string, error) {
	bytes, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	return string(bytes), err
}

func comparePasswords(hashedPassword string, plainPassword []byte) bool {
	byteHash := []byte(hashedPassword)
	err := bcrypt.CompareHashAndPassword(byteHash, plainPassword)
	if err != nil {
		log.Println(err)
		return false
	}

	return true
}

func AddUserToDatabase(email string, password string, pool *pgxpool.Pool) {
	password_hash, err := hashPassword(password)
	if err != nil {
		log.Fatalf("failed to hash password")
		return
	}
	_, err = pool.Exec(context.Background(), "INSERT INTO app_user(email, password_hash) VALUES ($1, $2);", email, password_hash)
	if err != nil {
		log.Fatal(err)
	}
}

func IsUserInDatabase(email string, pool *pgxpool.Pool) bool {
	rows, err := pool.Query(context.Background(), "SELECT email FROM app_user WHERE email=$1;", email)
	if err != nil {
		log.Fatal(err)
		return false
	}
	return rows.Next()
}

func checkLoginPassword(email string, password string, pool *pgxpool.Pool) bool {
	rows, err := pool.Query(context.Background(), "SELECT password_hash FROM app_user WHERE email=$1;", email)
	if err != nil {
		log.Fatal(err)
		return false
	}
	if !rows.Next() {
		return false
	}
	var password_hash string
	err = rows.Scan(&password_hash)
	if err != nil {
		return false
	}
	return comparePasswords(password_hash, []byte(password))
}

func loadPrivateECDSAKeyFromFile(filepath string) (*ecdsa.PrivateKey, error) {
	keyData, err := os.ReadFile(filepath)
	if err != nil {
		return nil, err
	}

	block, _ := pem.Decode(keyData)
	if block == nil || block.Type != "EC PRIVATE KEY" {
		return nil, fmt.Errorf("failed to decode PEM block containing ECDSA private key")
	}

	privateKey, err := x509.ParseECPrivateKey(block.Bytes)
	if err != nil {
		return nil, err
	}

	return privateKey, nil
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

type UserClaims struct {
	Username string `json:"username"`
	jwt.RegisteredClaims
}

func generateJWT(privateKeyPath string, username string) (string, error) {
	var (
		t *jwt.Token
	)
	key, _ := loadPrivateECDSAKeyFromFile(privateKeyPath)
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

func verifyJWT(tokenString string) (*jwt.Token, error) {
	publicKey, err := loadPublicECDSAKeyFromFile(os.Getenv("JWT_PUBLIC_KEY_PATH"))

	if err != nil {
		fmt.Println(err)
		return nil, err
	}

	token, err := jwt.ParseWithClaims(tokenString, &UserClaims{}, func(token *jwt.Token) (interface{}, error) {
		return publicKey, nil
	})
	if err != nil {
		log.Fatal(err)
	} else if _, ok := token.Claims.(*UserClaims); ok {
	} else {
		log.Fatal("unknown claims type, cannot proceed")
	}

	// Check if the token is valid
	if !token.Valid {
		return nil, fmt.Errorf("invalid token")
	}

	// Return the verified token
	return token, nil
}

func (s *AuthenticationServer) Login(ctx context.Context, in *pb.UserCredits) (*pb.LoginResponse, error) {
	if checkLoginPassword(in.Username, in.Password, s.DbPool) {
		token, _ := generateJWT(os.Getenv("JWT_PRIVATE_KEY_PATH"), in.Username)
		return &pb.LoginResponse{Successful: true, JWT: token}, nil

	}
	return &pb.LoginResponse{Successful: false, JWT: ""}, nil
}

func (s *AuthenticationServer) Register(ctx context.Context, in *pb.UserCredits) (*pb.StatusResponse, error) {
	if IsUserInDatabase(in.Username, s.DbPool) {
		return &pb.StatusResponse{Successful: false}, nil
	}
	AddUserToDatabase(in.Username, in.Password, s.DbPool)
	return &pb.StatusResponse{Successful: true}, nil
}

func GetTranslationFromDB(username string, pool *pgxpool.Pool) bool {
	_, err := pool.Query(context.Background(), "SELECT * FROM transcription WHERE app_user_id=(select id from app_user where email='nowy@email.com' LIMIT 1);", username)
	if err != nil {
		log.Fatal(err)
		return false
	}
	return true

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
	unverifiedToken := md["jwt"][0]
	token, err := verifyJWT(unverifiedToken)

	if err != nil {
		return err
	}
	claims := token.Claims.(*UserClaims)

	header := metadata.New(map[string]string{"location": "MTV", "timestamp": time.Now().Format(timestampFormat)})
	stream.SendHeader(header)

	rows, err := s.DbPool.Query(context.Background(), "SELECT content FROM transcription WHERE app_user_id=(select id from app_user where email=$1 LIMIT 1);", claims.Username)
	if err != nil {
		log.Fatal(err)
		return err
	}
	defer rows.Close()
	for rows.Next() {
		var transcirptionText string
		err = rows.Scan(&transcirptionText)
		if err != nil {
			log.Fatal(err)
		}

		err := stream.Send(&pb.TextHistory{Transcription: transcirptionText})
		if err != nil {
			return err
		}
	}
	return nil
}
