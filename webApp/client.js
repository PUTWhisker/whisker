
const { connectionTest, sendFile, sendFileTranslation} = require('./send_file.js')
const _validFileExtensions = [".mp3", ".wav"];

window.onload = function() {
    connectionTest()
    submitButton = document.getElementById("start")
    submitButton.addEventListener("click", validateAndSend)
}



async function validateAndSend() {
    document.getElementById("transcription_result").textContent = ""
    document.getElementById("translation_result").textContent = ""
    let translationCheckbox = document.getElementById("translate")
    let source_language = document.getElementById("choose_lang").value
    if (source_language == "Choose language") {
        source_language = ""
    } 
    let uploadedFile = document.getElementById("upload")
    if (!validate(uploadedFile)) {
        return
    }

    if (translationCheckbox.checked) {
        console.log("Trying to execute translation")
        let translate_language = document.getElementById("choose_trans_lang").value
        if (translate_language == "Choose language") {
            translate_language = ""
        } 
        console.log(translate_language)
        let answer = sendFileTranslation(uploadedFile.files[0], source_language, translate_language)
                let receivedTranscription = false
                for await (const res of answer) {
                    console.log(res)
                    if (!receivedTranscription) {
                        document.getElementById("transcription_result").textContent = res
                        receivedTranscription = true
                    } else {
                        document.getElementById("translation_result").textContent = res
                    }
                }
    } else {
        console.log("Trying to execute transcription")
        res = await sendFile(uploadedFile.files[0], source_language)
        console.log("AAAA")
        console.log(res)
        document.getElementById("transcription_result").textContent = res
    }
}

async function validate(input) { // Validate input file format
    if (input.type == "file") {
        let sFileName = input.value;
        if (sFileName.length > 0) {
            let blnValid = false
            for (let j = 0; j < _validFileExtensions.length; j++) {
                let sCurExtension = _validFileExtensions[j]
                if (sFileName.substr(sFileName.length - sCurExtension.length, sCurExtension.length).toLowerCase() == sCurExtension.toLowerCase()) {
                    blnValid = true
                    break
                }
            }
            if (!blnValid) {
                alert("Sorry, " + sFileName.split('\\').pop() + " is invalid, allowed extensions are: " + _validFileExtensions.join(", "))
                return false
            } else {
                return true
            }
        }
    }
}
