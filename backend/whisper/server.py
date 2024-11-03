from concurrent import futures
from threading import Thread
from transcrpitionData import TranscriptionData
import faster_whisper_model
import grpc
import asyncio
import logging
import os
import sys

curDir = os.path.dirname(__file__)
protoDir = os.path.join(curDir, "proto")
sys.path.insert(0, protoDir)

from proto import sound_transfer_pb2_grpc
from proto import sound_transfer_pb2

sys.path.insert(0, curDir)
'''
    Including proto libraries from proto folder. Have to change system directory, as sound_transfer_pb2_grpc imports sound_transfer_pb2 within itself,
    and there will be error as sound_transfer_pb2 is in a different directory than executable file (server.py). Another solution would be changing import line in
    sound_transfer_pb2_grpc, but would have to do that every time proto file is changed
'''

logging.basicConfig(format="%(levelname)s:%(name)s:%(message)s", level=logging.INFO)
_cleanup_coroutines = [] # Needed for asyncio graceful shutdown

class SoundService(sound_transfer_pb2_grpc.SoundServiceServicer):

    def __init__(self):
        self.number = 0
        self.fastModel = faster_whisper_model.FasterWhisperHandler()
        try:
            os.mkdir("tempFiles")
        except FileExistsError:
            pass
        except PermissionError:
            logging.error("Permission denied: Unable to create direcotry tempFiles.")
        except Exception as e:
            logging.error(f"An error occurred: {e}")


    def _errorHandler(func:callable):
        async def wrapper(*args, **kwargs):
            try:
                return await func(*args, **kwargs)
            except FileNotFoundError:
                logging.error("There was an internal error when saving your audio file.")
            except Exception as e:
                logging.error(f'An error occured while executing {func} function: {e} <Error type: {type(e)}>') #TODO: Debug and change for real error messages
        return wrapper


    @_errorHandler
    async def TestConnection(self, request, context):
        return sound_transfer_pb2.TextMessage(text=request.text)
    
    
    @_errorHandler
    async def SendSoundFile(self, request, context):
        logging.info("Received audio file.")
        transcriptionData = TranscriptionData()
        try:
            result = await self.fastModel.handleFile(request.sound_data, transcriptionData, context)
        except Exception as e:
            if transcriptionData.filePath.exists(): # To ensure tempFile gets deleted even when error occurs
                transcriptionData.filePath.unlink()
            raise e
        return sound_transfer_pb2.SoundResponse(text=result)


    # @_errorHandler    #TODO: Resolve async_generator problem to add errorHandler
    def StreamSoundFile(self, requestIter, context):
        logging.info("Received record streaming.")
        def parse_request():
            transcriptionData = TranscriptionData()
            for key, value in context.invocation_metadata():
                logging.info(f'{key} : {value}')
            transcriptionData.processMetadata(context)
            for request in requestIter:
                transcriptionData.isSilence = False
                if transcriptionData.curSeconds >= 10:
                    transcriptionData.curSegment += 1
                    transcriptionData.curSeconds = 0
                try:
                    transcriptionData = self.fastModel.handleRecord(request.sound_data, transcriptionData, context)
                    logging.info(transcriptionData.transcription)
                except Exception as e:
                    if transcriptionData.filePath.exists(): # To ensure tempFile gets deleted even when error occurs
                        transcriptionData.filePath.unlink()
                    raise e
                flags=[str(transcriptionData.isSilence)]
                # print(f'{transcriptionData.isSilence} : {transcriptionData.curSeconds}')
                yield sound_transfer_pb2.SoundStreamResponse(
                    text=transcriptionData.transcription[transcriptionData.curSegment],
                    flags=flags
                )
        return parse_request()


async def server():
    port = "7070"
    server = grpc.aio.server(
    futures.ThreadPoolExecutor(max_workers=10),
    options=[
        ('grpc.max_send_message_length', 50 * 1024 * 1024),  # 50MB
        ('grpc.max_receive_message_length', 50 * 1024 * 1024)  # 50MB
    ]
)
    sound_transfer_pb2_grpc.add_SoundServiceServicer_to_server(SoundService(), server)
    server.add_insecure_port("[::]:" + port) # change to secure later
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