from transformers import M2M100ForConditionalGeneration, M2M100Tokenizer
import os
import torch

class Translator:

    def __init__(self, model:str, cuda:str):
        self.device = torch.device("cuda" if torch.cuda.is_available() and cuda else "cpu")
        self.model = M2M100ForConditionalGeneration.from_pretrained(model).to(self.device)
        self.tokenizer = M2M100Tokenizer.from_pretrained(model)


    def translate(self, inputText: str, sourceLanguage: str, targetLanguage: str) -> str:
        self.tokenizer.src_lang = sourceLanguage
        modelInputs = self.tokenizer(inputText, return_tensors="pt").to(self.device)
        generatedTokens = self.model.generate(**modelInputs, forced_bos_token_id=self.tokenizer.get_lang_id(targetLanguage))
        res = self.tokenizer.batch_decode(generatedTokens, skip_special_tokens=True)
        return res
