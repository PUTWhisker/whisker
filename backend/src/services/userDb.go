package services

import (
	"context"
	"database/sql"
	"log"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type UserDbModel interface {
	getUserPassword(email string) (string, error)
	isUserInDatabase(email string) bool
	addUserToDatabase(email string, password string)
	getUserTranscriptionHistory(email string) (pgx.Rows, error)
}

type UserDb struct {
	Pool *pgxpool.Pool
}

func (db *UserDb) getUserPassword(email string) (string, error) {
	row := db.Pool.QueryRow(context.Background(), "SELECT password_hash FROM app_user WHERE email=$1;", email)
	var password string
	err := row.Scan(&password)
	return password, err
}

func (db *UserDb) isUserInDatabase(email string) bool {
	row := db.Pool.QueryRow(context.Background(), "SELECT email FROM app_user WHERE email=$1;", email)
	err := row.Scan(&email)
	return !(err == sql.ErrNoRows)
}

func (db *UserDb) addUserToDatabase(email string, password string) {
	password_hash, err := HashPassword(password)
	if err != nil {
		log.Fatalf("failed to hash password")
		return
	}
	_, err = db.Pool.Exec(context.Background(), "INSERT INTO app_user(email, password_hash) VALUES ($1, $2);", email, password_hash)
	if err != nil {
		log.Fatal(err)
	}
}

func (db *UserDb) getUserTranscriptionHistory(email string) (pgx.Rows, error) {
	rows, err := db.Pool.Query(context.Background(), "SELECT content FROM transcription WHERE app_user_id=(select id from app_user where email=$1 LIMIT 1);", email)
	if err != nil {
		log.Fatal(err)
		return nil, err
	}
	return rows, nil
}
