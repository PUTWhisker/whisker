from google.protobuf import timestamp_pb2 as _timestamp_pb2
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class TextHistory(_message.Message):
    __slots__ = ("transcription", "created_at")
    TRANSCRIPTION_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    transcription: str
    created_at: _timestamp_pb2.Timestamp
    def __init__(self, transcription: _Optional[str] = ..., created_at: _Optional[_Union[_timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class UserCredits(_message.Message):
    __slots__ = ("username", "password")
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    PASSWORD_FIELD_NUMBER: _ClassVar[int]
    username: str
    password: str
    def __init__(self, username: _Optional[str] = ..., password: _Optional[str] = ...) -> None: ...

class StatusResponse(_message.Message):
    __slots__ = ("error", "successful")
    ERROR_FIELD_NUMBER: _ClassVar[int]
    SUCCESSFUL_FIELD_NUMBER: _ClassVar[int]
    error: str
    successful: bool
    def __init__(self, error: _Optional[str] = ..., successful: bool = ...) -> None: ...

class LoginResponse(_message.Message):
    __slots__ = ("JWT", "successful")
    JWT_FIELD_NUMBER: _ClassVar[int]
    SUCCESSFUL_FIELD_NUMBER: _ClassVar[int]
    JWT: str
    successful: bool
    def __init__(self, JWT: _Optional[str] = ..., successful: bool = ...) -> None: ...

class Empty(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...
