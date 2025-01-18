const { transcribeLiveWeb, connectionTest, getSessionId } = require('./send-file.js')


let intervalId
var recording = false;


window.onload = function() {
    connectionTest()

    const blob = new Blob(
        [`  
        class MyProcessor extends AudioWorkletProcessor {
            process(inputs, outputs, parameters) {
                const input = inputs[0]; 
                if (input && input.length > 0) {
                    this.port.postMessage(input[0]); 
                }
                return true; 
            }
        }
        registerProcessor('my-processor', MyProcessor);
    `], { type: 'application/javascript' }
    )
    const url = URL.createObjectURL(blob);
    const audioContext = new AudioContext( { sampleRate: 44100 })
    audioContext.audioWorklet.addModule(url)
    const myAudioWorkletNode = new AudioWorkletNode(audioContext, 'my-processor')

    micButton = document.getElementById("mic_button")
    micButton.addEventListener("click", changeMicState(this, audioContext, myAudioWorkletNode))
}

function changeMicState(microphone, audioContext, myAudioWorkletNode) {
    const recording_dots = document.getElementById("recording_dots");
    recording = !recording;
    const icon = microphone.querySelector('.material-symbols-outlined');
    icon.innerText = recording ? 'stop_circle' : 'mic';
    if (recording) {
      recordAndSend(audioContext, myAudioWorkletNode)
    } else {
      stopRecording(audioContext, myAudioWorkletNode)
    }
    recording_dots.classList.toggle('hidden');
  }


async function recordAndSend(audioContext, myAudioWorkletNode) {
    
    recordButton.disabled = true
    stopButton.disabled = false
    console.log("button pressed")

    const stream = await navigator.mediaDevices.getUserMedia({ audio: { channelCount: 1} })
    const source = audioContext.createMediaStreamSource(stream)
    source.connect(myAudioWorkletNode)
    myAudioWorkletNode.connect(audioContext.destination)

    myAudioWorkletNode.port.onmessage = (event) => {
        audioChunks.push(event.data)
    };
    console.log("Before metadata")
    let sessionId = await getSessionId()
    let metadata = {"session_id": sessionId}
    intervalId = setInterval(() => {
        if (audioChunks.length > 0) {
            const currentChunk = audioChunks.splice(0)
            transcribeLiveWeb(currentChunk, "pl", metadata).then((response) => {
                        console.log(response.getText())
                    })
        }
    }, 2000) 
}


function stopRecording(audioContext, myAudioWorkletNode) {
    stopButton.disabled = true
    recordButton.disabled = false

    clearInterval(intervalId)
    myAudioWorkletNode.disconnect()
    audioContext.close()
    console.log("Koniec nagrywania")
    //TODO: Here send the remaining of the recording
}