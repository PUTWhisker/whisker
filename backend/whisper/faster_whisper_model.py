from faster_whisper import WhisperModel
from pathlib import Path
import uuid
import wave
import pyaudio
import os

model_size = "small"

class FasterWhisperHandler():
    
    def __init__(
        self, 
        language='en',
        task='transcribe'
    ):
        self.model = WhisperModel(model_size, device="cpu", compute_type="float32")
        self.language = language
        self.task = task


    def _saveFile(self, fileName, data):
        p = pyaudio.PyAudio()
        sampleWidth = p.get_sample_size(pyaudio.paInt16)
        output = wave.open(str(fileName), 'wb')
        output.setnchannels(1)
        output.setsampwidth(sampleWidth)
        output.setframerate(44100)
        output.writeframes(b''.join([data]))
        output.close()
        return fileName


    def preprocess(self, data, record):
        path = ""
        if record:
            temp_file = Path(f'./tempFiles/{uuid.uuid4()}.wav')
            with open(temp_file, "wb") as file:
                file.write(data)
            path = self._saveFile(temp_file, data)
        else:
            path = Path(f'./tempFiles/{uuid.uuid4()}')
            with open(str(path), "wb") as file:
                file.write(data)
        return path
    

    def transcribe(self, data, previous=""):
        print(f'Zapisane: {previous}')
        segments, info = self.model.transcribe(str(data), beam_size=5, language='pl', initial_prompt=previous)
        print("Detected language '%s' with probability %f" % (info.language, info.language_probability))

        response = ""
        for segment in segments:
            print("[%.2fs -> %.2fs] %s" % (segment.start, segment.end, segment.text))
            response += segment.text
        data.unlink()
        return response
    

    def postprocess(self, data, prefix):
        if (len(data) >= 3 and data[-3:] == '...'):
            data = data[0:-3]
        print(f'Po: {data}')
        prefix += data
        return data, prefix


    async def handleFile(self, data, context, record):
        text = self.preprocess(data, record)
        result = self.transcribe(text)
        return result
    

    def handleRecord(self, data, context, previous, record):
        text = self.preprocess(data, record)
        transcripted = self.transcribe(text, previous)
        result, previous = self.postprocess(transcripted, previous) 
        return result, previous