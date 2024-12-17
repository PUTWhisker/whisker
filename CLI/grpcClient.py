from typing import Union
from querryParameters import QuerryParameters

import audio
import os
import sys
import grpc

curDir = os.path.dirname(__file__)
protoDir = os.path.join(curDir, "proto/sound_transfer")
sys.path.insert(0, protoDir)

from proto.sound_transfer import sound_transfer_pb2_grpc as Services
from proto.sound_transfer import sound_transfer_pb2 as Variables

sys.path.insert(0, curDir)
curDir = os.path.dirname(__file__)
protoDir = os.path.join(curDir, "proto/authentication")
sys.path.insert(0, protoDir)

from proto.authentication import authentication_pb2_grpc
from proto.authentication import authentication_pb2


sys.path.insert(0, curDir)


# declare Keepalive pings
# close connection on ending


class GrpcClient:
    def __init__(
        self,
        host: str = None,
        port: str = None,
        language: str = None,
        save: str = None,
        translation: str = None,
        token:str = "",
    ):
        self.host = host
        self.port = port
        self.language = language
        self.save = save
        self.translation = translation
        self.channel = grpc.aio.insecure_channel(f"{self.host}:{self.port}")
        self.stub = Services.SoundServiceStub(
            self.channel
        )  # Creating server stub, these are reusable
        self.token = token


    async def testConnection(self, seed: str) -> Union[bool, grpc.RpcError]:
        try:
            response = await self.stub.TestConnection(  # sending generated number
                Variables.TextMessage(text=seed)
            )
            return response
        except Exception as e:
            raise e
        

    async def diarizateFile(self, audioFile: bytes) -> Union[bool, grpc.RpcError]:
        try:
            metadata = (
                ("jwt", self.token),
            )
            response = await self.stub.DiarizateFile(
                Variables.TranscriptionRequest(sound_data=audioFile,
                                               source_language=self.language),
                                               metadata=metadata
            )
            return response
        except Exception as e:
            raise e
        

    async def transcribeFile(self, audioFile: bytes) -> Union[bool, grpc.RpcError]:
        try:
            metadata = (
                ("jwt", self.token),
            )
            response = await self.stub.TranscribeFile(
                Variables.TranscriptionRequest(sound_data=audioFile,
                                               source_language=self.language),
                metadata=metadata,
            )  # Sending audio file to transcribe
            return response
        except Exception as e:
            raise e
        

    async def translateFile(self, audioFile: bytes) -> Union[bool, grpc.RpcError]:
        try:
            metadata = (
                ("jwt", self.token),
            )
            responseIter = self.stub.TranslateFile(
                Variables.TranslationRequest(sound_data=audioFile,
                                             source_language=self.language,
                                             translation_language=self.translation),
                metadata=metadata,
            )
            async for response in responseIter:
                yield response
        except Exception as e:
            raise e
        

    async def transcribeLive(self) -> Union[bool, grpc.RpcError]:
        try:
            recording = audio.AudioRecorder(self.save)  # Initiate recording class
            metadata = (
                ("jwt", self.token),
                ("source_language", self.language),
            )
            responseIter = self.stub.TranscribeLive(
                recording.record(self.language), metadata=metadata
            )  # Streaming recorded audio yield by record() funciton
            print("Recording started. You may start talking now. Press 'ctrl+c' to stop recording.")
            async for response in responseIter:
                yield response
        except Exception as e:
            raise e


    async def retreiveJWT(self, username:str, password:str) -> Union[bool, grpc.RpcError]:
        try:
            stub = authentication_pb2_grpc.ClientServiceStub(
                self.channel
            )
            tokenJWT = await stub.Login(
                authentication_pb2.UserCredits(username=username, password=password),
            )
            return tokenJWT
        except Exception as e:
            raise e
    
    
    async def register(self, username:str, password:str) -> Union[bool, grpc.RpcError]:
        try:
            stub = authentication_pb2_grpc.ClientServiceStub(
                self.channel
            )
            response = await stub.Register(
                authentication_pb2.UserCredits(username=username, password=password),
            )
            return response
        except Exception as e:
            raise e
        

    async def getTranscription(self, params:QuerryParameters) -> Union[bool, grpc.RpcError]:
        try:
            stub = authentication_pb2_grpc.ClientServiceStub(
                self.channel
            )
            metadata = (
                ("jwt", self.token),
            )
            responseIter = stub.GetTranscription(
                authentication_pb2.QueryParamethers(
                    start_time=params.startTime,
                    end_time=params.endTime,
                    limit=params.limit,
                    language=params.language,
                ),
                metadata=metadata
            )
            async for response in responseIter:
                yield response
        except Exception as e:
            raise e
        
    
    async def editTranscription(self, id:int, content:str) -> Union[bool, grpc.RpcError]:
        try:
            stub = authentication_pb2_grpc.ClientServiceStub(
                self.channel
            )
            metadata = (
                ("jwt", self.token),
            )
            response = await stub.EditTranscription(
                authentication_pb2.NewTranscription(
                    id=id,
                    content=content
                ),
                metadata=metadata
            )
            return response
        except Exception as e:
            raise e
        

    async def deleteTranscription(self, id:int) -> Union[bool, grpc.RpcError]:
        try:
            stub = authentication_pb2_grpc.ClientServiceStub(
                self.channel
            )
            metadata = (
                ("jwt", self.token),
            )
            response = await stub.DeleteTranscription(
                authentication_pb2.Id(
                    id=id,
                ),
                metadata=metadata
            )
            return response
        except Exception as e:
            raise e
        

    async def getTranslation(self, params:QuerryParameters) -> Union[bool, grpc.RpcError]:
        try:
            stub = authentication_pb2_grpc.ClientServiceStub(
                self.channel
            )
            metadata = (
                ("jwt", self.token),
            )
            responseIter = stub.GetTranslation(
                authentication_pb2.QueryParamethers(
                    start_time=params.startTime,
                    end_time=params.endTime,
                    limit=params.limit,
                    language=params.language,
                    translation_language=params.translation_language
                ),
                metadata=metadata
            )
            async for response in responseIter:
                yield response
        except Exception as e:
            raise e
        

    async def editTranslation(self, 
                              id:int, 
                              transcription:str,
                              translation:str,
                              edit_transcription:bool,
                              edit_translation:bool ) -> Union[bool, grpc.RpcError]:
        try:
            stub = authentication_pb2_grpc.ClientServiceStub(
                self.channel
            )
            metadata = (
                ("jwt", self.token),
            )
            response = await stub.EditTranslation(
                authentication_pb2.NewTranslation(
                    id=id,
                    transcription=transcription,
                    translation=translation,
                    edit_transcription=edit_transcription,
                    edit_translation=edit_translation
                ),
                metadata=metadata
            )
            return response
        except Exception as e:
            raise e
        

    async def deleteTranslation(self, id:int) -> Union[bool, grpc.RpcError]:
        try:
            stub = authentication_pb2_grpc.ClientServiceStub(
                self.channel
            )
            metadata = (
                ("jwt", self.token),
            )
            response = await stub.DeleteTranslation(
                authentication_pb2.Id(
                    id=id,
                ),
                metadata=metadata
            )
            return response
        except Exception as e:
            raise e
        

    async def getDiarization(self, params:QuerryParameters) -> Union[bool, grpc.RpcError]:
        try:
            stub = authentication_pb2_grpc.ClientServiceStub(
                self.channel
            )
            metadata = (
                ("jwt", self.token),
            )
            responseIter = stub.GetDiarization(
                authentication_pb2.QueryParamethers(
                    start_time=params.startTime,
                    end_time=params.endTime,
                    limit=params.limit,
                    language=params.language,
                ),
                metadata=metadata
            )
            async for response in responseIter:
                yield response
        except Exception as e:
            raise e


    async def editDiarization(self, id:int, speaker:list, line:list) -> Union[bool, grpc.RpcError]:
        try:
            stub = authentication_pb2_grpc.ClientServiceStub(
                self.channel
            )
            metadata = (
                ("jwt", self.token),
            )
            response = await stub.EditDiarization(
                authentication_pb2.NewDiarization(
                    id=id,
                    line=line,
                    speaker=speaker,
                ),
                metadata=metadata
            )
            return response
        except Exception as e:
            raise e
        

    async def deleteDiarization(self, id:int) -> Union[bool, grpc.RpcError]:
        try:
            stub = authentication_pb2_grpc.ClientServiceStub(
                self.channel
            )
            metadata = (
                ("jwt", self.token),
            )
            response = await stub.DeleteDiarization(
                authentication_pb2.Id(
                    id=id,
                ),
                metadata=metadata
            )
            return response
        except Exception as e:
            raise e