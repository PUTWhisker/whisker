from console import ConsolePrinter

import dicts
import argparse
import sys
import asyncio
import os.path


# FLAGS: language, modelSize, host, port, audioFile, record, saving

# Read user's input flags and arguments
def parse() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
            prog='Whisper Wrapper',
            description='Simple speach-to-text console application',
            epilog='test test test')

    parser.add_argument(
            '--record',
            action='store_true',
            help="Set true to record audio from microphone, false to input audio file")
    
    parser.add_argument(
            '--local',
            action='store_true',
            help="Use this flag to use local Whisper server")
    
    parser.add_argument(
            '--language',
            type=str,
            default="English",
            choices=sorted(list(dicts.LANGUAGES)) + sorted(list(dicts.LANGUAGES.values())),
            help="Set it to the audio language")
    
    parser.add_argument(
            '--model',
            type=str,
            default='small',
            choices=dicts.MODELS,
            help="Set it to the desired Whisper model")
    
    parser.add_argument(
            '--save',
            type=str,
            default="",
            metavar="file_path",
            help="Use this flag to save recorded file on your machine")

    parser.add_argument(
            '--port',
            type=str,
            default=50051,
            metavar="",
            help="Set it to the server's listening port number (only viable when using --local flag)")
    
    parser.add_argument(
        'fileName',
        nargs='?',
        default=None,
        help='File to be transcripted')
    
    parser.add_argument(
        '--version', '-v',
        action="version",
        version='%(prog)s - Version 0.1')
    
    return parser


async def main(parser: argparse.ArgumentParser):
    args = parser.parse_args()

    #read server's details
    if (args.local):
        print("local option")
        # host = "host.docker.internal"
        host = "127.0.0.1"
        port = args.port
    else:
        print("server option")
        host = "100.80.80.156" # Here insert pp server address
        port = args.port

    # Check if file exists if it was passed as an argument
    if (args.fileName != None):
        if (not os.path.isfile(args.fileName)):
            print("Incorrect file name.")
            return

    # Innitiate connection with the server
    console = ConsolePrinter(host, port, args.language, args.model, args.save)
    if not await console.startApp():
       return

    if (args.fileName != None): # If there is a valid audio file as an argument, initiate SendSoundFile method
        with open(args.fileName, 'rb') as file:
            audio = file.read() # read audio as bytes
        await console.sendFile(audio)
    elif (args.record): # If there is a record flag, initiate StreamSoundFile method (app can't do both, record and translate file)
        await console.record()
    else: # No action specified by the user
        return



if __name__ == "__main__":
    parser = parse()
    asyncio.run(main(parser))
    
    



