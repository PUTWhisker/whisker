# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# NO CHECKED-IN PROTOBUF GENCODE
# source: authentication.proto
# Protobuf Python Version: 5.28.1
"""Generated protocol buffer code."""
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import runtime_version as _runtime_version
from google.protobuf import symbol_database as _symbol_database
from google.protobuf.internal import builder as _builder
_runtime_version.ValidateProtobufRuntimeVersion(
    _runtime_version.Domain.PUBLIC,
    5,
    28,
    1,
    '',
    'authentication.proto'
)
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


from google.protobuf import timestamp_pb2 as google_dot_protobuf_dot_timestamp__pb2


DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\x14\x61uthentication.proto\x1a\x1fgoogle/protobuf/timestamp.proto\"T\n\x0bTextHistory\x12\x15\n\rtranscription\x18\x01 \x01(\t\x12.\n\ncreated_at\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\"1\n\x0bUserCredits\x12\x10\n\x08username\x18\x01 \x01(\t\x12\x10\n\x08password\x18\x02 \x01(\t\"3\n\x0eStatusResponse\x12\r\n\x05\x65rror\x18\x01 \x01(\t\x12\x12\n\nsuccessful\x18\x02 \x01(\x08\"0\n\rLoginResponse\x12\x0b\n\x03JWT\x18\x01 \x01(\t\x12\x12\n\nsuccessful\x18\x02 \x01(\x08\"\x07\n\x05\x45mpty2\x91\x01\n\rClientService\x12\'\n\x05Login\x12\x0c.UserCredits\x1a\x0e.LoginResponse\"\x00\x12*\n\x0eGetTranslation\x12\x06.Empty\x1a\x0c.TextHistory\"\x00\x30\x01\x12+\n\x08Register\x12\x0c.UserCredits\x1a\x0f.StatusResponse\"\x00\x42J\n\x16io.grpc.authenticationB\x13\x41uthenticationProtoP\x01Z\x19inzynierka/authenticationb\x06proto3')

_globals = globals()
_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, _globals)
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'authentication_pb2', _globals)
if not _descriptor._USE_C_DESCRIPTORS:
  _globals['DESCRIPTOR']._loaded_options = None
  _globals['DESCRIPTOR']._serialized_options = b'\n\026io.grpc.authenticationB\023AuthenticationProtoP\001Z\031inzynierka/authentication'
  _globals['_TEXTHISTORY']._serialized_start=57
  _globals['_TEXTHISTORY']._serialized_end=141
  _globals['_USERCREDITS']._serialized_start=143
  _globals['_USERCREDITS']._serialized_end=192
  _globals['_STATUSRESPONSE']._serialized_start=194
  _globals['_STATUSRESPONSE']._serialized_end=245
  _globals['_LOGINRESPONSE']._serialized_start=247
  _globals['_LOGINRESPONSE']._serialized_end=295
  _globals['_EMPTY']._serialized_start=297
  _globals['_EMPTY']._serialized_end=304
  _globals['_CLIENTSERVICE']._serialized_start=307
  _globals['_CLIENTSERVICE']._serialized_end=452
# @@protoc_insertion_point(module_scope)
