package edu.put.whisper

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.io.FileOutputStream
import kotlin.concurrent.thread


//Define AudioRecord Object and other parameters
const val RECORDER_SAMPLE_RATE = 44100
const val AUDIO_SOURCE = MediaRecorder.AudioSource.MIC
//for raw audio can use
const val RAW_AUDIO_SOURCE = MediaRecorder.AudioSource.UNPROCESSED
const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO
const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
val BUFFER_SIZE_RECORDING = AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)

class AudioStreamManager {

    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    var isRecordingAudio = false
    fun initAudioRecorder() {
        audioRecord = AudioRecord(
            AUDIO_SOURCE,
            RECORDER_SAMPLE_RATE,
            CHANNEL_CONFIG ,
            AUDIO_FORMAT,
            BUFFER_SIZE_RECORDING
        )
        if (audioRecord!!.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("audioStream", "error initializing AudioRecord");
            return
        }
        audioRecord?.startRecording()
    }

    fun record()
    {
        isRecordingAudio = true
        recordingThread = thread(true) {
            val data = ByteArray(BUFFER_SIZE_RECORDING / 2)

            while (isRecordingAudio) {
                val read = audioRecord!!.read(data, 0, data.size)
                val loggedBytes = data.take(16).joinToString(", ") { it.toString() }
                Log.i("audioStream", "Bytes read: [$loggedBytes], Read size: $read")
            }
        }
    }
    fun stop() {
        if (audioRecord != null) {
            isRecordingAudio = false
            audioRecord!!.stop()
            audioRecord!!.release()
            audioRecord = null
            recordingThread = null
        }
    }

}