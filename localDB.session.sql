ALTER TABLE transcription DROP CONSTRAINT transcription_app_user_id_fkey;
ALTER TABLE translation DROP CONSTRAINT translation_transcription_id_fkey;
ALTER TABLE speaker_line DROP CONSTRAINT speaker_line_diarization_id_fkey;

-- Recreate the foreign key constraints with ON DELETE CASCADE
ALTER TABLE transcription
ADD CONSTRAINT transcription_app_user_id_fkey
FOREIGN KEY (app_user_id) REFERENCES app_user(id);

ALTER TABLE translation
ADD CONSTRAINT translation_transcription_id_fkey
FOREIGN KEY (transcription_id) REFERENCES transcription(id) ON DELETE CASCADE;

ALTER TABLE speaker_line
ADD CONSTRAINT speaker_line_diarization_id_fkey
FOREIGN KEY (diarization_id) REFERENCES diarization(id) ON DELETE CASCADE;