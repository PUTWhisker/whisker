
const {client, SoundRequest, TextMessage} = require('./consts.js')

const _validFileExtensions = [".mp3", ".wav"];

export function setupConnection() {
    connectionTest()

    const form = document.getElementById('send_file')
    form.onsubmit = validateAndSend;
}

async function validateAndSend(e) {
    e.preventDefault()
    validate(e)
    .then(result => {
      if (result) {
        showTranscriptedText(result); 
      }
    })
}

function showTranscriptedText(text) {
    var transcripted = document.getElementById("transciptedText");
    console.log(typeof(text))
    transcripted.innerText = text;
}

function connectionTest() { // Verify whether we can connect with the Whisper server
    let randomNum = Math.random()
    let request = new TextMessage();
    request.setText(randomNum.toString())
    client.testConnection(request, {}, (err, response) => {
        if (err) {
            console.log(`Could not connect to the server: code = ${err.code}, message = ${err.message}`)
            return false;
        }
        let answer = response.getText();
        if (answer == randomNum.toString()) {
            console.log("Connected to the server")
        }
        else {
            console.log(`Failed to connect to the server (wrong response)`)
        }
    })
}

async function validate(e) { // Validate input file format
    let input = document.getElementById('input_file');
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
                let answer = await sendFile(input.files[0])
                return answer
            }
        }
    }
}

function sendFile(file) { // Send file to the server and return the answer
    let reader = new FileReader()
    console.log(file)
    reader.readAsArrayBuffer(file)

    reader.onload = function(e) {
        let buffer = e.target.result
        let byteArray = new Uint8Array(buffer)
        console.log(byteArray)
        let request = new SoundRequest()
        request.setSoundData(byteArray)
        request.setFlagsList("model: small")
        request.setFlagsList("language: english")
        client.sendSoundFile(request, {}, (err, response) => {
            if (err) {
                console.log(`Could not send files to the server: code = ${err.code}, message = ${err.message}`)
                return
            }
            let answer = response.getText()
            console.log(answer)
            console.log("Success! Answer should be visible in the console")
            return answer
        })
    }
}
