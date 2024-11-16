from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Optional as _Optional

DESCRIPTOR: _descriptor.FileDescriptor

class SoundRequest(_message.Message):
    __slots__ = ("sound_data", "flags")
    SOUND_DATA_FIELD_NUMBER: _ClassVar[int]
    FLAGS_FIELD_NUMBER: _ClassVar[int]
    sound_data: bytes
    flags: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, sound_data: _Optional[bytes] = ..., flags: _Optional[_Iterable[str]] = ...) -> None: ...

class SoundResponse(_message.Message):
    __slots__ = ("text",)
    TEXT_FIELD_NUMBER: _ClassVar[int]
    text: str
    def __init__(self, text: _Optional[str] = ...) -> None: ...

class TextMessage(_message.Message):
    __slots__ = ("text",)
    TEXT_FIELD_NUMBER: _ClassVar[int]
    text: str
    def __init__(self, text: _Optional[str] = ...) -> None: ...

class SoundStreamResponse(_message.Message):
    __slots__ = ("text", "flags")
    TEXT_FIELD_NUMBER: _ClassVar[int]
    FLAGS_FIELD_NUMBER: _ClassVar[int]
    text: str
    flags: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, text: _Optional[str] = ..., flags: _Optional[_Iterable[str]] = ...) -> None: ...

class SpeakerAndLine(_message.Message):
    __slots__ = ("text", "speakerName")
    TEXT_FIELD_NUMBER: _ClassVar[int]
    SPEAKERNAME_FIELD_NUMBER: _ClassVar[int]
    text: str
    speakerName: str
    def __init__(self, text: _Optional[str] = ..., speakerName: _Optional[str] = ...) -> None: ...
