from typing import Union

import sound_transfer_pb2_grpc as Services
import sound_transfer_pb2 as Variables
import audio
import grpc


# declare Keepalive pings
# close connection on ending

class GrpcClient:

    def __init__ (
        self,
        host:str = None,
        port:str = None,
        language:str = None,
        model:str = "small",
        save:str = None
    ):
        self.host = host
        self.port = port
        self.language = language
        self.model = model
        self.save = save


    async def initiateConnection(self, seed:str) -> Union[bool, grpc.RpcError]:
        try:
            self.channel = grpc.aio.insecure_channel(f'{self.host}:{self.port}')
            self.stub = Services.SoundServiceStub(self.channel) # Creating server stub, these are reusable 
            response = await self.stub.TestConnection( # sending generated number
                            Variables.TextMessage(text=seed))
            return response
        except (grpc.RpcError, Exception):
            raise


    async def sendSoundFile(self, audioFile: bytes) -> Union[bool, grpc.RpcError]:
        try:
            flags = [self.language, self.model] # Seting flags for transcribing
            response = await self.stub.SendSoundFile(
                Variables.SoundRequest(
                    sound_data=audioFile,
                    flags=flags)) # Sending audio file to transcribe
            return response
        except (grpc.RpcError, Exception):
            raise


    async def streamSoundFile(self)  -> Union[bool, grpc.RpcError]:
        try:
            recording = audio.AudioRecorder(self.save) # Initiate recording class
            responseIter = self.stub.StreamSoundFile(recording.record()) # Streaming recorded audio yield by record() funciton
            async for response in responseIter:
                print(f'Received from server: {response.text}') # Displaying server's responses
                
        except (grpc.RpcError, Exception):
            raise


 # python -m grpc_tools.protoc -I ./proto --python_out=./proto/ --pyi_out=./proto/ --grpc_python_out=./proto/ ./proto/sound_transfer.proto