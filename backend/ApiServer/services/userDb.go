package services

import (
	"context"
	"fmt"
	"log"
	"strconv"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
	"google.golang.org/protobuf/types/known/timestamppb"
)

var (
	noRowsAffected = status.Errorf(codes.NotFound, "No rows affected")
	timeFormat     = "2024-11-24 12:00:00"
)

// TODO make those lines consistent with implementation
type UserDbModel interface {
	getUserInfo(string) (string, string, error)
	isUserInDatabase(string) (bool, error)
	addUserToDatabase(string, string) error

	saveTranscription(text string, username string, is_translation bool, language string) error
	getUserTranscriptionHistory(ctx context.Context, user_id string, start_time *timestamppb.Timestamp, endTime *timestamppb.Timestamp, limit int) (pgx.Rows, error)
	editTranscription(ctx context.Context, id int, user_id string, new_content string) error
	deleteTranscription(ctx context.Context, id int, user_id string) error

	saveTranslation(text string, username string, is_translation bool, language string, translated_text string, translation_language string) error
	getUserTranslationHistory(ctx context.Context, user_id string, start_time *timestamppb.Timestamp, endTime *timestamppb.Timestamp, limit int) (pgx.Rows, error)
	editTranslation(edit_transcription bool, edit_translation bool, transcription_id int, new_transcription string, new_translation string, user_id string) error

	// saveDiarization(text []string, speaker []string, username string, language string) error
	// editDiarization(ctx context.Context, new_content []string, new_speaker []string, diarization_id string) error
	// getUserDiarizationHistory(userId string) (pgx.Rows, error)
	// deleteDiarization(ctx context.Context, id int, user_id string) error
}

type UserDb struct {
	pool *pgxpool.Pool
}

func NewUserDb(pool *pgxpool.Pool) *UserDb {
	p := &UserDb{}
	p.pool = pool
	return p
}

func (db UserDb) saveTranscription(text string, user_id string, is_translation bool, language string) error {
	_, err := db.pool.Exec(context.Background(), `
    INSERT INTO transcription(app_user_id, content, is_translation, lang) 
    VALUES $1, $2, $3, $4);
	`, user_id, text, is_translation, language)
	return err
}

func buildQuery(initialQuery string, startTime *timestamppb.Timestamp, endTime *timestamppb.Timestamp, limit int) string {
	if startTime != nil {
		initialQuery += " AND crated_at >= " + startTime.AsTime().Format(timeFormat)
	}
	if endTime != nil {
		initialQuery += " AND created_at <= " + endTime.AsTime().Format(timeFormat)
	}
	if limit != 0 {
		initialQuery += " LIMIT " + strconv.Itoa(limit)
	}
	initialQuery += ";"
	return initialQuery
}

func (db UserDb) getUserTranscriptionHistory(ctx context.Context, user_id string, startTime *timestamppb.Timestamp, endTime *timestamppb.Timestamp, limit int) (pgx.Rows, error) {
	queryText := `SELECT id, content, created_at FROM transcription WHERE app_user_id=$1`
	queryText = buildQuery(queryText, startTime, endTime, limit)
	rows, err := db.pool.Query(ctx, queryText, user_id)

	fmt.Println(queryText)
	if err != nil {
		log.Printf("Database error %v", err)
		return nil, err
	}
	return rows, nil
}

func (db UserDb) editTranscription(ctx context.Context, id int, user_id string, new_content string) error {
	result, err := db.pool.Exec(ctx, `
    UPDATE transcription set content = $1 where id = $2 and app_user_id = $3;
	`, new_content, id, user_id)
	nrows := result.RowsAffected()
	if nrows < 1 {
		return noRowsAffected
	}

	return err
}

func (db UserDb) deleteTranscription(ctx context.Context, id int, user_id string) error {
	result, err := db.pool.Exec(ctx, `
    DELETE FROM transcription WHERE id = $1 and app_user_id = $2;
	`, id, user_id)
	nrows := result.RowsAffected()
	fmt.Print(err)

	if nrows < 1 {
		return noRowsAffected
	}
	return err
}

func (db UserDb) saveTranslation(text string, user_id string, is_translation bool, language string, translated_text string, translation_language string) error {
	transcription_id := 0
	err := db.pool.QueryRow(context.Background(), `
    INSERT INTO transcription(app_user_id, content, is_translation, lang) 
    VALUES ($1, $2, $3, $4)
	RETURNING id;
	`, user_id, text, is_translation, language).Scan(&transcription_id)
	if err != nil {
		return (err)
	}
	_, err = db.pool.Exec(context.Background(), `
    INSERT INTO translation(transcription_id, lang, content) 
    VALUES ($1, $2, $3);
	`, transcription_id, translation_language, translated_text)
	if err != nil {
		return (err)
	}
	return nil
}

