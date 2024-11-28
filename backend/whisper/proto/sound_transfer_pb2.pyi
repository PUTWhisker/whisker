from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Optional as _Optional

DESCRIPTOR: _descriptor.FileDescriptor

class TextMessage(_message.Message):
    __slots__ = ("text",)
    TEXT_FIELD_NUMBER: _ClassVar[int]
    text: str
    def __init__(self, text: _Optional[str] = ...) -> None: ...

class TranscriptionRequest(_message.Message):
    __slots__ = ("sound_data", "source_language")
    SOUND_DATA_FIELD_NUMBER: _ClassVar[int]
    SOURCE_LANGUAGE_FIELD_NUMBER: _ClassVar[int]
    sound_data: bytes
    source_language: str
    def __init__(self, sound_data: _Optional[bytes] = ..., source_language: _Optional[str] = ...) -> None: ...

class TranslationRequest(_message.Message):
    __slots__ = ("sound_data", "source_language", "translation_language")
    SOUND_DATA_FIELD_NUMBER: _ClassVar[int]
    SOURCE_LANGUAGE_FIELD_NUMBER: _ClassVar[int]
    TRANSLATION_LANGUAGE_FIELD_NUMBER: _ClassVar[int]
    sound_data: bytes
    source_language: str
    translation_language: str
    def __init__(self, sound_data: _Optional[bytes] = ..., source_language: _Optional[str] = ..., translation_language: _Optional[str] = ...) -> None: ...

class SoundResponse(_message.Message):
    __slots__ = ("text", "detected_language")
    TEXT_FIELD_NUMBER: _ClassVar[int]
    DETECTED_LANGUAGE_FIELD_NUMBER: _ClassVar[int]
    text: str
    detected_language: str
    def __init__(self, text: _Optional[str] = ..., detected_language: _Optional[str] = ...) -> None: ...

class SoundStreamResponse(_message.Message):
    __slots__ = ("text", "new_chunk")
    TEXT_FIELD_NUMBER: _ClassVar[int]
    NEW_CHUNK_FIELD_NUMBER: _ClassVar[int]
    text: str
    new_chunk: bool
    def __init__(self, text: _Optional[str] = ..., new_chunk: bool = ...) -> None: ...

class SpeakerAndLineResponse(_message.Message):
    __slots__ = ("text", "speakerName", "detected_language")
    TEXT_FIELD_NUMBER: _ClassVar[int]
    SPEAKERNAME_FIELD_NUMBER: _ClassVar[int]
    DETECTED_LANGUAGE_FIELD_NUMBER: _ClassVar[int]
    text: _containers.RepeatedScalarFieldContainer[str]
    speakerName: _containers.RepeatedScalarFieldContainer[str]
    detected_language: str
    def __init__(self, text: _Optional[_Iterable[str]] = ..., speakerName: _Optional[_Iterable[str]] = ..., detected_language: _Optional[str] = ...) -> None: ...
