
const { soundClient,
        SoundRequest, 
        SoundResponse, 
        TextMessage, 
        SoundStreamResponse, 
        SpeakerAndLine} = require('./consts.js')

const { button_register, button_login, button_getTranslation } = require('./authentication.js')

const _validFileExtensions = [".mp3", ".wav"];

export function setupConnection() {
    connectionTest()

    const form = document.getElementById('send_file')
    form.onsubmit = validateAndSend;
    //TODO: down from here are buttons from test.html
//     const register = document.getElementById('register')
//     register.onsubmit = button_register;
//     const login = document.getElementById('login')
//     login.onsubmit = button_login;
//     const getTransl = document.getElementById('getTranslation')
//     getTransl.onsubmit = button_getTranslation;
}


async function validateAndSend(e) {
    e.preventDefault()
    validate(e)
    // .then(result => {
    //   if (result) {
    //     showTranscriptedText(result); 
    //   }else {
    //     // showTranscriptedText(result); 
    //     // showTranslatededText(result); 
    //   }
    // })
    //TODO: uncomment upper code, I use this function for testing
    // SoundTranslationFunction
    //     validate(e)
    // .then(result => {
    //   if (result) {
    //     sendFileTranslation(document.getElementById('input_file'), 'en', 'pl')
    //   }
    // })
}

function showTranscriptedText(text) {
    var transcripted = document.getElementById("transciptedText");
    console.log(typeof(text))
    transcripted.innerText = text;
}

function showTranslatedText(text) {
    var translated = document.getElementById("translatedText");
    console.log(typeof(text))
    translated.innerText = text;
}

function connectionTest() { // Verify whether we can connect with the Whisper server
    let randomNum = Math.random()
    let request = new TextMessage();
    request.setText(randomNum.toString())
    soundClient.testConnection(request, {}, (err, response) => {
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
                let answer = sendFileTranslation(input.files[0], 'en', 'pl')
                let answer_flag = false;
                for await (const res of answer) {
                    console.log(res)
                    if(!answer_flag){
                        showTranscriptedText(res);
                        answer_flag = true;
                    } else {
                        showTranslatedText(res);
                    } 
                }
                return "A"
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
        soundClient.sendSoundFile(request, {}, (err, response) => {
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


async function *sendFileTranslation(file, fileLanguage, translationLanguage) {
    const reader = (file) =>
        new Promise((resolve, reject) => {
          const fr = new FileReader();
          fr.onload = () => resolve(fr);
          fr.onerror = (err) => reject(err);
          fr.readAsArrayBuffer(file);
        });
    let e = await reader(file)
    let buffer = e.result
    let byteArray = new Uint8Array(buffer)
    console.log(byteArray)
    let metadata = {'language': fileLanguage, 'translation': translationLanguage}
    let request = new SoundRequest()
    request.setSoundData(byteArray)
    let stream = soundClient.sendSoundFileTranslation(request, metadata)

    // Handle responses
    yield stream.on('data', (response) => {
        console.log(`Received response: ${response.getText()}`);
        return response.getText()
    });

    // Handle stream end
    stream.on('end', () => {
        console.log('Received everything, stream ended.');
    });

    // Handle errors
    stream.on('error', (err) => {
        console.log(`There was an error: ${err.code}: ${err.message}`);
    });
}
