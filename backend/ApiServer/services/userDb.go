package services

import (
	"context"
	"database/sql"
	"log"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type UserDbModel interface {
	getUserPassword(username string) (string, error)
	isUserInDatabase(username string) (bool, error)
	addUserToDatabase(username string, password string) error
	getUserTranscriptionHistory(username string) (pgx.Rows, error)

	saveTranscription(text string, username string, is_translation bool, language string)
	editTranscription(id int, username string, new_content string) error
	deleteTranscription(id int, username string) error

	saveDiarization(text []string, speaker []string, username string, language string)
	saveTranslation(text string, username string, is_translation bool, language string, translated_text string, translation_language string)
}

type UserDb struct {
	pool *pgxpool.Pool
}

func NewUserDb(pool *pgxpool.Pool) *UserDb {
	p := &UserDb{}
	p.pool = pool
	return p
}

func (db UserDb) saveTranscription(text string, username string, is_translation bool, language string) {
	_, err := db.pool.Exec(context.Background(), `
    INSERT INTO transcription(app_user_id, content, is_translation, lang) 
    VALUES ((SELECT id FROM app_user WHERE username = $1), $2, $3, $4);
	`, username, text, is_translation, language)
	if err != nil {
		log.Fatal(err)
	}
}

func (db UserDb) editTranscription(id int, username string, new_content string) error {
	_, err := db.pool.Exec(context.Background(), `
    UPDATE transcription set content = $1 where id = $2 and app_user_id = (SELECT id FROM app_user WHERE username = $3);
	`, new_content, id, username)
	return err
}

func (db UserDb) deleteTranscription(id int, username string) error {
	_, err := db.pool.Exec(context.Background(), `
    DELETE FROM transcription WHERE id = $1 and app_user_id = (SELECT id FROM app_user WHERE username = $2);
	`, id, username)
	return err
}

func (db UserDb) saveTranslation(text string, username string, is_translation bool, language string, translated_text string, translation_language string) {
	transcription_id := 0
	err := db.pool.QueryRow(context.Background(), `
    INSERT INTO transcription(app_user_id, content, is_translation, lang) 
    VALUES ((SELECT id FROM app_user WHERE username = $1), $2, $3, $4)
	RETURNING id;
	`, username, text, is_translation, language).Scan(&transcription_id)
	if err != nil {
		log.Fatal(err)
	}
	_, err = db.pool.Exec(context.Background(), `
    INSERT INTO translation(transcription_id, lang, content) 
    VALUES ($1, $2, $3);
	`, transcription_id, translation_language, translated_text)
	if err != nil {
		log.Fatal(err)
	}
}

func (db UserDb) saveDiarization(text []string, speaker []string, username string, language string) {
	diarization_id := 0
	err := db.pool.QueryRow(context.Background(), `
    INSERT INTO diarization(app_user_id, lang) 
    VALUES ((SELECT id FROM app_user WHERE username = $1), $2, $3, $4)
	RETURNING id;
	`, username, language).Scan(&diarization_id)
	if err != nil {
		log.Fatal(err)
	}
	for i, _ := range text {
		_, err = db.pool.Exec(context.Background(),
			`INSERT INTO speaker_line(id, diarization_id, speaker, content) 
    		VALUES ($1, $2, $3, $4);`,
			i, diarization_id, speaker[i], text[i])
		if err != nil {
			log.Fatal(err)
		}
	}
}

func (db UserDb) getUserPassword(username string) (string, error) {
	row := db.pool.QueryRow(context.Background(), "SELECT password_hash FROM app_user WHERE username=$1;", username)
	var password string
	err := row.Scan(&password)
	return password, err
}

func (db UserDb) isUserInDatabase(username string) (bool, error) {
	row := db.pool.QueryRow(context.Background(), "SELECT username FROM app_user WHERE username=$1;", username)
	err := row.Scan(&username)
	if err == sql.ErrNoRows {
		return false, nil
	}
	if err == nil {
		return true, nil
	}
	return false, err
}

func (db UserDb) addUserToDatabase(username string, password string) error {
	password_hash, err := HashPassword(password)
	if err != nil {
		log.Printf("Database error %v", err)
		return err
	}
	_, err = db.pool.Exec(context.Background(), "INSERT INTO app_user(username, password_hash) VALUES ($1, $2);", username, password_hash)
	if err != nil {
		log.Printf("Database error %v", err)
		return err
	}
	return nil
}

func (db UserDb) getUserTranscriptionHistory(username string) (pgx.Rows, error) {
	rows, err := db.pool.Query(context.Background(), "SELECT id, content, created_at FROM transcription WHERE app_user_id=(select id from app_user where username=$1 LIMIT 1);", username)
	if err != nil {
		log.Printf("Database error %v", err)
		return nil, err
	}
	return rows, nil
}
