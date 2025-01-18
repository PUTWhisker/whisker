DROP TABLE IF EXISTS refresh_token;
DROP TABLE IF EXISTS translation;
DROP TABLE IF EXISTS transcription;
DROP TABLE IF EXISTS speaker_line;
DROP TABLE IF EXISTS diarization;
DROP TABLE IF EXISTS app_user;

CREATE TABLE app_user (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refresh_token (
    app_user_id INT UNIQUE REFERENCES app_user(id) ON DELETE CASCADE,
    token_hash VARCHAR(32) NOT NULL,
    expires_at TIMESTAMP NOT NULL
);

CREATE TABLE transcription (
    id SERIAL PRIMARY KEY,
    app_user_id INT REFERENCES app_user(id),
    content TEXT NOT NULL,
    is_translation BOOLEAN NOT NULL,
    lang TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    title VARCHAR(100)
);

CREATE TABLE translation (
    transcription_id INT UNIQUE REFERENCES transcription(id) ON DELETE CASCADE,
    lang VARCHAR(2) NOT NULL,
    content TEXT NOT NULL
);

CREATE TABLE diarization (
    id SERIAL PRIMARY KEY,
    app_user_id INT REFERENCES app_user(id),
    lang VARCHAR(2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    title VARCHAR(100)
);

CREATE TABLE speaker_line(
    id INT,
    diarization_id INT REFERENCES diarization(id) ON DELETE CASCADE,
    speaker VARCHAR(50),
    content TEXT,
    PRIMARY KEY (id, diarization_id)
);
