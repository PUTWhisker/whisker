from faster_whisper import WhisperModel
from pathlib import Path
from typing import Union
from transcrpitionData import TranscriptionData
import numpy as np
import grpc
import logging
import time
import os
from diarizate import Clip
import torch


class FasterWhisperHandler:

    def __init__(
        self,
        whisperSize:str,
        cuda:str
    ):
        self.whisperSize = whisperSize
        super(FasterWhisperHandler, self).__init__()
        if cuda.lower() == "true":
            if torch.cuda.is_available():
                self.model = WhisperModel(
                    self.whisperSize, device="cuda", compute_type="float32"
                )
                print("Cuda option enabled")
            else:
                print("Could not initiate Faster-Whisper model with cuda.")
        else:
            self.model = WhisperModel(
                self.whisperSize, device="cpu", compute_type="float32", cpu_threads=8
            )
            print("CPU option enabled")
        self.silenceLength = 1.5

    def preprocessStreaming(
        self,
        receivedAudio: bytes,
        data: TranscriptionData,
        context: grpc.ServicerContext,
        webTranscription: bool
    ) -> Union[Path, TranscriptionData]:
        data.appendData(receivedAudio)
        filePath = data.saveFile(webTranscription=webTranscription)
        data.isSilence = data.detectSilence(filePath, self.silenceLength)
        return filePath, data
    

    def preprocessRequest(
        self,
        data: bytes,
        transcriptionData: TranscriptionData,
        context: grpc.ServicerContext,
    ) -> Path:
        transcriptionData.audio = data
        return transcriptionData.saveFile()
    

    def transcribe(
        self,
        audio: Path,
        data: TranscriptionData,
    ) -> str:
        if data.language != "":
            segments, info = self.model.transcribe(
                str(audio), beam_size=3, language=data.language, word_timestamps=True
            )
        else:
            segments, info = self.model.transcribe(str(audio), beam_size=3)
        logging.info(
            "Detected language '%s' with probability %f"
            % (info.language, info.language_probability)
        )
        if data.language == "":
            data.language = info.language
        response = ""
        if data.diarizate:
            response = []
            for segment in segments:
                response.append(Clip(segment.start, segment.end, "", segment.text))
            return response
        for segment in segments:
            logging.info(
                "[%.2fs -> %.2fs] %s" % (segment.start, segment.end, segment.text)
            )
            response += segment.text
        audio.unlink()
        return response
    

    def postprocess(self, data: TranscriptionData) -> TranscriptionData:
        if (
            len(data.transcription[data.curSegment]) >= 3
            and data.transcription[data.curSegment][-3:] == "..."
        ):
            data.transcription[data.curSegment] = data.transcription[data.curSegment][
                0:-3
            ]
        data.incrementData()
        return data
    

    def handleFile(
        self,
        receivedAudio: bytes,
        data: TranscriptionData,
        context: grpc.ServicerContext
    ) -> str:
        filePath = self.preprocessRequest(receivedAudio, data, context)
        result = self.transcribe(
                filePath, data,
            )
        return result, data
    

    def handleRecord(
        self,
        receivedAudio: bytes,
        data: TranscriptionData,
        context: grpc.ServicerContext,
        webTranscription: bool
    ) -> TranscriptionData:
        processedAudio, data = self.preprocessStreaming(receivedAudio, data, context, webTranscription)
        if not data.silenceAudio:
            data.transcription[data.curSegment] = self.transcribe(
                processedAudio, data,
            )
        data = self.postprocess(data)
        return data
