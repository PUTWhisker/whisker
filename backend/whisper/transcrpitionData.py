from pathlib import Path
from pydub import AudioSegment, silence

import uuid
import wave
import pyaudio
import grpc
import logging
import languages


class WrongLanguage(Exception):
    pass

class TranscriptionData():

    def __init__(self,
                 transcription:list=[""],
                 audio:bytes=b'',
                 previousAudio:bytes=b'',
                 curSegment:int=0,
                 curSeconds:int=0,
                 silenceAudio:bool=False,
                 language:str="",
                 translate:str="",
                 diarizate:bool=False
                 ):
        # TODO: validate source and translate language
        self.transcription = transcription
        self.audio = audio
        self.previousAudio = previousAudio
        self.curSegment = curSegment
        self.curSeconds = curSeconds
        self.silenceAudio = silenceAudio
        self.filePath = Path(f"./tempFiles/{uuid.uuid4()}")
        self.language = self.validateLanguage(language)
        if self.language is None:
            raise WrongLanguage(
                    "Given language is not supported for translation."
                )
        self.translate = self.validateLanguage(translate)
        if self.translate is None:
            raise WrongLanguage(
                    "Given language is not supported for translation."
                )
        self.diarizate = diarizate
        self.sessionId = None

    def appendData(self, receivedAudio: bytes):
        if self.curSeconds < 10:
            self.audio = self.previousAudio + receivedAudio
        self.previousAudio = self.audio

    def _saveAudioFile(self, fileName: Path, data: bytes, webTranscription: bool):
        p = pyaudio.PyAudio()
        if not webTranscription:
            sampleWidth = p.get_sample_size(pyaudio.paInt16)
        else:
            sampleWidth = p.get_sample_size(pyaudio.paUInt8)
        output = wave.open(str(fileName), "wb")
        output.setnchannels(1)
        output.setsampwidth(sampleWidth)
        output.setframerate(44100)
        output.writeframes(b"".join([data]))
        output.close()

    def saveFile(self, save_as_wav=True, webTranscription=False) -> Path:
        if save_as_wav:
            self.filePath = self.filePath.with_suffix(".wav")
            self._saveAudioFile(self.filePath, self.audio, webTranscription)
        else:
            with self.filePath.open("wb") as file:
                file.write(self.audio)
        return self.filePath

    def detectSilence(self, path: Path, silenceLength: int) -> bool:
        silenceAudio = AudioSegment.from_wav(path)
        detectedSilence = silence.detect_silence(
            silenceAudio, min_silence_len=1000, silence_thresh=-45
        )
        detectedSilence = [
            ((start / 1000), (stop / 1000)) for start, stop in detectedSilence
        ]  # convert to sec
        for start, stop in detectedSilence:
            if stop - start > silenceLength:
                logging.info("Silence Detected!!!")
                return True
        return False


    def processMetadata(self, context: grpc.ServicerContext):
        for key, value in context.invocation_metadata():
            if key == 'source_language':
                language = self.validateLanguage(value)
                if language is None:
                    raise WrongLanguage(
                        "Given language is not supported for translation."
                    )
                self.language = language
                break
            elif key == 'session_id':
                self.sessionId = value
    

    def validateLanguage(self, language: str) -> bool:
        for languageKey, languageValue in languages.LANGUAGES.items():
            if languageKey == language or languageValue == language:
                return languageValue
        if (
            language != ""
        ):  # Raise error if chosen language is not in M2M100 available language list
            return None
        return ""


    def incrementData(self):
        self.curSeconds += 2
        if self.isSilence or self.curSeconds >= 10:
            self.isSilence = True
        if self.isSilence:
            self.previousAudio = b""
            self.transcription.append("")