func (db UserDb) editTranslation(edit_transcription bool, edit_translation bool, transcription_id int, new_transcription string, new_translation string, user_id string) error {
	if edit_transcription {
		_, err := db.pool.Exec(context.Background(), `
		UPDATE transcription set content = $1 where id = $2 and app_user_id = $3;
		`, new_transcription, transcription_id, user_id)
		return err
	}
	if edit_translation {
		_, err := db.pool.Exec(context.Background(), `
		UPDATE translation set content = $1 where transcription_id = $2 and app_user_id = $3;
		`, new_translation, transcription_id, user_id)
		return err
	}
	return nil
}

func (db UserDb) getUserTranslationHistory(ctx context.Context, user_id string, start_time *timestamppb.Timestamp, endTime *timestamppb.Timestamp, limit int) (pgx.Rows, error) {
	query := `
	SELECT 
		transcription.id, 
		transcription.content AS transcription_content, 
		translation.content AS translation_content,
		transcription.created_at 
	FROM transcription
	JOIN translation 
    ON transcription.id = translation.transcription_id 
	WHERE transcription.id = $1;
	`
	query = buildQuery(query, start_time, endTime, limit)

	rows, err := db.pool.Query(context.Background(), query, user_id)
	if err != nil {
		log.Printf("Database error %v", err)
		return nil, err
	}
	return rows, nil
}

func (db UserDb) saveDiarization(text []string, speaker []string, user_id string, language string) error {
	diarization_id := 0
	err := db.pool.QueryRow(context.Background(), `
    INSERT INTO diarization(app_user_id, lang) 
    VALUES ($1, $2, $3, $4)
	RETURNING id;
	`, user_id, language).Scan(&diarization_id)
	if err != nil {
		return err
	}
	for i := range text {
		_, err = db.pool.Exec(context.Background(),
			`INSERT INTO speaker_line(id, diarization_id, speaker, content) 
    		VALUES ($1, $2, $3, $4);`,
			i, diarization_id, speaker[i], text[i])
		if err != nil {
			return err
		}
	}
	return nil
}

func (db UserDb) editDiarization(ctx context.Context, new_content []string, new_speaker []string, diarization_id string) error {
	conn, err := db.pool.Acquire(ctx)
	if err != nil {
		return fmt.Errorf("failed to acquire connection: %v", err)
	}
	defer conn.Release()

	tx, err := conn.Begin(ctx)
	if err != nil {
		return fmt.Errorf("failed to begin transaction: %v", err)
	}
	defer tx.Rollback(ctx)

	var min int
	err = db.pool.QueryRow(context.Background(), `
    select max(id), min(id) from speaker_line where diarization_id = $1;
	`, diarization_id).Scan(&min)

	if err != nil {
		return fmt.Errorf("failed to create temporary table: %v", err)
	}

	_, err = tx.Exec(context.Background(), `
	CREATE TEMP TABLE temp_updates (id INT, content TEXT) ON COMMIT DROP;
	`)
	if err != nil {
		return fmt.Errorf("failed to create temporary table: %v", err)
	}

	batch := &pgx.Batch{}
	for _, update := range new_content {
		batch.Queue("INSERT INTO temp_updates (id, content) VALUES ($1, $2)", min, update)
	}

	_, err = tx.Exec(context.Background(), `
        UPDATE speaker_line
        SET content = temp_updates.content
        FROM temp_updates
        WHERE speaker_line.id = temp_updates.id;
    `)
	if err != nil {
		return fmt.Errorf("failed to update target table: %v", err)
	}

	err = tx.Commit(ctx)
	if err != nil {
		return fmt.Errorf("failed to commit transaction: %v", err)
	}
	return nil
}

func (db UserDb) deleteDiarization(ctx context.Context, id int, user_id string) error {
	result, err := db.pool.Exec(ctx, `
    DELETE FROM diarization WHERE id = $1 and app_user_id = $2;
	`, id, user_id)
	nrows := result.RowsAffected()
	fmt.Print(err)

	if nrows < 1 {
		return noRowsAffected
	}
	return err
}

func (db UserDb) getUserInfo(username string) (string, string, error) {
	row := db.pool.QueryRow(context.Background(), "SELECT id, password_hash FROM app_user WHERE username=$1;", username)
	var password string
	var database_id string
	err := row.Scan(&database_id, &password)
	return database_id, password, err
}

func (db UserDb) isUserInDatabase(username string) (bool, error) {
	var count int
	err := db.pool.QueryRow(context.Background(), "SELECT COUNT(username) FROM app_user WHERE username=$1;", username).Scan(&count)
	if err != nil {
		return false, err
	}
	return count > 0, nil
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
