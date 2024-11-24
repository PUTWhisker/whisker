from concurrent import futures
from threading import Thread
from transcrpitionData import TranscriptionData, WrongLanguage
from dotenv import load_dotenv
from translate import Translator
import faster_whisper_model
import grpc
import asyncio
import logging
import os
import diarizate
import concurrent.futures
import sys
import inspect

curDir = os.path.dirname(__file__)
protoDir = os.path.join(curDir, "proto")
sys.path.insert(0, protoDir)

from proto import sound_transfer_pb2_grpc
from proto import sound_transfer_pb2

sys.path.insert(0, curDir)

"""
    Including proto libraries from proto folder. Have to change system directory, as sound_transfer_pb2_grpc imports sound_transfer_pb2 within itself,
    and there will be error as sound_transfer_pb2 is in a different directory than executable file (server.py). Another solution would be changing import line in
    sound_transfer_pb2_grpc, but would have to do that every time proto file is changed
"""

logging.basicConfig(format="%(levelname)s:%(name)s:%(message)s", level=logging.INFO)
_cleanup_coroutines = []  # Needed for asyncio graceful shutdown


def _errorMessages(e: Exception, func: callable):
    if isinstance(e, FileNotFoundError):
        errorCode = grpc.StatusCode.INTERNAL
        errorMessage = "There was an internal error when saving your audio file."
    if isinstance(e, WrongLanguage):
        errorCode = grpc.StatusCode.INVALID_ARGUMENT
        errorMessage = "Supplied translate language is not currently supported."
    else:  # TODO: Debug and change for real error messages
        errorCode = grpc.StatusCode.INTERNAL
        errorMessage = f"An error occured while executing {func} function: {e} <Error type: {type(e)}>"
    return errorCode, errorMessage


def run_transcribe(file_path, data:TranscriptionData):
    model = faster_whisper_model.FasterWhisperHandler(os.getenv("FASTER_WHISPER_MODEL"))
    # return model.transcribe(str(file_path), return_fragments=True)
    return model.transcribe(
        str(file_path), data
    )


