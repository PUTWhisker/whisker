from typing import Union

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
        model: str = "small",
        save: str = None,
        translation: str = None,
    ):
        self.host = host
        self.port = port
        self.language = language
        self.model = model
        self.save = save
        self.translation = translation
        self.channel = grpc.aio.insecure_channel(f"{self.host}:{self.port}")
        self.stub = Services.SoundServiceStub(
            self.channel
        )  # Creating server stub, these are reusable


    async def initiateConnection(self, seed: str) -> Union[bool, grpc.RpcError]:
        try:
            response = await self.stub.TestConnection(  # sending generated number
                Variables.TextMessage(text=seed)
            )
            return response
        except Exception as e:
            raise e
        
        
    async def diarizateSpeakers(self, audioFile: bytes) -> Union[bool, grpc.RpcError]:
        try:
            response = await self.stub.DiarizateSpeakers(
                Variables.SoundRequest(sound_data=audioFile, flags=None),
            )
            return response
        except Exception as e:
            raise e
        

    async def sendSoundFile(self, audioFile: bytes) -> Union[bool, grpc.RpcError]:
        try:
            metadata = (
                ("language", self.language),
            )
            response = await self.stub.SendSoundFile(
                Variables.SoundRequest(sound_data=audioFile, flags=None),
                metadata=metadata,
            )  # Sending audio file to transcribe
            return response
        except Exception as e:
            raise e
        
        
    async def SendSoundFileTranslation(self, audioFile: bytes) -> Union[bool, grpc.RpcError]:
        try:
            metadata = (
                ("language", self.language),
                ("translation", self.translation),
            )
            responseIter = self.stub.SendSoundFileTranslation(
                Variables.SoundRequest(sound_data=audioFile, flags=None),
                metadata=metadata,
            )
            async for response in responseIter:
                yield response
        except Exception as e:
            raise e
        

    async def streamSoundFile(self) -> Union[bool, grpc.RpcError]:
        try:
            recording = audio.AudioRecorder(self.save)  # Initiate recording class
            metadata = (
                ("language", self.language),
                ("translation", self.translation),
            )
            responseIter = self.stub.StreamSoundFile(
                recording.record(), metadata=metadata
            )  # Streaming recorded audio yield by record() funciton
            print("Recording started. You may start talking now.")
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
        
    
    async def getTranslation(self, JWT:str) -> Union[list, grpc.RpcError]:
        try:
            stub = authentication_pb2_grpc.ClientServiceStub(
                self.channel
            )
            metadata = (
                ("jwt", JWT),
            )
            transcriptions = stub.GetTranslation(
                authentication_pb2.Empty(),
                metadata=metadata
            )
            res = list()
            async for transcription in transcriptions:
                res.append(transcription)
            return res
        except Exception as e:
            raise e
