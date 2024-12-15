
const { connectionTest, sendFile, sendFileTranslation, diarizateFile} = require('./send-file.js')
const _validFileExtensions = [".mp3", ".wav"];

window.onload = function() {
    connectionTest()
    submitButton = document.getElementById("start")
    submitButton.addEventListener("click", processFile)
}



async function processFile() {
    document.getElementById("transcription_result").textContent = ""
    document.getElementById("translation_result").textContent = ""
    let source_language = document.getElementById("choose_lang").value
    if (source_language == "Choose language") {
        source_language = ""
    } 
    let uploadedFile = document.getElementById("upload")
    if (!validate(uploadedFile)) {
        return
    }
    try {
        if (document.getElementById("translate").checked) {
            console.log("Trying to execute translation")
            let translate_language = document.getElementById("choose_trans_lang").value
            if (translate_language == "Choose language") {
                translate_language = ""
            } 
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
        } else if (document.getElementById("role_division").checked){
            let res = await diarizateFile(uploadedFile.files[0], source_language)
            for (res in answer) {
                document.getElementById("transcription_result").textContent += res.getSpeakerName() + " " + res.getText()
            }
        } else {
            console.log("Trying to execute transcription")
            answer = await sendFile(uploadedFile.files[0], source_language)
            console.log(answer)
            document.getElementById("transcription_result").textContent = answer.getText()
        }
    } catch (err){
        console.log(`An error occured when sending a file: code = ${err.code}, message = ${err.message}`)
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
