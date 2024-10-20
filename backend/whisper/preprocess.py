from pathlib import Path
from pydub import AudioSegment, silence
import uuid
import wave
import pyaudio

def _saveAudioFile(fileName, data):
    p = pyaudio.PyAudio()
    sampleWidth = p.get_sample_size(pyaudio.paInt16)
    output = wave.open(str(fileName), 'wb')
    output.setnchannels(1)
    output.setsampwidth(sampleWidth)
    output.setframerate(44100)
    output.writeframes(b''.join([data]))
    output.close()
    return fileName


def saveFile(data, record):
    path = ""
    if record:
        temp_file = Path(f'./tempFiles/{uuid.uuid4()}.wav')
        with open(temp_file, "wb") as file:
            file.write(data)
        path = _saveAudioFile(temp_file, data)
    else:
        path = Path(f'./tempFiles/{uuid.uuid4()}')
        with open(str(path), "wb") as file:
            file.write(data)
    return path


def detectSilence(path, silenceLength):
    isSilence = False
    silenceAudio = AudioSegment.from_wav(path)
    detectedSilence = silence.detect_silence(silenceAudio, min_silence_len=1000, silence_thresh=-45)
    detectedSilence = [((start/1000),(stop/1000)) for start,stop in detectedSilence] #convert to sec
    for start,stop in detectedSilence:
        if stop - start > silenceLength:
            isSilence = True
            print("Silence Detected!!!")
            #TODO: check only the last one
    return isSilence
        