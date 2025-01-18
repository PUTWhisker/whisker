from console import ConsolePrinter, LoginFailure
from dotenv import load_dotenv, set_key

import dicts
import argparse
import sys
import asyncio
import os.path
import logging

_cleanup_coroutines = []  # Needed for asyncio graceful shutdown


# Read user's input flags and arguments
def parse() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog="Whisper Wrapper", description="Simple speech-to-text console application"
    )

    parser.add_argument(
        "--record",
        action="store_true",
        help="Set true to record audio from microphone, false to input audio file",
    )

    parser.add_argument(
        "--local", action="store_true", help="Use this flag to use local Whisper server"
    )

    parser.add_argument(
        "--language",
        type=str,
        default=None,
        choices=sorted(list(dicts.LANGUAGES)) + sorted(list(dicts.LANGUAGES.values())),
        help="Set it to the audio language",
    )

    parser.add_argument(
        "--save",
        type=str,
        default="",
        metavar="file_path",
        help="Use this flag to save recorded file on your machine",
    )

    parser.add_argument(
        "--host",
        type=str,
        default="127.0.0.1",
        metavar="APIserver IP",
        help="Set it to the server's IP address (only available when using --local flag)",
    )

    parser.add_argument(
        "--port",
        type=str,
        default=50051,
        metavar="",
        help="Set it to the server's listening port number (only available when using --local flag)",
    )

    parser.add_argument(
        "--trans",
        type=str,
        default=None,
        help="Use this flag to enable translation and set translation language",
    )

    parser.add_argument(
        "--diarization",
        action="store_true",
        help="Use this flag to enable speaker diarization",
    )

    parser.add_argument(
        "--username",
        type=str,
        default=None,
        help="Use this when registering or with your first logging"
    )

    parser.add_argument(
        "--logout",
        action="store_true",
        help="Use this flag to logout of your account (clear tokens)"
    )

    parser.add_argument(
        "--register",
        action="store_true",
        help="Use this flag when creating a new account"
    )

    parser.add_argument(
        "--retrieve", "-r",
        action="store_true",
        help="Set this flag to retrieve transcription history from your account"
    )

    parser.add_argument(
        "fileName", nargs="?", default=None, help="File to be transcribed"
    )

    parser.add_argument(
        "--version", "-v", action="version", version="%(prog)s - Version 0.1"
    )

    return parser


async def handleException(e: Exception):
    if isinstance(e, LoginFailure):
        print("Error occurred when logging into your account.")
    else:
        print(f"An exception occurred: {type(e)}: {e}")
    pass


async def main(parser: argparse.ArgumentParser):
    args = parser.parse_args()
    env = '.env'
    load_dotenv(env)
    # read server's details
    if args.local:
        logging.info("local option")
        host = args.host
        port = args.port
    else:
        logging.info("server option")
        host = "100.80.80.156"  # Here insert pp server address
        port = args.port

    # Check if file exists if it was passed as an argument
    if args.fileName is not None:
        if not os.path.isfile(args.fileName):
            logging.error("Incorrect file name.")
            return

    console = ConsolePrinter(
        host, port, args.language, args.save, args.trans, ".env"
    )
    if args.logout:
        set_key(env, "JWT_TOKEN", "")
        set_key(env, "REFRESH_TOKEN", "")
        print("Successfully logout!")
        return
    elif not await console.startApp():
        return
    
    if args.register:
        await console.register()
    elif args.retrieve:
        # if token != "":
            await console.retrieveHistory()
        # else:
        #     print("Login into your account first!")

    elif args.username is not None:
        await console.login(args.username)
    elif (
        args.fileName is not None
    ):  # If there is a valid audio file as an argument, initiate SendSoundFile method
        with open(args.fileName, "rb") as file:
            audio = file.read()  # read audio as bytes
        if args.diarization:
            await console.speakersDiarization(audio)
        elif args.trans:
            await console.sendFileTranslation(audio)
        else:
            await console.sendFile(audio)
    elif args.record:  # If there is a record flag, initiate StreamSoundFile method (app can't do both, record and translate file)
        await console.record()
    else:  # No action specified by the user
        print(
            "You have to specify an action (supply an audio file/initiate recording/register/retrieve transcripts)."
        )
    return


if __name__ == "__main__":
    parser = parse()
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    try:
        loop.run_until_complete(main(parser))
    except KeyboardInterrupt:
        logging.info("Detected keyboard interruption, shutting down...")
    finally:
        loop.run_until_complete(asyncio.gather(*_cleanup_coroutines))
        loop.close()
