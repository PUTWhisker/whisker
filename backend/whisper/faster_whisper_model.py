from faster_whisper import WhisperModel
from pathlib import Path
from typing import Union
from transcrpitionData import TranscriptionData
import grpc
import logging


model_size = "tiny"

class FasterWhisperHandler():
    
    def __init__(
        self, 
        transcrpition:TranscriptionData=TranscriptionData()
    ):
        super(FasterWhisperHandler, self).__init__()
        self.model = WhisperModel(model_size, device="cpu", compute_type="float32", cpu_threads=8)
        self.transcrpition = transcrpition
        self.silenceLength = 1.5
    

    def preprocessStreaming(self, receivedAudio:bytes, data:TranscriptionData, context:grpc.ServicerContext) -> Union[Path, TranscriptionData]:
        data.appendData(receivedAudio)
        filePath = data.saveFile()
        data.isSilence = data.detectSilence(filePath, self.silenceLength)
        return filePath, data
    

    def preprocessRequest(self, data:bytes, transcriptionData:TranscriptionData, context:grpc.ServicerContext) -> Path:
        transcriptionData.processMetadata(context)
        transcriptionData.audio = data
        print(transcriptionData.language)
        return transcriptionData.saveFile()
    

    def transcribe(self, data:Path, language:str, translation:bool) ->  str:
        logging.info(data)
        if translation:
            logging.info("Translating audio.")
            segments, info = self.model.transcribe(str(data), beam_size=3, language=language, task='translate') 
        else:
            logging.info("Transcribing audio")
            segments, info = self.model.transcribe(str(data), beam_size=3, language=language)
        logging.info("Detected language '%s' with probability %f" % (info.language, info.language_probability))

        response = ""
        for segment in segments:
            logging.info("[%.2fs -> %.2fs] %s" % (segment.start, segment.end, segment.text))
            response += segment.text
        data.unlink() # TODO: Instead of creating and deleting file all the time, just do it on one and delete it after all translations
        return response
    

    def postprocess(self, data:TranscriptionData) -> TranscriptionData:
        if (len(data.transcription[data.curSegment]) >= 3 and data.transcription[data.curSegment][-3:] == '...'):
            data.transcription[data.curSegment] = data.transcription[data.curSegment][0:-3]
        data.incrementData()
        return data


    async def handleFile(self, receivedAudio:bytes, data:TranscriptionData, context:grpc.ServicerContext) -> str:
        filePath = self.preprocessRequest(receivedAudio, data, context)
        result = self.transcribe(filePath, data.language, data.translate)
        return result
    

    def handleRecord(self, receivedAudio:bytes, data:TranscriptionData, context:grpc.ServicerContext) -> TranscriptionData:
        processedAudio, data = self.preprocessStreaming(receivedAudio, data, context)
        if not data.silenceAudio:
            data.transcription[data.curSegment] = self.transcribe(processedAudio, data.language, data.translate)
        data = self.postprocess(data)
        return data