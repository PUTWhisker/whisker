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
	isUserInDatabase(email string) (bool, error)
	addUserToDatabase(email string, password string) error
	getUserTranscriptionHistory(email string) (pgx.Rows, error)
	saveTranscription(text string, username string)
}

type UserDb struct {
	pool *pgxpool.Pool
}

func NewUserDb(pool *pgxpool.Pool) *UserDb {
	p := &UserDb{}
	p.pool = pool
	return p
}

func (db UserDb) saveTranscription(text string, username string) {
	_, err := db.pool.Exec(context.Background(), `
    INSERT INTO transcription(app_user_id, content) 
    VALUES ((SELECT id FROM app_user WHERE email = $1), $2);
	`, username, text)
	if err != nil {
		log.Fatal(err)
	}
}

func (db UserDb) getUserPassword(email string) (string, error) {
	row := db.pool.QueryRow(context.Background(), "SELECT password_hash FROM app_user WHERE email=$1;", email)
	var password string
	err := row.Scan(&password)
	return password, err
}

func (db UserDb) isUserInDatabase(email string) (bool, error) {
	row := db.pool.QueryRow(context.Background(), "SELECT email FROM app_user WHERE email=$1;", email)
	err := row.Scan(&email)
	if err == sql.ErrNoRows {
		return false, nil
	}
	if err == nil {
		return true, nil
	}
	return false, err
}

func (db UserDb) addUserToDatabase(email string, password string) error {
	password_hash, err := HashPassword(password)
	if err != nil {
		log.Printf("Database error %v", err)
		return err
	}
	_, err = db.pool.Exec(context.Background(), "INSERT INTO app_user(email, password_hash) VALUES ($1, $2);", email, password_hash)
	if err != nil {
		log.Printf("Database error %v", err)
		return err
	}
	return nil
}

func (db UserDb) getUserTranscriptionHistory(email string) (pgx.Rows, error) {
	rows, err := db.pool.Query(context.Background(), "SELECT content, created_at FROM transcription WHERE app_user_id=(select id from app_user where email=$1 LIMIT 1);", email)
	if err != nil {
		log.Printf("Database error %v", err)
		return nil, err
	}
	return rows, nil
}
