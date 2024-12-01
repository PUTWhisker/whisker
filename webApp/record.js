const {soundClient, SoundRequest} = require('./consts.js')

let chunks = []
let record, finish
let shouldContinue = true

export async function *recordInChunks() {
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        console.log("getUserMedia is not supported in this browser")
        return
    }
    try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
        const recorder = new MediaRecorder(stream)
        let localChunks = []

        recorder.ondataavailable = (e) => {
            if (e.data.size > 0) {
                localChunks.push(e.data)
            }
        };
        recorder.onstop = () => {
            if (localChunks.length > 0) {
                const blob = new Blob(localChunks, { type: 'audio/webm' })
                newAudioFile(blob)
            }
        }
        recorder.start()
    
        console.log("Recording started")
        shouldContinue = true

        while (shouldContinue) {
            await new Promise((resolve) => setTimeout(resolve, 2000))
            recorder.requestData();
            if (localChunks.length > 0) {
                const blob = new Blob(localChunks, { type: 'audio/webm' })
                yield localChunks
                localChunks = []
            }
        }
        console.log("Recording stopped")
        stream.getTracks().forEach(track => track.stop())
    } catch (err) {
        console.error(`Error during recording setup: ${err.message}`)
    }
}

export function setupRecord() {
    finish = document.getElementById('stop')

    finish.onclick = () => stopAudio(mediaRecorder)
}


function stopAudio(mediaRecorder) {
    shouldContinue = false
    mediaRecorder.stop()
    record.style.background = ""
    finish.style.background = ""
    console.log("Stop recording")
}

async function newAudioFile(blob) {
    console.log("Trying to save file")
    console.log(blob)
    //===========================================
    const clips = document.getElementById('clips')
    const audio = document.createElement('audio')
    
    audio.setAttribute("controls", "")
    clips.appendChild(audio)
    const audioURL = window.URL.createObjectURL(blob)
    audio.src = audioURL
    //============================================= <- This prints recorded files on the screen, remove later (cause we won't do that I guess)
    //let answer = sendFile(blob) // Not sure of this one
    //console.log("Here handle the received answer")
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