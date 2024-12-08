const { transcribeLiveWeb, connectionTest, getSessionId } = require('./send-file.js')

let audioChunks = []
let intervalId

window.onload = async function() {
    connectionTest()
    recordButton = document.getElementById("record")
    stopButton = document.getElementById("stop")

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
    await audioContext.audioWorklet.addModule(url)
    const myAudioWorkletNode = new AudioWorkletNode(audioContext, 'my-processor')

    recordButton.addEventListener('click', async () => {
        recordButton.disabled = true
        stopButton.disabled = false
        console.log("button pressed")
    
        const stream = await navigator.mediaDevices.getUserMedia({ audio: { channelCount: 1} })
        const source = audioContext.createMediaStreamSource(stream)
        source.connect(myAudioWorkletNode)
        myAudioWorkletNode.connect(audioContext.destination)
    
        // Collect audio data in chunks
        myAudioWorkletNode.port.onmessage = (event) => {
            audioChunks.push(event.data); // Collect raw PCM data
        };
        console.log("Before metadata")
        let sessionId = await getSessionId()
        let metadata = {"session_id": sessionId}
        intervalId = setInterval(() => {
            if (audioChunks.length > 0) {
                const currentChunk = audioChunks.splice(0); // Extract all available chunks
                transcribeLiveWeb(currentChunk, "pl", metadata).then((response) => {
                            console.log(response.getText())
                        })
            }
        }, 2000) 
    })
    stopButton.addEventListener('click', () => {
        stopButton.disabled = true
        recordButton.disabled = false
    
        clearInterval(intervalId) // Stop the interval
        myAudioWorkletNode.disconnect()
        audioContext.close()
        console.log("Koniec nagrywania")
        //TODO: Here send the remaining of the recording
    })
}