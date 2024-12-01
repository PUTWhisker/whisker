package io.grpc.soundtransfer

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import com.google.protobuf.ByteString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.Thread.sleep
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread


//Define AudioRecord Object and other parameters
const val RECORDER_SAMPLE_RATE = 44100
const val AUDIO_SOURCE = MediaRecorder.AudioSource.MIC

//for raw audio can use
const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
val BUFFER_SIZE_RECORDING =
    AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)

class AudioStreamManager {
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var writingThread: Thread? = null
    var queue: LinkedBlockingQueue<ByteArray> = LinkedBlockingQueue<ByteArray>()
    var internalQueue = LinkedBlockingQueue<ByteArray>()

    var isRecordingAudio = false
    fun initAudioRecorder() {
        audioRecord = AudioRecord(
            AUDIO_SOURCE,
            RECORDER_SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            BUFFER_SIZE_RECORDING
        )
        if (audioRecord!!.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("audioStream", "error initializing AudioRecord");
            return
        }
        audioRecord?.startRecording()
    }

    fun record(): Flow<TranscirptionLiveRequest> = flow {
        isRecordingAudio = true
        recordingThread = thread(true) {
            val data = ByteArray(BUFFER_SIZE_RECORDING / 2)
            while (isRecordingAudio) {
                val read = audioRecord!!.read(data, 0, data.size)
                internalQueue.put(data.copyOf(read))

            }
        }
        writingThread = thread(true) {
            var largeChunk : ByteArray
            while (isRecordingAudio) {
                sleep(2000)
                val n = internalQueue.size
                var size = 0
                largeChunk = ByteArray(n * (BUFFER_SIZE_RECORDING /2))
                for (i in 0..<internalQueue.size) {
                    val element = internalQueue.take()
                    for (j in element.indices){
                        largeChunk[size] = element[j]
                        size++
                    }
                }
                queue.put(largeChunk.copyOf(size))
            }
        }
        while(isRecordingAudio){
            emit(
                transcirptionLiveRequest { this.soundData = ByteString.copyFrom(queue.take()) }
            )
        }
    }

    fun stop() {
        if (audioRecord != null) {
            isRecordingAudio = false
            audioRecord!!.stop()
            audioRecord!!.release()
            audioRecord = null
            recordingThread = null
            writingThread = null
        }
    }
}