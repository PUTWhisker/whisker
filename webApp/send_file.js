const { soundClient,
        TextMessage, 
        TranscriptionRequest, 
        TranslationRequest, 
        TranscirptionLiveRequest, 
        SoundResponse,
        SoundStreamResponse,
        SpeakerAndLineResponse} = require('./consts.js')

const { recordInChunks } = require('./record.js')

export function connectionTest() { // Verify whether we can connect with the Whisper server
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



export function sendFile(file, source_language) { // Send file to the server and return the answer
    console.log("Sending file for transcription")
    let reader = new FileReader()
    console.log(file)
    reader.readAsArrayBuffer(file)
    return new Promise((resolve, reject) => {
        reader.onload = function (e) {
            let buffer = e.target.result
            let byteArray = new Uint8Array(buffer)
            console.log(byteArray)
            let request = new TranscriptionRequest()
            request.setSoundData(byteArray)
            request.setSourceLanguage(source_language)
            soundClient.transcribeFile(request, {}, (err, response) => {
                if (err) {
                    console.log(`Could not send files to the server: code = ${err.code}, message = ${err.message}`)
                    reject(err)
                }
                let answer = response.getText()
                console.log(answer)
                console.log("Success! Answer should be visible in the console")
                resolve(answer)
            })
        }
    })
}


export async function* sendFileTranslation(file, source_language, translation_language) {
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
    let request = new TranslationRequest()
    request.setSoundData(byteArray)
    request.setSourceLanguage(source_language)
    request.setTranslationLanguage(translation_language)
    const stream = soundClient.translateFile(request, {})

    const responseQueue = []
    let resolveQueue = null

    stream.on('data', (response) => {
        const text = response.getText()
        console.log(`Received response: ${text}`)
        responseQueue.push(text)
        if (resolveQueue) {
            resolveQueue()
            resolveQueue = null
        }
    });

    stream.on('end', () => {
        console.log('Received everything, stream ended.')
        if (resolveQueue) {
            resolveQueue()
            resolveQueue = null
        }
    });

    stream.on('error', (err) => {
        console.error(`There was an error: ${err.code}: ${err.message}`)
        if (resolveQueue) {
            resolveQueue()
            resolveQueue = null
        }
    });

    while (responseQueue.length > 0 || !stream.finished) {
        if (responseQueue.length > 0) {
            yield responseQueue.shift()
        } else {
            await new Promise((resolve) => (resolveQueue = resolve))
        }
    }
}






export async function transcribeLiveWeb() {
    console.log("dzialam wgl lol")
    let sessionId = await getSessionId(new TranscriptionRequest())
    let metadata = {"session_id": sessionId}
    console.log(metadata)
    let recording = recordInChunks()
    for await (const audioChunk of recording) {
        console.log(`I'm sending the request`)
        let request = new TranscriptionRequest()
        request.setSoundData(audioChunk.byteArray)
        await soundClient.transcribeLiveWeb(request, metadata, (err, response) => {
            if (err) {
                console.log(`Could not establish connection with the server: code = ${err.code}, message = ${err.message}`)
                return
            }
            console.log(`I got the response ${response.getText()}`)
        })
    }
}


async function getSessionId(request) {
    return new Promise((resolve, reject) => {
        soundClient.transcribeLiveWeb(request, {}, (err, response) => {
            if (err) {
                reject(`Could not establish connection with the server: code = ${err.code}, message = ${err.message}`);
            } else {
                resolve(response.getText());
            }
        });
    });
}