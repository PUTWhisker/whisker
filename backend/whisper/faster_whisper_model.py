from faster_whisper import WhisperModel
from pathlib import Path
from typing import Union
from transcrpitionData import TranscriptionData
from translate import Translator
import grpc
import logging
import time
from diarizate import Clip


model_size = "tiny"


class FasterWhisperHandler:
    model = None

    def __init__(
        self,
    ):
        super(FasterWhisperHandler, self).__init__()
        self.model = WhisperModel(
            model_size, device="cpu", compute_type="float32", cpu_threads=8
        )
        self.silenceLength = 1.5
        self.translator = Translator()

    def preprocessStreaming(
        self,
        receivedAudio: bytes,
        data: TranscriptionData,
        context: grpc.ServicerContext,
    ) -> Union[Path, TranscriptionData]:
        data.appendData(receivedAudio)
        filePath = data.saveFile()
        data.isSilence = data.detectSilence(filePath, self.silenceLength)
        return filePath, data

    def preprocessRequest(
        self,
        data: bytes,
        transcriptionData: TranscriptionData,
        context: grpc.ServicerContext,
    ) -> Path:
        transcriptionData.processMetadata(context)
        transcriptionData.audio = data
        return transcriptionData.saveFile()

    def transcribe(
        self,
        data: Path,
        language: str,
        translationLanguage: str,
        return_fragments=False,
    ) -> str:
        startTime = time.time()
        if language != "":
            segments, info = self.model.transcribe(
                str(data), beam_size=3, language=language, word_timestamps=True
            )
        else:
            segments, info = self.model.transcribe(str(data), beam_size=3)
        logging.info(
            "Detected language '%s' with probability %f"
            % (info.language, info.language_probability)
        )
        if language == "":
            language = info.language
        response = ""
        if return_fragments:
            response = []
            for segment in segments:
                response.append(Clip(segment.start, segment.end, "", segment.text))
            return response
        for segment in segments:
            logging.info(
                "[%.2fs -> %.2fs] %s" % (segment.start, segment.end, segment.text)
            )
            response += segment.text
        logging.info(f"Whsiper transcribing finished in: {time.time() - startTime}")
        data.unlink()  # TODO: Instead of creating and deleting file all the time, just do it on one and delete it after all translations
        if translationLanguage:
            response = self.translator.translate(
                response, language, translationLanguage
            )[0]
            logging.info(f"Translating finished in: {time.time() - startTime}")
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

    async def handleFile(
        self,
        receivedAudio: bytes,
        data: TranscriptionData,
        context: grpc.ServicerContext,
        diarizate_speakers=False,
    ) -> str:
        filePath = self.preprocessRequest(receivedAudio, data, context)
        if diarizate_speakers:
            result = self.transcribe(
                filePath, data.language, data.translate, return_fragments=True
            )
        else:
            result = self.transcribe(
                filePath, data.language, data.translate, return_fragments=False
            )
        return result

    async def handleRecord(
        self,
        receivedAudio: bytes,
        data: TranscriptionData,
        context: grpc.ServicerContext,
    ) -> TranscriptionData:
        processedAudio, data = self.preprocessStreaming(receivedAudio, data, context)
        if not data.silenceAudio:
            data.transcription[data.curSegment] = self.transcribe(
                processedAudio, data.language, data.translate
            )
        data = self.postprocess(data)
        return data