class SoundService(sound_transfer_pb2_grpc.SoundServiceServicer):
    def __init__(self):
        self.number = 0
        self.fastModel = faster_whisper_model.FasterWhisperHandler(
            os.getenv("FASTER_WHISPER_MODEL"),
        )
        self.translator = Translator(os.getenv("M2M100_MODEL"))
        try:
            os.mkdir("tempFiles")
        except FileExistsError:
            pass
        except PermissionError:
            logging.error("Permission denied: Unable to create direcotry tempFiles.")
        except Exception as e:
            logging.error(f"An error occurred: {e}")


    def _errorUnaryHandler(func: callable):
        async def wrapper(*args, **kwargs):
            try:
                for arg in args:
                    if type(arg) is grpc._cython.cygrpc._ServicerContext:
                        context = arg
                return await func(*args, **kwargs)
            except Exception as e:
                errorCode, errorMessage = _errorMessages(e, func)
                logging.exception(f"{errorCode}: {errorMessage}")
                await context.abort(errorCode, errorMessage)
                return
        return wrapper
    

    def _errorStreamHandler(func: callable):
        async def wrapper(*args, **kwargs):
            try:
                for arg in args:
                    if type(arg) is grpc._cython.cygrpc._ServicerContext:
                        context = arg
                result = func(*args, **kwargs)

                if inspect.isasyncgen(result):
                    async for res in result:
                        yield res
                return
            except Exception as e:
                errorCode, errorMessage = _errorMessages(e, func)
                logging.exception(f"{errorCode}: {errorMessage}")
                await context.abort(errorCode, errorMessage)
                return
        return wrapper
    
    
    @_errorUnaryHandler
    async def TestConnection(self, request, context):
        return sound_transfer_pb2.TextMessage(text=request.text)
    
    @_errorUnaryHandler
    async def DiarizateSpeakers(self, request, context):
        transcriptionData = TranscriptionData(audio=request.sound_data, diarizate=True)
        file_path = transcriptionData.saveFile()
        out = []
        try:
            with concurrent.futures.ProcessPoolExecutor() as executor:
                futures = {
                    "transcribe": executor.submit(run_transcribe, file_path, transcriptionData),
                    "diarize": executor.submit(
                        diarizate.diarizate_speakers, str(file_path)
                    ),
                }
                concurrent.futures.wait(futures.values())
                out = diarizate.combine(
                    futures["transcribe"].result(), futures["diarize"].result()
                )
        except Exception as e:
            logging.exception(f"An error occurred: {e}")
            out = []
        finally:
            file_path.unlink()
        transcription, speaker = [], []
        for elem in out:
            transcription.append(elem["text"])
            speaker.append(elem["speaker"])
        return sound_transfer_pb2.SpeakerAndLine(
                text=transcription, speakerName=speaker
            )


    @_errorUnaryHandler
    async def SendSoundFile(self, request, context):
        logging.info("Received audio file for transcription.")
        transcriptionData = TranscriptionData()
        try:
            result, _ = await self.fastModel.handleFile(
                request.sound_data, transcriptionData, context
            )
        except Exception as e:
            if (
                transcriptionData.filePath.exists()
            ):  # To ensure tempFile gets deleted even when error occurs
                transcriptionData.filePath.unlink()
            raise e
        return sound_transfer_pb2.SoundResponse(text=result)
    
    
    @_errorStreamHandler
    async def SendSoundFileTranslation(self, request, context):
        logging.info("Received audio file for translation")
        transcriptionData = TranscriptionData()
        transcriptionData.processMetadata(context)
        if transcriptionData.translate is None:
            raise # TODO: Here raise error when not specified to what language text should be translated (Transcription is good cause whisper has autodetect)
        transcription, transcriptionData = await self.fastModel.handleFile(
            request.sound_data, transcriptionData, context
        )
        yield sound_transfer_pb2.SoundStreamResponse(
                text=transcription,
                flags=["transcription",],
            )
        translation = self.translator.translate(
            transcription, transcriptionData.language, transcriptionData.translate
        )[0]
        yield sound_transfer_pb2.SoundStreamResponse(
                text=translation,
                flags=["translation",],
            )


    @_errorStreamHandler  # TODO: Resolve async_generator problem to add errorHandler
    async def StreamSoundFile(self, requestIter, context):
        logging.info("Received record streaming.")
        transcriptionData = TranscriptionData()
        transcriptionData.processMetadata(context)
        async for request in requestIter:
            transcriptionData.isSilence = False
            if transcriptionData.curSeconds >= 10:
                transcriptionData.curSegment += 1
                transcriptionData.curSeconds = 0
            try:
                transcriptionData = await self.fastModel.handleRecord(
                    request.sound_data, transcriptionData, context
                )
                logging.info(transcriptionData.transcription)
            except Exception as e:
                if (
                    transcriptionData.filePath.exists()
                ):  # To ensure tempFile gets deleted even when error occurs
                    transcriptionData.filePath.unlink()
                raise e
            flags = [str(transcriptionData.isSilence)]
            yield sound_transfer_pb2.SoundStreamResponse(
                text=transcriptionData.transcription[transcriptionData.curSegment],
                flags=flags,
            )


async def server():
    load_dotenv()
    port = os.getenv("SERVER_PORT")
    server = grpc.aio.server(
        futures.ThreadPoolExecutor(max_workers=10),
        options=[
            ("grpc.max_send_message_length", os.getenv("MAX_FILE_MB") * 1024 * 1024),  # 50MB
            ("grpc.max_receive_message_length", os.getenv("MAX_FILE_MB") * 1024 * 1024),  # 50MB
        ],
    )
    sound_transfer_pb2_grpc.add_SoundServiceServicer_to_server(SoundService(), server)
    server.add_insecure_port("[::]:" + port)  # change to secure later
    await server.start()
    try:
        logging.info("Server started, listening on " + port)
        await server.wait_for_termination()
    except KeyboardInterrupt:
        await server.stop(5)
        logging.info("Keybourd interruption detected, server closing...")
    await server.wait_for_termination()


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    try:
        loop.run_until_complete(server())
    except KeyboardInterrupt:
        logging.info("Detected keyboard interruption, shutting down...")
    finally:
        loop.run_until_complete(asyncio.gather(*_cleanup_coroutines))
        loop.close()
