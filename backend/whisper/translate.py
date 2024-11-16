from transformers import M2M100ForConditionalGeneration, M2M100Tokenizer

class Translator:

    def __init__(self):
        self.model = M2M100ForConditionalGeneration.from_pretrained("facebook/m2m100_418M")
        self.tokenizer = M2M100Tokenizer.from_pretrained("facebook/m2m100_418M")


    def translate(self, inputText: str, sourceLanguage: str, targetLanguage: str) -> str:
        self.tokenizer.src_lang = sourceLanguage
        modelInputs = self.tokenizer(inputText, return_tensors="pt")
        generatedTokens = self.model.generate(**modelInputs, forced_bos_token_id=self.tokenizer.get_lang_id(targetLanguage))
        res = self.tokenizer.batch_decode(generatedTokens, skip_special_tokens=True)
        return res
