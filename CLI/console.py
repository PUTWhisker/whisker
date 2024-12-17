#TODO: waiting animation for SendFileTranslation and diarization doesn't work for reasons
from grpcClient import GrpcClient
from getpass import getpass
from querryParameters import QuerryParameters

import random
import asyncio
import grpc
import time
import logging
import os
import math
import subprocess
import platform
import datetime
import sys
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
            print(f"\033[1m\033[38;2;{speakerColors[speaker][0]};{speakerColors[speaker][1]};{speakerColors[speaker][2]}m{speaker}: {text}\033[0m")


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


    @_errorHandler
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
            

    @_errorHandler
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


    @_errorHandler
    async def record(self):
        transcription, iter = [""], 0
        responseIter = self.grpcClient.transcribeLive()  # Initiate streaming file async
        async for response in responseIter:
            transcription[iter] = response.text
            terminalWidth, _ = os.get_terminal_size()
            print(" " * terminalWidth, end="\r", flush=True)
            print(transcription[iter], end="\r", flush=True)
            if response.new_chunk == "True":
                iter += 1
                transcription.append("")
                print()
        print() # Because sometimes the print "Execution time..." overlaps with the last line (idk why)


    async def retrieveHistory(self):
        print("Choose which one you want to retrieve:\n1) Transcription\n2) Translation\n3) Diarization")
        choice = input("Your input: ")
        while choice != "1" and choice != "2" and choice != "3":
            print("Incorrect choice.")
            choice = input("Your input: ")
        if choice == "2":
            await self.translationHistory()
        else:
            if choice == "1":
                await self.transcriptionHistory()
            else:
                await self.diarizationHistory()


    @_errorHandler
    async def transcriptionHistory(self):
        async def transcriptionHistoryRequest(params: QuerryParameters):
            history = dict()
            responseIter = self.grpcClient.getTranscription(params)
            async for response in responseIter:
                datetimeObject = datetime.datetime.fromtimestamp(response.created_at.seconds)
                creationTime = datetimeObject.replace(microsecond=response.created_at.nanos // 1000)
                history[str(response.id)] = response.transcription
                print(f'\033[1m[{response.id} | {creationTime} | {response.language}]:\033[0m {response.transcription}')
            return history
        try:
            print("Choose filters for transcription retrieving")
            params = await self._setParameters(QuerryParameters(False))
            params.convertDate()
            print("Response structure: \033[1m[id | creation_time | lanugage]:\033[0m transcription")
            history = await transcriptionHistoryRequest(params)
            print("\nWrite 'e<id>' to edit transcription. Write 'd<id>' to delete transcription. Write '0' to end program.")
            choice = input()
            while choice != 0:
                if len(choice) > 1 and choice[1:len(choice)].isdigit() and choice[1:len(choice)] in history:
                    if choice[0] == "e":
                        correctedTranscription = await self._editTranscription(history[choice[1:len(choice)]])
                        await self.grpcClient.editTranscription(int(choice[1:len(choice)]), correctedTranscription)
                        history = await transcriptionHistoryRequest(params)
                        print("\nWrite 'e<id>' to edit transcription. Write 'd<id>' to delete transcription. Write '0' to end program.")
                    elif choice[0] == "d":
                        await self.grpcClient.deleteTranscription(int(choice[1:len(choice)]))
                        history = await transcriptionHistoryRequest(params)
                        print("\nWrite 'e<id>' to edit transcription. Write 'd<id>' to delete transcription. Write '0' to end program.")
                elif choice == "0":
                    break
                else:
                    print("\033[1mIncorrect input.\033[0m")
                choice = input()
        except Exception as e:
            print(e)
        return
    

    @_errorHandler
    async def translationHistory(self):
        async def translationHistoryRequest(params: QuerryParameters):
            history = dict()
            responseIter = self.grpcClient.getTranslation(params)
            async for response in responseIter:
                datetimeObject = datetime.datetime.fromtimestamp(response.created_at.seconds)
                creationTime = datetimeObject.replace(microsecond=response.created_at.nanos // 1000)
                history[str(response.id)] = (response.transcription, response.translation)
                print(f'\033[1m[{response.id} | {creationTime} | {response.transcription_langauge} -> {response.translation_langauge}]:\nTranscription:\033[0m {response.transcription}\n\033[1mTranslation:\033[0m {response.translation}')
            return history
        
        print("Choose filters for translation retrieving")
        params = await self._setParameters(QuerryParameters(True))
        params.convertDate()
        print("Response structure: \033[1m[id | creation_time | audio_language -> translate_language]\033[0m\n transcription\n translation")
        history = await translationHistoryRequest(params)
        print("\nWrite 'e<id>' to edit translation. Write 'd<id>' to delete translation. Write '0' to end program.")
        choice = input()
        while choice != 0:
            if len(choice) > 1 and choice[1:len(choice)].isdigit() and choice[1:len(choice)] in history:
                if choice[0] == "e":
                    editTranscription, editTranslation = False, False
                    correctedTranslation = await self._editTranslation(history[choice[1:len(choice)]])
                    if correctedTranslation[0] != history[choice[1:len(choice)]][0]:
                        editTranscription = True
                    if correctedTranslation[1] != history[choice[1:len(choice)]][1]:
                        editTranslation = True
                    await self.grpcClient.editTranslation(int(choice[1:len(choice)]), correctedTranslation[0], correctedTranslation[1], editTranscription, editTranslation)
                    history = await translationHistoryRequest(params)
                    print("\nWrite 'e<id>' to edit translation. Write 'd<id>' to delete translation. Write '0' to end program.")
                elif choice[0] == "d":
                    await self.grpcClient.deleteTranslation(int(choice[1:len(choice)]))
                    history = await translationHistoryRequest(params)
                    print("\nWrite 'e<id>' to edit translation. Write 'd<id>' to delete translation. Write '0' to end program.")
            elif choice == "0":
                break
            else:
                print("\033[1mIncorrect input.\033[0m")
            choice = input()
        return


    @_errorHandler
    async def diarizationHistory(self):
        async def diarizationHistoryRequest(params: QuerryParameters):
            history = dict()
            responseIter = self.grpcClient.getDiarization(params)
            async for response in responseIter:
                datetimeObject = datetime.datetime.fromtimestamp(response.created_at.seconds)
                creationTime = datetimeObject.replace(microsecond=response.created_at.nanos // 1000)
                history[str(response.diarization_id)] = (response.speaker, response.line)
                print(f'\033[1m[{response.diarization_id} | {creationTime} | {response.language}]:\033[0m')
                speakerColors = self._generateRandomUniqueColors(response.speaker)
                for speaker, line in zip(response.speaker, response.line):
                    print(f"\033[1m\033[38;2;{speakerColors[speaker][0]};{speakerColors[speaker][1]};{speakerColors[speaker][2]}m{speaker}: {line}\033[0m")
            return history
        
        print("Choose filters for diarization retrieving")
        params = await self._setParameters(QuerryParameters(False))
        params.convertDate()
        print("Response structure: \033[1m[id | creation_time | language]\033[0m\nspeaker: speaker_line")
        history = await diarizationHistoryRequest(params)
        print("\nWrite 'e<id>' to edit dialog. Write 'd<id>' to delete dialog. Write '0' to end program.")
        choice = input()
        while choice != 0:
            if len(choice) > 1 and choice[1:len(choice)].isdigit() and choice[1:len(choice)] in history:
                if choice[0] == "e":
                    correctedDiarization = await self._editDiarization(history[choice[1:len(choice)]])
                    await self.grpcClient.editDiarization(int(choice[1:len(choice)]), correctedDiarization[0], correctedDiarization[1])
                    history = await diarizationHistoryRequest(params)
                    print("\nWrite 'e<id>' to edit dialog. Write 'd<id>' to delete dialog. Write '0' to end program.")
                elif choice[0] == "d":
                    await self.grpcClient.deleteDiarization(int(choice[1:len(choice)]))
                    history = await diarizationHistoryRequest(params)
                    print("\nWrite 'e<id>' to edit dialog. Write 'd<id>' to delete dialog. Write '0' to end program.")
            elif choice == "0":
                break
            else:
                print("\033[1mIncorrect input.\033[0m")
            choice = input()
        return


    async def _editTranscription(self, initialContent:str) -> str:
        try:
            fileName = "./temp.txt"
            with open(fileName, "w") as file:
                file.write(initialContent)
            if platform.system() == "Windows":
                editor = "notepad.exe"
            elif platform.system() == "Linux":
                editor = "vim"
            elif platform.system() == "Darwin":
                editor = "vim"
            else:
                print("No support for text editing on this system.")
                return initialContent
            subprocess.call([editor, fileName])
            with open(fileName, "r") as file:
                correctedContent = file.read()
            os.remove("./temp.txt")
            return correctedContent
        except Exception:
            print("There was an issue when reading corrected transcription. Make sure you followed the instructions.")
            return initialContent
    

    async def _editTranslation(self, initialContent:tuple) -> tuple:
        try:
            fileName = "./temp.txt"
            with open(fileName, "w") as file:
                file.write("#Do not delete this line. Correct transcription/translation with blank line between them.\n")
                file.write(initialContent[0] + "\n\n" + initialContent[1])
            if platform.system() == "Windows":
                editor = "notepad.exe"
            elif platform.system() == "Linux":
                editor = "vim"
            elif platform.system() == "Darwin":
                editor = "vim"
            else:
                print("No support for text editing on this system.")
                return initialContent
            subprocess.call([editor, fileName])
            correctedTranscription, correctedTranslation = "", ""
            chunk = 0
            with open(fileName, "r") as file:
                next(file)
                for line in file:
                    readLine = line.strip()
                    if not readLine:
                        chunk += 1
                        if chunk == 2:
                            break
                        continue
                    if chunk == 0:
                        correctedTranscription += readLine
                    else:
                        correctedTranslation += readLine
            os.remove("./temp.txt")
            return (correctedTranscription, correctedTranslation)
        except Exception:
            print("There was an issue when reading corrected translation. Make sure you followed the instructions.")
            return initialContent


    async def _editDiarization(self, initialContent:tuple) -> tuple:
        try:
            fileName = "./temp.txt"
            with open(fileName, "w") as file:
                speakerSet = set(initialContent[0])
                file.write("#Do not delete this line. To change speakers use pattern: speaker->corrected_speaker.\n")
                for speaker in speakerSet:
                    file.write(f"{speaker}->{speaker}\n")
                file.write("\n#Do not delete this line. Change speaker's line without changing speaker's name here. Leave a blank line above this comment\n")
                for speaker, line in zip(initialContent[0], initialContent[1]):
                    file.write(f"{speaker}: {line}\n")
            if platform.system() == "Windows":
                editor = "notepad.exe"
            elif platform.system() == "Linux":
                editor = "vim"
            elif platform.system() == "Darwin":
                editor = "vim"
            else:
                print("No support for text editing on this system.")
                return initialContent
            subprocess.call([editor, fileName])
            correctedSpeakers, correctedLines = list(), list()
            with open(fileName, "r") as file:
                speakerDict = dict()
                chunk = 0
                next(file)
                for line in file:
                    readLine = line.strip()
                    if not readLine:
                        chunk += 1
                        next(file)
                        if chunk == 2:
                            break
                        continue
                    if chunk == 0:
                        splited = readLine.split('->')
                        speakerDict[splited[0]] = splited[1]
                    else:
                        splited = readLine.split(': ')
                        correctedLines.append(splited[1])
                        if splited[0] in speakerDict:
                            correctedSpeakers.append(speakerDict[splited[0]])
                        else:
                            correctedSpeakers.append(splited[0])
            os.remove("./temp.txt")
            return (correctedSpeakers, correctedLines)
        except Exception:
            print("There was an issue when reading corrected diarization. Make sure you followed the instructions.")
            return initialContent


    async def _setParameters(self, params: QuerryParameters):
        choice = -1
        while choice != "0":
            print("=======================================")
            print("1) From date:", params.startTime)
            print("2) To date:", params.endTime)
            print("3) Limit:", params.limit)
            print("4) Transcription language:", params.language)
            if (params.translate):
                print("5) Translation language:", params.translation_language)
            print("0) Retrieve")
            choice = input("Your input: ")
            match choice:
                case "1":
                    params.startTime = input("From date(YYYY-MM-DD HH:MM:SS): ")
                    params.validateStartTime()
                    if not params.startTime:
                        print("\033[1mIncorrect date format. Please use date format specified above.\033[0m")
                case "2":
                    params.endTime = input("From date(YYYY-MM-DD HH:MM:SS): ")
                    params.validateEndTime()
                    if not params.endTime:
                        print("\033[1mIncorrect date format. Please use date format specified above.\033[0m")
                case "3":
                    params.limit = input("Records to return: ")
                    params.validateLimit()
                    if not params.limit:
                        print("\033[1mIncorrect limit number. Please set it from a range <1; 100>.\033[0m")
                case "4":
                    params.language = input("Transcription language: ")
                    params.validateLanguage()
                    if not params.language:
                        print("\033[1mLanguage not supported. Use --help flag for list of supported languages.\033[0m")
                case "5":
                    if params.translate:
                        params.translation_language = input("Translation language: ")
                        params.validateTranslationLanguage()
                        if not params.translation_language:
                            print("\033[1mLanguage not supported. Use --help flag for list of supported languages.\033[0m")
                    else:
                        print("\033[1mIncorrect input\033[0m")
                case "0":
                    pass
                case _:
                    print("\033[1mIncorrect input.\033[0m")
        return params


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