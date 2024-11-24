#TODO: waiting animation for SendFileTranslation and diarization doesn't work for reasons
from grpcClient import GrpcClient
from getpass import getpass
from dotenv import load_dotenv, set_key

import random
import asyncio
import grpc
import time
import logging
import os

class LoginFailure(Exception):
    pass

class ConsolePrinter:
    def __init__(
        self,
        host: str = None,
        port: str = None,
        language: str = None,
        model: str = "small",
        save: str = None,
        translation: str = None,
    ):
        self.grpcClient = GrpcClient(host, port, language, model, save, translation)

    def _errorHandler(func):
        async def wrapper(*args, **kwargs):
            start = time.time()
            try:
                return await func(*args, **kwargs)
            except grpc.RpcError as grpcError:
                print(f"Grpc connection failure: {grpcError.details()}") # <- custom message sent by server
                print(f"{grpcError.code()}") # <- one of the 17 rpcError status codes
                print(f"{grpcError.debug_error_string()}") # <- Error message string, Usefull might be IP address and created_time
                print(f"{grpcError.initial_metadata()}") # <- No idea the difference, but metadata sent with the error message
                print(f"{grpcError.trailing_metadata()}") # <- No idea the difference, but metadata sent with the error message
                return
            except Exception as e:
                print(f"This is an unhandled exception: {e}")
                return
            finally:
                end = time.time()
                print(f"Execution time: {end - start}")

        return wrapper

    @_errorHandler
    async def startApp(self):
        seed = str(random.randint(0, 10000))
        conTask = asyncio.create_task(
            self.grpcClient.initiateConnection(seed)
        )  # Initiate connection attempt async
        dot = 0
        while not conTask.done():  # Dot animation until connection task is finished
            dot = self._waitingAnimation(dot)
            await asyncio.sleep(0.1)
        self._waitingAnimation(3)
        await conTask
        connected = conTask.result()
        if connected.text != seed:  # Unsuccessful if server returned different number
            print("Problem connecting to the server")
            return False

        print("Connected to the server!")
        return True
    
    @_errorHandler
    async def diarizateSpeakers(self, audio: bytes):
        sendTask = asyncio.create_task(
            self.grpcClient.diarizateSpeakers(audio)
        )  # Initiate sending file async
        dot = 0
        while not sendTask.done():  # Dot animation until connection task is finished
            dot = self._waitingAnimation(dot)
            await asyncio.sleep(0.1)
        self._waitingAnimation(3)
        script = sendTask.result()  # Received transcribed text
        speaker = list(script.speakerName)
        text = list(script.text)
        for i in range(0, len(speaker)):
            print(f"{speaker[i]}: {text[i]}")


    @_errorHandler
    async def sendFile(self, audio: bytes):
        sendTask = asyncio.create_task(
            self.grpcClient.sendSoundFile(audio)
        )  # Initiate sending file async
        dot = 0
        while not sendTask.done():  # Dot animation until connection task is finished
            dot = self._waitingAnimation(dot)
            await asyncio.sleep(0.1)
        self._waitingAnimation(3)
        script = sendTask.result()  # Received transcribed text
        print(script)


    @_errorHandler
    async def sendFileTranslation(self, audio: bytes):
        responseIter = self.grpcClient.SendSoundFileTranslation(audio)
        dot = 0
        
        async for response in responseIter:
            if "transcription" in response.flags:
                print(f'\033[1mAudio transcription: \033[0m{response.text}')
            elif "translation" in response.flags:
                print(f'\033[1mAudio translation: \033[0m{response.text}')


    async def register(self):
        username = input("Enter your new username: ")
        while True:
            password1 = getpass("Enter your password: ")
            password2 = getpass("Enter your password again: ")
            if password1 == password2:
                break
            else:
                print("Password mismatch, please try again.")
        registered = await self.grpcClient.register(username, password1)
        if not registered.successful:
            print(registered.error)
            raise LoginFailure
        print("Successfully registered your account!")
        return
            

    async def retreiveToken(self, username:str):
        if os.getenv("JWT_TOKEN") == "":
            password = getpass(f"{username}'s password: ")
            env = ".env"
            load_dotenv(env)
            newTokenJWT = await self.grpcClient.retreiveJWT(username, password)
            print(newTokenJWT.JWT)
            print(newTokenJWT.successful)
            if not newTokenJWT.successful:
                raise LoginFailure
            print("Successfully logged into your account!")
            print(newTokenJWT.JWT)
            set_key(env, "JWT_TOKEN", newTokenJWT.JWT)
        else:
            print("You already have retreived a token. Clear env file if you want to retreive a different one.")


    async def getTranslation(self):
        env = ".env"
        load_dotenv(env)
        token = os.getenv("JWT_TOKEN")
        if token != "":
            transcriptions = await self.grpcClient.getTranslation(token)
        else:
            print("Login into your account first!")
        print(transcriptions)
        return


    @_errorHandler
    async def record(self):
        transcription, iter = [""], 0
        responseIter = self.grpcClient.streamSoundFile()  # Initiate streaming file async
        async for response in responseIter:
            transcription[iter] = response.text
            terminalWidth, _ = os.get_terminal_size()
            print(" " * terminalWidth, end="\r", flush=True)
            print(transcription[iter], end="\r", flush=True)  # Delete?
            if response.flags[0] == "True":
                iter += 1
                transcription.append("")
                print()
        print() # Because sometimes the print "Execution time..." overlaps with the last line (idk why)

    def _waitingAnimation(self, dot: int) -> int:
        if dot == 3:
            print(end="\b\b\b", flush=True)
            print(end="   ", flush=True)
            print(end="\b\b\b", flush=True)
            dot = 0
        else:
            print(end=".", flush=True)
            dot += 1
        return dot
