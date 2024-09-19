from concurrent import futures
from time import sleep
from threading import Thread

import sound_transfer_pb2_grpc as Services
import sound_transfer_pb2 as Variables
import grpc

# declare Keepalive pings


class SoundService(Services.SoundServiceServicer):

    def __init__(self):
        self.number = 0

    def TestConnection(self, request, context):
        print(request.text)
        sleep(0.9)
        return Variables.TextMessage(text=request.text)
    
    
    def SendSoundFile(self, request, context):
        print("Received audio file.")
        print(type(request.sound_data)) # <- received audio file
        print(request.flags) # <- received flags
        return Variables.SoundResponse(text="Here is a translated file script")


    def StreamSoundFile(self, requestIter, context):
        def parse_request():
            for request in requestIter:

                print(f'Received from client number: {request.sound_data} and flags: {request.flags}')

                
                self.number += 1

                
                yield Variables.SoundResponse(
                    text=str(self.number)
                )

        return parse_request()


def server():
    port = "50051"
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    Services.add_SoundServiceServicer_to_server(SoundService(), server)
    server.add_insecure_port("[::]:" + port) # change to secure later
    server.start()
    print("Server started, listening on " + port)
    server.wait_for_termination()

if __name__ == "__main__":
    server()