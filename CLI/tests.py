import unittest
import random
from grpcClient import GrpcClient

audioFile = "english.wav"

class TestConsoleSoundTransfer(unittest.IsolatedAsyncioTestCase):
    def setUp(self):
        self.grpcClient = GrpcClient("127.0.0.1", "7070", "en", None, False, "pl")
        with open(audioFile, "rb") as file:
            self.audio = file.read()  # read audio as bytes


    async def test_startApp(self):
        seed = str(random.randint(0, 10000))
        res = await self.grpcClient.initiateConnection(seed)
        self.assertEqual(res.text, seed)


    async def test_sendFile(self):
        res = await self.grpcClient.sendSoundFile(self.audio)
        self.assertIsInstance(res.text, str)


    async def test_sendFileTranslation(self):
        res = await self.grpcClient.SendSoundFileTranslation(self.audio)
        self.assertEqual(len(res), 2)
        self.assertIsInstance(res[0].text, str)
        self.assertIsInstance(res[1].text, str)


    async def test_diarizateSpeakers(self):
        res = await self.grpcClient.diarizateSpeakers(self.audio)
        self.assertEqual(len(res), 1)
        self.assertIsInstance(res[0].text, str)

    # Not really sure how to test it properly
    # def test_record():
        # pass



if __name__ == '__main__':
    unittest.main()