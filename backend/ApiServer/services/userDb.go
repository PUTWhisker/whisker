package services

import (
	"context"
	"fmt"
	"log"
	"strconv"

	pb "inzynierka/server/proto/authentication"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

var (
	noRowsAffected = status.Errorf(codes.NotFound, "No rows affected")
	timeFormat     = "2006-01-02 15:04:05"
)

// TODO make those lines consistent with implementation
type UserDbModel interface {
	getUserInfo(string) (string, string, error)
	isUserInDatabase(string) (bool, error)
	addUserToDatabase(string, string) error

	saveTranscription(text string, username string, is_translation bool, language string, title string) (int, error)
	getTranscriptions(ctx context.Context, user_id string, query *pb.QueryParamethers) (pgx.Rows, error)

	editTranscription(ctx context.Context, id int, user_id string, new_content string) error
	deleteTranscription(ctx context.Context, id int, user_id string) error

	saveTranslation(text string, username string, language string, translated_text string, translation_language string, title string) error
	getTranslations(ctx context.Context, user_id string, query *pb.QueryParamethers) (pgx.Rows, error)
	editTranslation(edit_transcription bool, edit_translation bool, transcription_id int, new_transcription string, new_translation string, user_id string) error

	insertOnlyTranslation(ctx context.Context, in *pb.TranslationText) error
	saveDiarization(text []string, speaker []string, username string, language string, title string) error
	getDiarizations(ctx context.Context, userId string, queryParameters *pb.QueryParamethers) (pgx.Rows, error)

	editDiarization(ctx context.Context, new_content []string, new_speaker []string, id int, userId string) error
	deleteDiarization(ctx context.Context, id int, user_id string) error

	getDiarizationsAndTranscriptions(ctx context.Context, user_id string, query *pb.QueryParamethers) (pgx.Rows, error)
}

type UserDb struct {
	pool *pgxpool.Pool
}

func NewUserDb(pool *pgxpool.Pool) *UserDb {
	p := &UserDb{}
	p.pool = pool
	return p
}

func (db UserDb) saveTranscription(text string, user_id string, is_translation bool, language string, title string) (int, error) {
	var transcription_id int
	err := db.pool.QueryRow(context.Background(), `
    INSERT INTO transcription(app_user_id, content, is_translation, lang, title) 
    VALUES ($1, $2, $3, $4, $5);
	RETURNING id;
	`, user_id, text, true, language, title).Scan(&transcription_id)

	return transcription_id, err
}

func buildQuery(initialQuery string, query *pb.QueryParamethers, mainTableName string) string {
	if query.StartTime != nil {
		initialQuery += " AND created_at >= '" + query.StartTime.AsTime().UTC().Format(timeFormat) + "'"
	}
	if query.EndTime != nil {
		initialQuery += " AND created_at <= '" + query.EndTime.AsTime().UTC().Format(timeFormat) + "'"
	}
	if query.Language != "" {
		initialQuery += " AND " + mainTableName + ".lang = '" + query.Language + "'"
	}
	if query.TranslationLanguage != "" {
		initialQuery += " AND translation.lang = '" + query.TranslationLanguage + "'"
	}
	if query.Limit != 0 {
		initialQuery += " LIMIT " + strconv.Itoa(int(query.Limit))
	}

	initialQuery += ";"
	return initialQuery
}

func (db UserDb) getTranscriptions(ctx context.Context, user_id string, query *pb.QueryParamethers) (pgx.Rows, error) {
	queryText := `SELECT id, content, created_at, lang, title FROM transcription WHERE app_user_id=$1`
	queryText = buildQuery(queryText, query, "transcription")
	return db.pool.Query(ctx, queryText, user_id)
}

func (db UserDb) editTranscription(ctx context.Context, id int, user_id string, new_content string) error {
	result, err := db.pool.Exec(ctx, `
    UPDATE transcription set content = $1 where id = $2;
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

func (db UserDb) saveTranslation(text string, user_id string, language string, translated_text string, translation_language string, title string) error {
	transcription_id := 0
	err := db.pool.QueryRow(context.Background(), `
    INSERT INTO transcription(app_user_id, content, is_translation, lang, title) 
    VALUES ($1, $2, $3, $4, $5)
	RETURNING id;
	`, user_id, text, true, language, title).Scan(&transcription_id)
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

func (db UserDb) insertOnlyTranslation(ctx context.Context, in *pb.TranslationText) error {
	_, err := db.pool.Exec(ctx, `
	INSERT INTO translation (
    	transcription_id,
    	lang,
    	content
  	)
	VALUES (
    	$1,
    	$2,
    	$3
  	)ON CONFLICT (transcription_id)
	DO UPDATE SET
    	lang = EXCLUDED.lang,
    	content = EXCLUDED.content;
	`, in.TranscriptionId, in.Language, in.Content)
	return err
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
		UPDATE translation set content = $1 where transcription_id = $2;
		`, new_translation, transcription_id, user_id)
		return err
	}
	return nil
}

