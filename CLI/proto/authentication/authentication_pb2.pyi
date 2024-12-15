from google.protobuf import timestamp_pb2 as _timestamp_pb2
from google.protobuf import empty_pb2 as _empty_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class TranscriptionHistory(_message.Message):
    __slots__ = ("transcription", "created_at", "id", "language")
    TRANSCRIPTION_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    ID_FIELD_NUMBER: _ClassVar[int]
    LANGUAGE_FIELD_NUMBER: _ClassVar[int]
    transcription: str
    created_at: _timestamp_pb2.Timestamp
    id: int
    language: str
    def __init__(self, transcription: _Optional[str] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., id: _Optional[int] = ..., language: _Optional[str] = ...) -> None: ...

class TranslationHistory(_message.Message):
    __slots__ = ("id", "transcription", "translation", "created_at", "transcription_langauge", "translation_langauge")
    ID_FIELD_NUMBER: _ClassVar[int]
    TRANSCRIPTION_FIELD_NUMBER: _ClassVar[int]
    TRANSLATION_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    TRANSCRIPTION_LANGAUGE_FIELD_NUMBER: _ClassVar[int]
    TRANSLATION_LANGAUGE_FIELD_NUMBER: _ClassVar[int]
    id: int
    transcription: str
    translation: str
    created_at: _timestamp_pb2.Timestamp
    transcription_langauge: str
    translation_langauge: str
    def __init__(self, id: _Optional[int] = ..., transcription: _Optional[str] = ..., translation: _Optional[str] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., transcription_langauge: _Optional[str] = ..., translation_langauge: _Optional[str] = ...) -> None: ...

class DiarizationHistory(_message.Message):
    __slots__ = ("diarization_id", "speaker", "line", "created_at", "language")
    DIARIZATION_ID_FIELD_NUMBER: _ClassVar[int]
    SPEAKER_FIELD_NUMBER: _ClassVar[int]
    LINE_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    LANGUAGE_FIELD_NUMBER: _ClassVar[int]
    diarization_id: int
    speaker: _containers.RepeatedScalarFieldContainer[str]
    line: _containers.RepeatedScalarFieldContainer[str]
    created_at: _timestamp_pb2.Timestamp
    language: str
    def __init__(self, diarization_id: _Optional[int] = ..., speaker: _Optional[_Iterable[str]] = ..., line: _Optional[_Iterable[str]] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., language: _Optional[str] = ...) -> None: ...

class UserCredits(_message.Message):
    __slots__ = ("username", "password")
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    PASSWORD_FIELD_NUMBER: _ClassVar[int]
    username: str
    password: str
    def __init__(self, username: _Optional[str] = ..., password: _Optional[str] = ...) -> None: ...

class LoginResponse(_message.Message):
    __slots__ = ("JWT", "successful")
    JWT_FIELD_NUMBER: _ClassVar[int]
    SUCCESSFUL_FIELD_NUMBER: _ClassVar[int]
    JWT: str
    successful: bool
    def __init__(self, JWT: _Optional[str] = ..., successful: bool = ...) -> None: ...

class Id(_message.Message):
    __slots__ = ("id",)
    ID_FIELD_NUMBER: _ClassVar[int]
    id: int
    def __init__(self, id: _Optional[int] = ...) -> None: ...

class NewTranscription(_message.Message):
    __slots__ = ("id", "content")
    ID_FIELD_NUMBER: _ClassVar[int]
    CONTENT_FIELD_NUMBER: _ClassVar[int]
    id: int
    content: str
    def __init__(self, id: _Optional[int] = ..., content: _Optional[str] = ...) -> None: ...

class NewTranslation(_message.Message):
    __slots__ = ("id", "transcription", "translation", "edit_transcription", "edit_translation")
    ID_FIELD_NUMBER: _ClassVar[int]
    TRANSCRIPTION_FIELD_NUMBER: _ClassVar[int]
    TRANSLATION_FIELD_NUMBER: _ClassVar[int]
    EDIT_TRANSCRIPTION_FIELD_NUMBER: _ClassVar[int]
    EDIT_TRANSLATION_FIELD_NUMBER: _ClassVar[int]
    id: int
    transcription: str
    translation: str
    edit_transcription: bool
    edit_translation: bool
    def __init__(self, id: _Optional[int] = ..., transcription: _Optional[str] = ..., translation: _Optional[str] = ..., edit_transcription: bool = ..., edit_translation: bool = ...) -> None: ...

class NewDiarization(_message.Message):
    __slots__ = ("id", "line", "speaker")
    ID_FIELD_NUMBER: _ClassVar[int]
    LINE_FIELD_NUMBER: _ClassVar[int]
    SPEAKER_FIELD_NUMBER: _ClassVar[int]
    id: int
    line: _containers.RepeatedScalarFieldContainer[str]
    speaker: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, id: _Optional[int] = ..., line: _Optional[_Iterable[str]] = ..., speaker: _Optional[_Iterable[str]] = ...) -> None: ...

class QueryParamethers(_message.Message):
    __slots__ = ("start_time", "end_time", "limit", "language", "translation_language")
    START_TIME_FIELD_NUMBER: _ClassVar[int]
    END_TIME_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    LANGUAGE_FIELD_NUMBER: _ClassVar[int]
    TRANSLATION_LANGUAGE_FIELD_NUMBER: _ClassVar[int]
    start_time: _timestamp_pb2.Timestamp
    end_time: _timestamp_pb2.Timestamp
    limit: int
    language: str
    translation_language: str
    def __init__(self, start_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., end_time: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ..., limit: _Optional[int] = ..., language: _Optional[str] = ..., translation_language: _Optional[str] = ...) -> None: ...
