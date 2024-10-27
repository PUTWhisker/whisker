import unittest
from diarizate import combine
from diarizate import Clip


print("start testing")


class TestSum(unittest.TestCase):
    def test_two_speakers(self):
        diarization_input = [
            Clip(0, 7.40534375, "SPEAKER_01", ""),
            Clip(8.384093750000002, 15.657218750000002, "SPEAKER_00", ""),
        ]
        transcription_input = [
            Clip(
                1.9800000000000004,
                7.52,
                "",
                "Hi, jak się masz, co robiłeś wczoraj w nocy, kiedy nie było mnie w domu?",
            ),
            Clip(
                8.7,
                15.46,
                "",
                "Nie było mnie w domu, bo musiałem pójspać, a teraz idź do łóżka i nie przeszkadza imię.",
            ),
        ]
        expected_output = [
            {
                "text": "Hi, jak się masz, co robiłeś wczoraj w nocy, kiedy nie było mnie w domu?",
                "speaker": "SPEAKER_01",
            },
            {
                "text": "Nie było mnie w domu, bo musiałem pójspać, a teraz idź do łóżka i nie przeszkadza imię.",
                "speaker": "SPEAKER_00",
            },
        ]

        out = combine(transcription_input, diarization_input)

        self.assertEqual(expected_output, out)

    def test_longer_dialogue(self):
        diarization_input = [
            Clip(0, 3.49034375, "SPEAKER_00", ""),
            Clip(2.9672187500000002, 3.22034375, "SPEAKER_03", ""),
            Clip(3.52409375, 13.46346875, "SPEAKER_00", ""),
            Clip(13.665968750000001, 14.52659375, "SPEAKER_01", ""),
            Clip(14.762843750000002, 16.70346875, "SPEAKER_00", ""),
            Clip(16.70346875, 16.82159375, "SPEAKER_03", ""),
            Clip(16.82159375, 16.83846875, "SPEAKER_00", ""),
            Clip(16.83846875, 16.85534375, "SPEAKER_03", ""),
            Clip(16.85534375, 16.872218750000002, "SPEAKER_00", ""),
            Clip(16.872218750000002, 16.95659375, "SPEAKER_03", ""),
            Clip(16.95659375, 17.20971875, "SPEAKER_01", ""),
            Clip(17.20971875, 17.24346875, "SPEAKER_03", ""),
            Clip(17.24346875, 17.31096875, "SPEAKER_01", ""),
            Clip(17.31096875, 21.192218750000002, "SPEAKER_00", ""),
            Clip(21.47909375, 23.38596875, "SPEAKER_03", ""),
            Clip(24.246593750000002, 25.714718750000003, "SPEAKER_00", ""),
            Clip(27.149093750000002, 27.21659375, "SPEAKER_03", ""),
            Clip(27.21659375, 27.28409375, "SPEAKER_00", ""),
            Clip(27.21659375, 27.31784375, "SPEAKER_02", ""),
            Clip(27.28409375, 27.97596875, "SPEAKER_03", ""),
            Clip(27.97596875, 39.180968750000005, "SPEAKER_02", ""),
        ]
        transcription_input = [
            Clip(1.1399999999999986, 3.46, "", "Dobra, to jestem reyentem, tak?"),
            Clip(
                3.96,
                10.28,
                "",
                "My strunie mówcie śmiało, opiszemy sprawę całą, na te ciężkie nasze czasy, boskim darę takie basy.",
            ),
            Clip(
                10.42, 13.7, "", "Każdy kuak z pieniążymy, że was by to wszyscy wiemy."
            ),
            Clip(14.0, 14.6, "", "Niekoniecznie."),
            Clip(14.9, 16.34, "", "Bili przedsięwój myj struniu."),
            Clip(16.5, 17.36, "", " Nie wyraźnie."),
            Clip(
                17.72, 21.18, "", "Czego jeszcze wam nie stało, bo mechano dosie razie?"
            ),
            Clip(21.6, 23.64, "", "Od sztulknię to tam coś mało."),
            Clip(24.24, 26.34, "", "Ktoś tam za to skaryc ze chcę?"),
            Clip(27.54, 29.34, "", "Lecz ktoś szuka, to nie że chce."),
            Clip(30.34, 30.5, "", "Dobra."),
            Clip(31.14, 31.5, "", "Tak, tak, tak."),
            Clip(31.5, 32.94, "", "To byłabym do miała micia, no nie ważne."),
            Clip(33.92, 34.86, "", "Ha, zapewnę."),
            Clip(35.08, 35.86, "", "A więc bień?"),
            Clip(
                36.78,
                39.180968750000005,
                "",
                "Komuki je, porachujam kościy w drzbiecie.",
            ),
        ]
        expected_output = [
            {"text": "Dobra, to jestem reyentem, tak?", "speaker": "SPEAKER_00"},
            {
                "text": "My strunie mówcie śmiało, opiszemy sprawę całą, na te ciężkie nasze czasy, boskim darę takie basy.",
                "speaker": "SPEAKER_00",
            },
            {
                "text": "Każdy kuak z pieniążymy, że was by to wszyscy wiemy.",
                "speaker": "SPEAKER_00",
            },
            {"text": "Niekoniecznie.", "speaker": "SPEAKER_01"},
            {"text": "Bili przedsięwój myj struniu.", "speaker": "SPEAKER_00"},
            {"text": " Nie wyraźnie.", "speaker": "SPEAKER_01"},
            {
                "text": "Czego jeszcze wam nie stało, bo mechano dosie razie?",
                "speaker": "SPEAKER_00",
            },
            {"text": "Od sztulknię to tam coś mało.", "speaker": "SPEAKER_03"},
            {"text": "Ktoś tam za to skaryc ze chcę?", "speaker": "SPEAKER_00"},
            {"text": "Lecz ktoś szuka, to nie że chce.", "speaker": "SPEAKER_02"},
            {"text": "Dobra.", "speaker": "SPEAKER_02"},
            {"text": "Tak, tak, tak.", "speaker": "SPEAKER_02"},
            {
                "text": "To byłabym do miała micia, no nie ważne.",
                "speaker": "SPEAKER_02",
            },
            {"text": "Ha, zapewnę.", "speaker": "SPEAKER_02"},
            {"text": "A więc bień?", "speaker": "SPEAKER_02"},
            {
                "text": "Komuki je, porachujam kościy w drzbiecie.",
                "speaker": "SPEAKER_02",
            },
        ]
        out = combine(transcription_input, diarization_input)
        # print(out)
        self.assertEqual(out, expected_output)


if __name__ == "__main__":
    unittest.main()


[
    {
        "start": 1.9800000000000004,
        "stop": 7.52,
        "text": "Hi, jak się masz, co robiłeś wczoraj w nocy, kiedy nie było mnie w domu?",
    },
    {
        "start": 8.7,
        "stop": 15.46,
        "text": " Nie było mnie w domu, bo musiałem pójspać, a teraz idź do łóżka i nie przeszkadza imię.",
    },
]
