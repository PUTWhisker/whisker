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
                print(
                    f"Grpc connection failure: {grpcError.details()}"
                )  # TODO: when there is implemented secure connection, here handle those exceptions
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
        print(script)


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
        response = await self.grpcClient.SendSoundFileTranslation(audio)
        print(f'Audio transcription: {response[0].text}\n')
        print(f'Audio translation: {response[1].text}\n')


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
        await self.grpcClient.streamSoundFile()  # Initiate streaming file async

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
