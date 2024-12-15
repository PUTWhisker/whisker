#TODO: waiting animation for SendFileTranslation and diarization doesn't work for reasons
from grpcClient import GrpcClient
from getpass import getpass

import random
import asyncio
import grpc
import time
import logging
import os
import math
import subprocess
import platform

class LoginFailure(Exception):
    pass

class ConsolePrinter:
    def __init__(
        self,
        host: str = None,
        port: str = None,
        language: str = None,
        save: str = None,
        translation: str = None,
        token:str = ""
    ):
        self.grpcClient = GrpcClient(host, port, language, save, translation, token)
        self.token = token
        self.language = language


    def _errorHandler(func):
        async def wrapper(*args, **kwargs):
            start = time.time()
            try:
                return await func(*args, **kwargs)
            except grpc.RpcError as grpcError:
                print(f"Grpc connection failure: {grpcError.details()}") # <- custom message sent by server
                print(f"{grpcError.code()}") # <- one of the 17 rpcError status codes
                print(f"{grpcError.debug_error_string()}") # <- Error message string, Usefull might be IP address and created_time
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
            self.grpcClient.testConnection(seed)
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
            self.grpcClient.diarizateFile(audio)
        )  # Initiate sending file async
        dot = 0
        while not sendTask.done():  # Dot animation until connection task is finished
            dot = self._waitingAnimation(dot)
            await asyncio.sleep(0.1)
        self._waitingAnimation(3)
        scripts = sendTask.result()  # Received transcribed text
        speakers = list(scripts.speakerName)
        speakerColors = self._generateRandomUniqueColors(speakers)
        texts = list(scripts.text)
        if self.language is None:
            print(f"Detected transcription language: {scripts.detected_language}")
        for speaker, text in zip(speakers, texts):
            print(f"\033[1m\033[38;2;{speakerColors[speaker][0]};{speakerColors[speaker][1]};{speakerColors[speaker][2]}m{speaker}\033[0m: {text}")


    @_errorHandler
    async def sendFile(self, audio: bytes):
        sendTask = asyncio.create_task(
            self.grpcClient.transcribeFile(audio)
        )  # Initiate sending file async
        dot = 0
        while not sendTask.done():  # Dot animation until connection task is finished
            dot = self._waitingAnimation(dot)
            await asyncio.sleep(0.1)
        self._waitingAnimation(3)
        response = sendTask.result()  # Received transcribed text
        if self.language is None:
            print(f"Detected transcription language: {response.detected_language}")
        print(f"{response.text}")


    @_errorHandler
    async def sendFileTranslation(self, audio: bytes):
        responseIter = self.grpcClient.translateFile(audio)
        awaitingTranscription = True
        async for response in responseIter:
            if awaitingTranscription:
                awaitingTranscription = False
                if self.language is None:
                    print(f"Detected transcription language: {response.detected_language}")
                print(f'\033[1mAudio transcription: \033[0m{response.text}')
            else:
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
            newTokenJWT = await self.grpcClient.retreiveJWT(username, password)
            print(newTokenJWT.successful)
            if not newTokenJWT.successful:
                raise LoginFailure
            print("Successfully logged into your account!")
            return newTokenJWT.JWT
        else:
            print("You already have retreived a token. Clear env file if you want to retreive a different one.")


    async def getTranslation(self):
        transcriptions = await self.grpcClient.getTranslation(self.token)
        print(transcriptions)

    @_errorHandler
    async def record(self):
        transcription, iter = [""], 0
        responseIter = self.grpcClient.transcribeLive()  # Initiate streaming file async
        async for response in responseIter:
            transcription[iter] = response.text
            terminalWidth, _ = os.get_terminal_size()
            print(" " * terminalWidth, end="\r", flush=True)
            print(transcription[iter], end="\r", flush=True)  # Delete?
            if response.new_chunk == "True":
                iter += 1
                transcription.append("")
                print()
        print() # Because sometimes the print "Execution time..." overlaps with the last line (idk why)

    async def edit(self):
        exampleText = "Never gonna give you up. Never gonna let you down. Never gonna run around and desert you."
        # Get desired transcription history here
        fileName = "./temp.txt"
        with open(fileName, "w") as file:
            file.write(exampleText)
        if platform.system() == "Windows":
            editor = "notepad.exe"
        elif platform.system() == "Linux":
            editor = "vim"
        elif platform.system() == "Darwin":
            editor = "vim"
        else:
            print("No support for text editing on this system.")
            return
        #subprocess.call(os.system(os.path.dirname(__file__) + fileName))
        subprocess.call([editor, fileName])
        with open(fileName, "r") as file:
            exampleText = file.read()
        print(f'{exampleText}')
        # Submit changes here
        os.remove("./temp.txt")

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

    def _generateRandomUniqueColors(self, speakers: str) -> dict:
        uniqueSpeakers = set(speakers)
        speakers = list(uniqueSpeakers)
        colors = dict()
        for speaker in speakers:
            while True:
                isSimilar = False
                newColor = list(random.sample(range(256), 3))
                newColor.append(sum(newColor))
                for _, value in colors.items():
                    if abs(value[3] - newColor[3]) < math.floor((3 * 256) / len(uniqueSpeakers) * 0.7):
                        isSimilar = True
                        break
                if not isSimilar:
                    colors[speaker] = newColor
                    break
        return colors