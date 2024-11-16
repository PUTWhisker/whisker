from pyannote.audio import Pipeline


import os

pipeline = Pipeline.from_pretrained(
    "pyannote/speaker-diarization-3.1",
    use_auth_token=os.environ.get("HUGGING_FACE_API_KEY"),
)


class Clip:
    def __init__(self, start: float, end: float, speaker: str, text: str):
        self.start = start
        self.end = end
        self.speaker = speaker
        self.text = text

    def __str__(self):
        return f"{self.start} - {self.end}, {self.speaker}: {self.text}"


def diarizate_speakers(path: str) -> list[Clip]:
    diarization = pipeline(path)
    out = []
    current_speaker = None
    for turn, _, speaker in diarization.itertracks(yield_label=True):
        if current_speaker != speaker:
            out.append(Clip(turn.start, turn.end, speaker, ""))
            current_speaker = speaker
        else:
            out[-1].end = turn.end
    if out:
        out[0].start = 0
    return out


def preprocess_segments(transcription_segments: list[Clip], diarization: list[Clip]):
    transcription_segments[0].start = 0
    diarization[0].start = 0
    if transcription_segments[-1].end > diarization[-1].end:
        diarization[-1].end = transcription_segments[-1].end
    else:
        transcription_segments[-1].end = diarization[-1].end


def count_coverage(line: Clip, speaker: Clip) -> float:
    line_lenght = line.end - line.start
    if line.start >= speaker.start and line.end <= speaker.end:
        return 1
    if line.end > speaker.start and line.end < speaker.end:
        return (line.end - speaker.start) / line_lenght
    if line.start < speaker.start and line.end > speaker.end:
        return (speaker.end - speaker.start) / line_lenght
    if line.start > speaker.start and line.start < speaker.end:
        return (speaker.end - line.start) / line_lenght
    return 0


def combine(transcription: list[Clip], diarization: list[Clip]) -> dict:
    out = []
    i, j = 0, 0
    while i < len(diarization) and j < len(transcription):
        max_coverage = count_coverage(transcription[j], diarization[i])
        if max_coverage != 1:
            max_diarization_fragment = i
            for p in range(i, len(diarization)):
                if diarization[p].start > transcription[j].end:
                    break
                coverage = count_coverage(transcription[j], diarization[p])
                if coverage > max_coverage:
                    max_coverage = coverage
                    max_diarization_fragment = p
            i = max_diarization_fragment

        out.append({"text": transcription[j].text, "speaker": diarization[i].speaker})
        j += 1

    return out
