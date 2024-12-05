package services

import (
	"context"
	"database/sql"
	pb "inzynierka/server/proto/authentication"
	"testing"

	"github.com/jackc/pgx/v5"
)

type LoginInput struct {
	ctx         context.Context
	userCredits *pb.UserCredits
}
type LoginOutput struct {
	response *pb.LoginResponse
	err      error
}

type MockDb struct {
	UserPassword string
}

type MockJWTGenerator struct {
}

func (g *MockJWTGenerator) generate(email string) (string, error) {
	return "something", nil
}

func (db *MockDb) getUserInfo(email string) (string, string, error) {
	if db.UserPassword != "" {
		hashedPassword, err := HashPassword(db.UserPassword)
		return "", hashedPassword, err
	}
	return "", "", sql.ErrNoRows
}

func (db *MockDb) isUserInDatabase(email string) (bool, error) {
	return true, nil
}

func (db *MockDb) addUserToDatabase(email string, password string) error {
	return nil
}

func (db *MockDb) getUserTranscriptionHistory(email string) (pgx.Rows, error) {
	return nil, nil
}

func (db *MockDb) saveTranscription(a string, b string, c bool, d string) error {
	return nil
}

func (db *MockDb) deleteTranscription(a int, b string) error {
	return nil
}

func (db *MockDb) editTranscription(a int, b string, c string) error {
	return nil
}

func (db *MockDb) saveDiarization(text []string, speaker []string, username string, language string) error {
	return nil
}
func (db *MockDb) saveTranslation(text string, username string, is_translation bool, language string, translated_text string, translation_language string) error {
	return nil
}

func compareLoginResponse(a *pb.LoginResponse, b *pb.LoginResponse) bool {
	return a.Successful == b.Successful && a.JWT == b.JWT
}

func TestLoging(t *testing.T) {
	// var tests = []struct {
	// 	name     string
	// 	input    LoginInput
	// 	dbOutput string
	// 	want     LoginOutput
	// }{
	// 	{
	// 		"Succesfull login",
	// 		LoginInput{context.TODO(), &pb.UserCredits{Username: "Krzysztof", Password: "Krzysztof"}},
	// 		"Krzysztof",
	// 		LoginOutput{&pb.LoginResponse{Successful: true, JWT: "something"}, nil},
	// 	},
	// 	{
	// 		"Password mismach",
	// 		LoginInput{context.TODO(), &pb.UserCredits{Username: "Krzysztof", Password: "Krzysztof"}},
	// 		"wrong password",
	// 		LoginOutput{&pb.LoginResponse{Successful: false, JWT: ""}, nil},
	// 	},
	// 	{
	// 		"Password not in database",
	// 		LoginInput{context.TODO(), &pb.UserCredits{Username: "Krzysztof", Password: "Krzysztof"}},
	// 		"",
	// 		LoginOutput{&pb.LoginResponse{Successful: false, JWT: ""}, nil},
	// 	},
	// }

	// for _, tt := range tests {
	// 	t.Run(tt.name, func(t *testing.T) {
	// 		server := AuthenticationServer{Db: &MockDb{UserPassword: tt.dbOutput}, JwtGenerator: &MockJWTGenerator{}}
	// 		ans, err := server.Login(tt.input.ctx, tt.input.userCredits)
	// 		if !compareLoginResponse(ans, tt.want.response) {
	// 			t.Errorf("LoginResponse mismach")
	// 		}
	// 		if err != tt.want.err {
	// 			t.Errorf("Error mismach")
	// 		}
	// 	})
	// }
}