func (db UserDb) getTranslations(ctx context.Context, user_id string, query *pb.QueryParamethers) (pgx.Rows, error) {
	queryText := `
	SELECT 
		transcription.id, 
		transcription.content AS transcription_content, 
		translation.content AS translation_content,
		transcription.created_at,
		transcription.lang AS transcription_language,
		translation.lang AS translation_language,
		transcription.title AS title
	FROM transcription
	JOIN translation 
		ON transcription.id = translation.transcription_id 
	WHERE transcription.app_user_id = $1
	`
	queryText = buildQuery(queryText, query, "transcription")
	return db.pool.Query(ctx, queryText, user_id)
}

func (db UserDb) saveDiarization(text []string, speaker []string, user_id string, language string, title string) error {
	diarization_id := 0
	err := db.pool.QueryRow(context.Background(), `
    INSERT INTO diarization(app_user_id, lang, title) 
    VALUES ($1, $2, $3)
	RETURNING id;
	`, user_id, language, title).Scan(&diarization_id)
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

func (db UserDb) getDiarizations(ctx context.Context, userId string, query *pb.QueryParamethers) (pgx.Rows, error) {
	queryText := `
	SELECT id, created_at, lang, title
    FROM diarization
    WHERE app_user_id = $1`

	queryText = buildQuery(queryText, query, "diarization")

	queryText = "SELECT d.id, speaker_line.speaker, speaker_line.content, d.created_at, d.lang, d.title from (" + queryText[0:len(queryText)-1] + ") AS d join speaker_line on d.id = speaker_line.diarization_id ORDER BY d.created_at, speaker_line.id asc;"
	fmt.Println(queryText)
	return db.pool.Query(ctx, queryText, userId)
}

func (db UserDb) editDiarization(ctx context.Context, new_content []string, new_speaker []string, id int, userId string) error {
	conn, err := db.pool.Acquire(ctx)
	if err != nil {
		return fmt.Errorf("failed to acquire connection: %v", err)
	}
	defer conn.Release()
	var returnedID int
	err = conn.QueryRow(ctx, "SELECT id from diarization where id = $1 and app_user_id = $2", strconv.Itoa(id), userId).Scan(&returnedID)

	if err != nil {
		return err
	}

	tx, err := conn.Begin(ctx)
	if err != nil {
		return fmt.Errorf("failed to begin transaction: %v", err)
	}
	defer tx.Rollback(ctx)

	batch := &pgx.Batch{}
	for i := range new_content {
		batch.Queue(`
		UPDATE speaker_line SET
			content = $1,
			speaker = $2 
		WHERE
			id = $3 AND diarization_id = $4;`, new_content[i], new_speaker[i], i, strconv.Itoa(id))
	}
	results := tx.SendBatch(ctx, batch)
	for i := 0; i < len(new_content); i++ {
		if _, err := results.Exec(); err != nil {
			return fmt.Errorf("failed to execute batch operation: %v", err)
		}
	}
	results.Close()

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

func (db UserDb) getDiarizationsAndTranscriptions(ctx context.Context, user_id string, query *pb.QueryParamethers) (pgx.Rows, error) {
	queryText := `
	SELECT 
		type,
		id,
		title,
		lang,
		created_at,
		content,
		speaker
	FROM (
		SELECT 
			'transcription' AS type,
			t.id AS id,
			t.app_user_id AS user_id,
			t.title AS title,
			t.lang AS lang,
			t.created_at AS created_at,
			t.content AS content,
			'' AS speaker,
			1 AS row_id
		FROM transcription t

		UNION ALL

		SELECT 
			'diarization' AS type,
			d.id AS id,
			d.app_user_id AS user_id,
			d.title AS title,
			d.lang AS lang,
			d.created_at AS created_at,
			sl.content as content,
			sl.speaker AS speaker,
			sl.id as row_id
		FROM diarization d
		LEFT JOIN speaker_line sl ON d.id = sl.diarization_id
	) combined_results WHERE user_id = $1
	`
	queryText = buildQuery(queryText, query, "transcription")
	queryText = queryText[0:len(queryText)-1] + " ORDER BY created_at, row_id ASC;"
	return db.pool.Query(ctx, queryText, user_id)
}
