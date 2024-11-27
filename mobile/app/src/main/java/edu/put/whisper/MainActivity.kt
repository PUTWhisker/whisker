package edu.put.whisper

import android.Manifest
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.android.material.bottomsheet.BottomSheetBehavior
import edu.put.whisper.utilities.Utilities
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val MICROPHONE_PERMISSION_CODE = 200
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var btnStop: ImageButton
    private lateinit var btnResume: ImageButton
    private lateinit var btnSave: ImageButton
    private lateinit var btnDelete: ImageButton
    private lateinit var btnList: ImageButton
    private lateinit var btnRecord: ImageButton
    private lateinit var btnCopy: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var tvTranscript: TextView
    private lateinit var utilities: Utilities
    private var isRecordingStopped: Boolean = false
    private var currentFileName: String? = null
    private var tempFilePath: String? = null

    // Timer variables
    private lateinit var tvTimer: TextView
    private lateinit var waveformView: WaveformView
    private var startTime: Long = 0L
    private var elapsedTime: Long = 0L
    private var handler = Handler()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetBG: View
    private lateinit var btnCancel: Button
    private lateinit var btnOk: Button
    private lateinit var btnTranscript: Button
    private lateinit var filenameInput: EditText

    private lateinit var db : AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        utilities = Utilities(this)
        tvTimer = findViewById(R.id.tvTimer)
        waveformView = findViewById(R.id.waveformView)

        btnStop = findViewById(R.id.btnStop)
        btnResume = findViewById(R.id.btnResume)
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)
        btnList = findViewById(R.id.btnList)
        btnRecord = findViewById(R.id.btnRecord)
        val bottomSheet: LinearLayout = findViewById(R.id.bottomSheet)
        bottomSheetBG = findViewById(R.id.bottomSheetBG)


        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        filenameInput = findViewById(R.id.filenameInput)
        btnCancel = findViewById(R.id.btnCancel)
        btnOk = findViewById(R.id.btnOk)
        btnTranscript = findViewById(R.id.btnTranscript)
        btnCopy = findViewById(R.id.btnCopy)
        tvTranscript = findViewById(R.id.tvTranscript)
        btnBack = findViewById(R.id.btnBack)


        if (isMicrophonePresent()) {
            getMicrophonePermission()
        }

        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "audioRecords"
        ).build()

        btnCancel.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetBG.visibility = View.GONE

            // Hide the keyboard
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(filenameInput.windowToken, 0)
        }

        btnOk.setOnClickListener {
            val fileName = filenameInput.text.toString()
            if (fileName.isEmpty()) {
                Toast.makeText(this, "Please enter a file name", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            saveRecording(fileName)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetBG.visibility = View.GONE

            utilities.setVisibility(View.GONE, btnStop, btnResume, btnSave, btnDelete)
            utilities.setVisibility(View.VISIBLE, btnRecord, btnList, btnBack)
            tvTimer.text = "00:00:00"
            waveformView.clearAmplitudes()

            // Hide the keyboard
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(filenameInput.windowToken, 0)
        }

        filenameInput.setOnClickListener {
            filenameInput.selectAll()
        }

        btnList.setOnClickListener{
            startActivity(Intent(this, GalleryActivity::class.java))
        }

        btnBack.setOnClickListener {
            utilities.goBack(this)
        }
    }

    private val amplitudeRunnable: Runnable = object : Runnable {
        override fun run() {
            if (mediaRecorder != null) {
                val maxAmplitude = mediaRecorder?.maxAmplitude?.toFloat() ?: 0f
                waveformView.addAmplitude(maxAmplitude)
            }
            handler.postDelayed(this, 100)
        }
    }

//    /storage/emulated/0/Android/data/edu.put.whisper/files/Music/test.mp3
    fun btnRecordPressed(v: View) {
        tempFilePath = getTempRecordingFilePath()
        utilities.setVisibility(View.GONE, btnList, btnRecord, btnBack)
        utilities.setVisibility(View.VISIBLE, btnSave, btnStop, btnTranscript, btnDelete)
        btnStop.setImageResource(R.drawable.ic_pause)
        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                setOutputFile(tempFilePath) // Save to a temporary file
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                prepare()
                start()
            }
            isRecordingStopped = false
            startTime = System.currentTimeMillis()
            elapsedTime = 0L
            handler.post(timerRunnable)
            handler.post(amplitudeRunnable)
            Toast.makeText(this, "Recording started", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun btnCopyPressed(v: View) {
        val transcriptText = tvTranscript.text.toString()
        if (transcriptText.isNotEmpty()) {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Transcript", transcriptText)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No text to copy", Toast.LENGTH_SHORT).show()
        }
    }
    fun btnTranscriptPressed(v:View){

        if (!isRecordingStopped) {
            Toast.makeText(this, "Please stop the recording first", Toast.LENGTH_LONG).show()
            return
        }

        utilities.setVisibility(View.GONE, tvTimer, btnRecord, btnList, btnResume, btnStop, btnCancel, btnSave, btnDelete, waveformView, btnTranscript)
        utilities.setVisibility(View.VISIBLE, tvTranscript, btnBack)

        lifecycleScope.launch {
            val filePath = tempFilePath ?: return@launch
            uploadRecording(filePath)
        }



    }

    fun btnStopPressed(v: View) {
        try {
            mediaRecorder?.apply {
                stop()
            }
            isRecordingStopped = true
            handler.removeCallbacks(timerRunnable)
            handler.removeCallbacks(amplitudeRunnable)
            elapsedTime += System.currentTimeMillis() - startTime
            btnStop.visibility = View.GONE
            utilities.setVisibility(View.VISIBLE, btnResume, btnTranscript)
            btnResume.setImageResource(R.drawable.ic_restart)
            Toast.makeText(this, "Recording stopped. You can now transcript it.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun btnResumePressed(v: View) {
        try {
            mediaRecorder?.apply {
                // Resetowanie MediaRecorder, aby przygotować go do ponownego użycia
                reset()
                // Ustawienie ustawień MediaRecorder na te same, co wcześniej
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                setOutputFile(tempFilePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                prepare()
                start()
            }

            // Resetowanie zegara i innych zmiennych
            elapsedTime = 0L
            startTime = System.currentTimeMillis()

            handler.removeCallbacks(timerRunnable)
            handler.removeCallbacks(amplitudeRunnable)
            handler.post(timerRunnable)
            handler.post(amplitudeRunnable)

            isRecordingStopped = false
            btnResume.visibility = View.GONE
            btnStop.visibility = View.VISIBLE
            btnTranscript.visibility = View.INVISIBLE

            tvTimer.text = "00:00:00"
            waveformView.clearAmplitudes()

            Toast.makeText(this, "Restarting recording", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun btnDeletePressed(v: View) {
        utilities.setVisibility(View.GONE, btnDelete)
        utilities.setVisibility(View.VISIBLE, btnBack)
        try {
            // Check if mediaRecorder is initialized and is recording
            if (mediaRecorder != null && !isRecordingStopped) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
            }
            mediaRecorder = null
            handler.removeCallbacks(timerRunnable)
            handler.removeCallbacks(amplitudeRunnable)

            val tempFile = File(tempFilePath ?: return)
            if (tempFile.exists()) {
                tempFile.delete()
            }

            isRecordingStopped = false
            currentFileName = null
            tempFilePath = null
            elapsedTime = 0L
            tvTimer.text = "00:00:00"
            waveformView.clearAmplitudes()

            utilities.setVisibility(View.GONE, btnStop, btnResume, btnSave)
            utilities.setVisibility(View.VISIBLE, btnRecord, btnList)
            btnTranscript.visibility = View.INVISIBLE

            Toast.makeText(this, "Recording deleted", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun btnSavePressed(v: View) {
        if (!isRecordingStopped) {
            Toast.makeText(this, "Please stop the recording first", Toast.LENGTH_LONG).show()
            return
        }

        // Generate a default file name based on the current date and time
        val defaultFileName = generateDefaultFileName()

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBG.visibility = View.VISIBLE
        btnTranscript.visibility = View.INVISIBLE
        filenameInput.setText(defaultFileName) // Set the default file name

    }

    private fun isMicrophonePresent(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
    }

    private fun getMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                MICROPHONE_PERMISSION_CODE
            )
        }
    }

    private fun getRecordingFilePath(fileName: String): String {
        val musicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        if (!musicDirectory.exists()) {
            musicDirectory.mkdirs() // Utwórz katalog, jeśli nie istnieje
        }
        val file = File(musicDirectory, "$fileName.mp3")
        return file.path
    }

    private fun getTempRecordingFilePath(): String {
        val contextWrapper = ContextWrapper(applicationContext)
        val musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val tempFile = File(musicDirectory, "tempRecording.mp3")
        return tempFile.path
    }

    private fun saveRecording(fileName: String) {
        currentFileName = fileName
        try {
            val finalFilePath = getRecordingFilePath(currentFileName!!)
            val tempFile = File(tempFilePath!!)
            val finalFile = File(finalFilePath)

            tempFile.copyTo(finalFile, overwrite = true)
            tempFile.delete() // Remove the temporary file


            mediaRecorder?.release()
            mediaRecorder = null

            // Zapisywanie rekordu w bazie danych
            val dirPath = getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath
            if (dirPath == null) {
                Toast.makeText(this, "Error: Unable to access music directory", Toast.LENGTH_LONG).show()
                return
            }

            val ampsPath = "$dirPath/$fileName"
            val timestamp = Date().time

            val amplitudes = waveformView.getAmplitudes()
            val duration = elapsedTime

            try {
                FileOutputStream(ampsPath).use { fos ->
                    ObjectOutputStream(fos).use { out ->
                        out.writeObject(amplitudes)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Error saving amplitudes", Toast.LENGTH_LONG).show()
                return
            }

            val record = AudioRecord(fileName, finalFilePath, timestamp, duration.toString(), ampsPath)

            GlobalScope.launch {
                db.audioRecordDao().insert(record)
            }

            Toast.makeText(this, "Recording saved", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val timerRunnable: Runnable = object : Runnable {
        override fun run() {
            val currentTime = System.currentTimeMillis()
            val millis = elapsedTime + (currentTime - startTime)
            val minutes = (millis / 60000).toInt() % 60
            val seconds = (millis / 1000).toInt() % 60
            val milliseconds = (millis % 1000) / 10
            tvTimer.text = String.format("%02d:%02d:%02d", minutes, seconds, milliseconds)
            handler.postDelayed(this, 10)
        }
    }

    private fun generateDefaultFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val date = Date()
        return "recording_${dateFormat.format(date)}"
    }

    private suspend fun uploadRecording(filePath: String) {
        utilities.uploadRecording(filePath) { output ->
            runOnUiThread {
                if (output != null) {
                    tvTranscript.text = output
                    utilities.setVisibility(View.VISIBLE, btnCopy)
                } else {
                    tvTranscript.text = "Transkrypcja nie jest dostępna."
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val tempFile = File(tempFilePath ?: return)
        if (tempFile.exists()) {
            tempFile.delete() // Delete the temporary file if it exists
        }
    }
}
