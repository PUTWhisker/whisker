from concurrent import futures
from time import sleep
from threading import Thread

import sound_transfer_pb2_grpc as Services
import sound_transfer_pb2 as Variables
import faster_whisper_model
import grpc
import asyncio
import logging
import os


_cleanup_coroutines = []

class SoundService(Services.SoundServiceServicer):

    def __init__(self):
        self.number = 0
        self.fastModel = faster_whisper_model.FasterWhisperHandler()
        try:
            os.mkdir("tempFiles")
        except FileExistsError:
            pass
        except PermissionError:
            print("Permission denied: Unable to create direcotry tempFiles.")
        except Exception as e:
            print(f"An error occurred: {e}")


    def TestConnection(self, request, context):
        return Variables.TextMessage(text=request.text)
    
    
    async def SendSoundFile(self, request, context):
        print("Received audio file.")
        print(type(request.sound_data)) # <- received audio file
        print(request.flags) # <- received flags
        result = await self.fastModel.handleFile(request.sound_data, context, False)
        return Variables.SoundResponse(text=result)


    def StreamSoundFile(self, requestIter, context):
        logging.info("Received record streaming.")
        def parse_request():
            transcription, previousAudio, segment, seconds = [""], b'', 0, 0
            for request in requestIter:
                try:
                    newLine = False
                    if seconds >= 10:
                        segment += 1
                        seconds = 0
                    result, transcription, previousAudio, segment, newLine, seconds = self.fastModel.handleRecord(request.sound_data, transcription, previousAudio, segment, seconds, True)
                except Exception as e:
                    logging.error(f'Exception caught: {e}')
                flags=[str(newLine)]
                # print(flags)
                print("About to send data")
                print(result)
                print(transcription[segment])
                yield Variables.SoundStreamResponse(
                    text=transcription[segment],
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
    Services.add_SoundServiceServicer_to_server(SoundService(), server)
    server.add_insecure_port("[::]:" + port) # change to secure later
    await server.start()
    try:
        print("Server started, listening on " + port)
        await server.wait_for_termination()
    except KeyboardInterrupt:
        await server.stop(5)
        print("Keybourd interruption detected, server closing...")
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