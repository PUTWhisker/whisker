
import pyaudio
import asyncio
import logging
import wave
import os
import sys

curDir = os.path.dirname(__file__)
protoDir = os.path.join(curDir, "proto/sound_transfer")
sys.path.insert(0, protoDir)

from proto.sound_transfer import sound_transfer_pb2 as Variables

sys.path.insert(0, curDir)


class AudioRecorder():

    def __init__(self, save:str):
        self.chunk = 1024
        self.sampleFormat = pyaudio.paInt16
        self.channels = 1 # Unsure if this can stay as 1, need testing
        self.fs = 44100
        self.save = save
        self.probeTime = 2 # frequency of yielding audio (seconds)
        self.frames = []
        self.data = []


    async def _startRecording(self):
        try:
            p = pyaudio.PyAudio()
            stream = p.open(format=self.sampleFormat,
                channels=self.channels,
                rate=self.fs,
                frames_per_buffer=self.chunk,
                input=True)
            self.sampleSize = p.get_sample_size(self.sampleFormat)
            while(True): # Recording audio from user's input device
                data = stream.read(self.chunk)
                self.frames.append(data)
                await asyncio.sleep(0) # Check if while in record is ready to execute (probeTime has passed)

        except KeyboardInterrupt:
            # logging.info("Detected interruption, ending recording.") TODO: Figure out how to print it nicely
            print()
            stream.stop_stream()
            stream.close()
            p.terminate()


    def _saveFile(self, sampleWidth: int):
        if not self.save.endswith('.wav'):
            self.save += '.wav'
        output = wave.open(self.save, 'wb')
        output.setnchannels(self.channels)
        output.setsampwidth(sampleWidth)
        output.setframerate(self.fs)
        output.writeframes(b''.join(self.data))
        output.close()


    async def record(self):
        try:
            recordTask = asyncio.create_task(self._startRecording()) # Initiate recording async
            while not recordTask.done():
                await asyncio.sleep(self.probeTime) # Await for set in constructor time
                yield Variables.SoundRequest( # After probeTime seconds send 
                    sound_data=b''.join(self.frames),
                    flags=["a","b"] # TODO: resolve flags problem
                )
                if self.save is not None: # Saving bytes if --save flag is raised
                    self.data += self.frames
                self.frames = []

            if self.save != "": # Saving recording if --save flag is raised
                self._saveFile(self.sampleSize)

        except Exception as e:
            logging.error(f'Problem occured while recording audio: {e}')
        finally:
            yield Variables.SoundRequest( # Send recorded data after keyboard interruption
                    sound_data=b''.join(self.frames),
                    flags=["a","b"]
                ) 


