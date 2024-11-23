DROP TABLE IF EXISTS transcription;
DROP TABLE IF EXISTS app_user;
DROP TABLE IF EXISTS refresh_token;
DROP TABLE IF EXISTS translation;
DROP TABLE IF EXISTS diarization;

CREATE TYPE diarization_content AS (
    id INT NOT NULL,
    speaker VARCHAR(50) NOT NULL,
    content TEXT NOT NULL
);

CREATE TABLE app_user (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refresh_token (
    app_user_id VARCHAR(100) UNIQUE REFERENCES app_user(id),
    content VARCHAR(100),
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transcription (
    id SERIAL PRIMARY KEY,
    app_user_id INT REFERENCES app_user(id),
    content TEXT NOT NULL,
    is_translation BOOLEAN NOT NULL,
    lang TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE translation (
    transcription_id INT UNIQUE REFERENCES transcription(id),
    lang VARCHAR(2) NOT NULL,
    content TEXT NOT NULL
);

CREATE TABLE diarization (
    id SERIAL PRIMARY KEY,
    app_user_id INT REFERENCES app_user(id),
    content diarization_content[] NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
