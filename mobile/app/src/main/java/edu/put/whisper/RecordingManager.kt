package edu.put.whisper

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Environment
import android.os.Handler
import android.widget.Toast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.util.*

class RecordingManager(
    private val context: Context,
    private val handler: Handler,
    private val waveformView: WaveformView
) {
    private var mediaRecorder: MediaRecorder? = null
    private var isRecordingStopped: Boolean = false
    private var startTime: Long = 0L
    private var elapsedTime: Long = 0L
    private var tempFilePath: String? = null

    private val amplitudeRunnable: Runnable = object : Runnable {
        override fun run() {
            mediaRecorder?.let {
                val maxAmplitude = it.maxAmplitude.toFloat()
                waveformView.addAmplitude(maxAmplitude)
            }
            handler.postDelayed(this, 100)
        }
    }

    fun startRecording(tempFilePath: String) {
        this.tempFilePath = tempFilePath
        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                setOutputFile(tempFilePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                prepare()
                start()
            }
            isRecordingStopped = false
            startTime = System.currentTimeMillis()
            elapsedTime = 0L
            handler.post(amplitudeRunnable)
            Toast.makeText(context, "Recording started", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.stop()
            isRecordingStopped = true
            handler.removeCallbacks(amplitudeRunnable)
            elapsedTime += System.currentTimeMillis() - startTime
            Toast.makeText(context, "Recording stopped", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder?.release()
            mediaRecorder = null
        }
    }

    fun deleteRecording() {
        try {
            mediaRecorder?.release()
            mediaRecorder = null
            handler.removeCallbacks(amplitudeRunnable)

            tempFilePath?.let {
                val tempFile = File(it)
                if (tempFile.exists()) {
                    tempFile.delete()
                }
            }

            isRecordingStopped = false
            tempFilePath = null
            elapsedTime = 0L
            waveformView.clearAmplitudes()

            Toast.makeText(context, "Recording deleted", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveRecording(fileName: String, db: AppDatabase): String? {
        if (!isRecordingStopped || tempFilePath == null) {
            return null
        }

        val finalFilePath = getRecordingFilePath(fileName)
        val tempFile = File(tempFilePath!!)
        val finalFile = File(finalFilePath)

        try {
            tempFile.copyTo(finalFile, overwrite = true)
            tempFile.delete()

            val ampsPath = "${context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath}/$fileName"
            val timestamp = Date().time

            val amplitudes = waveformView.getAmplitudes()
            val duration = elapsedTime

            FileOutputStream(ampsPath).use { fos ->
                ObjectOutputStream(fos).use { out ->
                    out.writeObject(amplitudes)
                }
            }

            val record = AudioRecord(fileName, finalFilePath, timestamp, duration.toString(), ampsPath)
            GlobalScope.launch {
                db.audioRecordDao().insert(record)
            }

            return finalFilePath
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving recording", Toast.LENGTH_LONG).show()
            return null
        }
    }

    private fun getRecordingFilePath(fileName: String): String {
        val musicDirectory = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val file = File(musicDirectory, "$fileName.mp3")
        return file.path
    }
}
