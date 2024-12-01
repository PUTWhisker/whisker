const {soundClient, SoundRequest} = require('./consts.js')

let chunks = []
let record, finish

export function setupRecord() {
    recording().then((mediaRecorder) => {
        record = document.getElementById('record')
        finish = document.getElementById('stop')

        record.onclick = () => recordAudio(mediaRecorder)
        finish.onclick = () => stopAudio(mediaRecorder)

        mediaRecorder.ondataavailable = (e) => {
            chunks.push(e.data)
        }
        mediaRecorder.onstop = () => newAudioFile(chunks)
    })
}

async function recording() {
    return prepareRecord()
}

function prepareRecord() { //fix error handling
    return new Promise((resolve, reject) => {
        if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
            console.log("Can record")
            navigator.mediaDevices
                .getUserMedia({
                    audio: true
                })
                .then((stream) => {resolve (new MediaRecorder(stream))})
                .catch((err) => {
                    console.log(`Could not set getUserMedia: code = ${err.code}, message = ${err.message}`)
                    reject(err)
                })
        } else {
            console.log("getUserMedia not supported in this browser")
            reject(new Error("getUserMedia not supported in this browser"))
        }
    })
}


function recordAudio(mediaRecorder) {
    chunks = []
    mediaRecorder.start()
    record.style.background = "red"
    finish.style.background = "green"
    console.log("Start recording")
}

function stopAudio(mediaRecorder) {
    mediaRecorder.stop()
    record.style.background = ""
    finish.style.background = ""
    console.log("Stop recording")
}

function newAudioFile(chunks) {
    console.log("Trying to save file")
    const blob = new Blob(chunks, {type: "audio/wav; codecs=opus"})
    //===========================================
    const clips = document.getElementById('clips')
    const audio = document.createElement('audio')
    
    audio.setAttribute("controls", "")
    clips.appendChild(audio)
    console.log(chunks)
    const audioURL = window.URL.createObjectURL(blob)
    audio.src = audioURL
    //============================================= <- This prints recorded files on the screen, remove later (cause we won't do that I guess)
    let answer = sendFile(blob) // Not sure of this one
    console.log("Here handle the received answer")
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