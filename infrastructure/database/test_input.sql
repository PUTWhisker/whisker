INSERT INTO transcription (
    app_user_id,
    content,
    is_translation,
    lang
  )
VALUES (
    1,
    'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum',
    false,
    'Fk'
  );

INSERT INTO transcription (
    app_user_id,
    content,
    is_translation,
    lang
  )
VALUES (
    1,
    'Mieć więc ranę tyle znaczy,
Co mieć ciało skaleczone:
Że zaś raną గest drapnięcie,
Więc zapewnić możem święcie,
Że గesteście skaleczeni,
Przez to chleba pozbawieni.',
    true,
    'PL'
  );

INSERT INTO translation (
    transcription_id,
    lang,
    content
  )
VALUES (
    2,
    'FR',
    'Ui ui'
  );