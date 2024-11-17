from transformers import M2M100ForConditionalGeneration, M2M100Tokenizer
import os

class Translator:

    def __init__(self, model:str):
        self.model = M2M100ForConditionalGeneration.from_pretrained(model)
        self.tokenizer = M2M100Tokenizer.from_pretrained(model)


    def translate(self, inputText: str, sourceLanguage: str, targetLanguage: str) -> str:
        self.tokenizer.src_lang = sourceLanguage
        modelInputs = self.tokenizer(inputText, return_tensors="pt")
        generatedTokens = self.model.generate(**modelInputs, forced_bos_token_id=self.tokenizer.get_lang_id(targetLanguage))
        res = self.tokenizer.batch_decode(generatedTokens, skip_special_tokens=True)
        return res
