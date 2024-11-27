package edu.put.whisper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import edu.put.whisper.utilities.Utilities
import io.grpc.authentication.AuthenticationClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class StartActivity : AppCompatActivity() {
    private lateinit var btnRecordActivity: CardView
    private lateinit var btnChooseFile: CardView
    private lateinit var btnTranscriptLive: CardView
    private lateinit var tvSelectedFile: TextView
    private lateinit var tvTranscriptedFile: TextView
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetLogin: View
    private lateinit var bottomSheetTitle: TextView
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var btnCancelLog: Button
    private lateinit var btnSubmit: Button
    private lateinit var passwordInput: EditText
    private lateinit var repeatPasswordInput: EditText
    private lateinit var loginInput: EditText
    private lateinit var tvHello: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnHistory: CardView
    private lateinit var btnCopy: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var tvChoose: TextView
    private lateinit var rvTranscriptions: RecyclerView
    private lateinit var transcriptionAdapter: TranscriptionAdapter
    private lateinit var llFileRecord: LinearLayout
    private lateinit var utilities: Utilities
    private var tempFilePath: String? = null
    private val PICK_FILE_REQUEST_CODE = 1

    private lateinit var authClient: AuthenticationClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val serverUri = Uri.parse("http://100.80.80.156:50051/")
        authClient = AuthenticationClient(serverUri)
        utilities = Utilities(this)

        btnRecordActivity = findViewById(R.id.btnRecordActivity)
        btnTranscriptLive = findViewById(R.id.btnTranscriptLive)
        btnCopy = findViewById(R.id.btnCopy)
        btnChooseFile = findViewById(R.id.btnChooseFile)
        tvSelectedFile = findViewById(R.id.tvSelectedFile)
        tvTranscriptedFile = findViewById(R.id.tvTranscriptedFile)
        tvChoose = findViewById(R.id.tvChoose)
        btnLogin = findViewById(R.id.btnLogin)
        loginInput = findViewById(R.id.loginInput)
        passwordInput = findViewById(R.id.passwordInput)
        repeatPasswordInput = findViewById(R.id.repeatPasswordInput)
        btnRegister = findViewById(R.id.btnRegister)
        btnLogin = findViewById(R.id.btnLogin)
        btnCancelLog = findViewById(R.id.btnCancelLog)
        btnSubmit = findViewById(R.id.btnSubmit)
        bottomSheetTitle = findViewById(R.id.bottomSheetTitle)
        tvHello = findViewById(R.id.tvHello)
        btnHistory = findViewById(R.id.btnHistory)
        btnLogout = findViewById(R.id.btnLogout)
        btnBack = findViewById(R.id.btnBack)
        rvTranscriptions = findViewById(R.id.rvTranscriptions)
        llFileRecord = findViewById(R.id.llFileRecord)
        rvTranscriptions.layoutManager = LinearLayoutManager(this)


        val bottomSheetL: LinearLayout = findViewById(R.id.bottomSheetL)
        bottomSheetLogin = findViewById(R.id.bottomSheetLogin)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetL)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        btnRegister.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetLogin.visibility = View.VISIBLE
            bottomSheetTitle.setText("Register")
            repeatPasswordInput.visibility = View.VISIBLE
            btnSubmit.text = "Register"

        }

        btnLogin.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetLogin.visibility = View.VISIBLE
            bottomSheetTitle.setText("Log in")
            btnLogin.setText("Log in")
        }
        btnCancelLog.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetLogin.visibility = View.GONE
            loginInput.text.clear()
            passwordInput.text.clear()
            repeatPasswordInput.text.clear()
            repeatPasswordInput.visibility = View.GONE

        }

        btnSubmit.setOnClickListener {
            val username = loginInput.text.toString()
            val password = passwordInput.text.toString()
            val repeatPassword = repeatPasswordInput.text.toString()
            val title = bottomSheetTitle.text.toString()
            if (title == "Register") {
                if (password == repeatPassword) {
                    GlobalScope.launch(Dispatchers.IO) {
                        val success = authClient.Register(username, password)
                        withContext(Dispatchers.Main) {
                            if (success) {
                                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                                bottomSheetLogin.visibility = View.GONE
                                Toast.makeText(
                                    this@StartActivity,
                                    "Registration successful! You can now log in.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                loginInput.text.clear()
                                passwordInput.text.clear()
                                repeatPasswordInput.text.clear()
                                repeatPasswordInput.visibility = View.GONE
                                btnLogin.setText("Log in")
                            } else {
                                Toast.makeText(
                                    this@StartActivity,
                                    "Registration failed. Try again.",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                        }
                    }

                } else {
                    Toast.makeText(
                        this,
                        "Passwords do not match. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                    passwordInput.text.clear()
                    repeatPasswordInput.text.clear()
                }
            }
            if(title == "Log in"){
                GlobalScope.launch(Dispatchers.IO) {
                    val success = authClient.Login(username, password)
                    withContext(Dispatchers.Main) {
                        if (success) {
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                            bottomSheetLogin.visibility = View.GONE
                            Toast.makeText(this@StartActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                            utilities.setVisibility(View.GONE, btnLogin, btnRegister)
                            tvHello.text = "Hello $username"
                            utilities.setVisibility(View.VISIBLE, btnHistory, btnLogout, tvHello)
                        } else {
                            Toast.makeText(this@StartActivity, "Login failed. Try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        btnHistory.setOnClickListener{
            showTranslationHistory()
        }

        btnRecordActivity.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btnChooseFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "audio/*"  // Tylko pliki audio
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
        }

        btnBack.setOnClickListener {
            utilities.setVisibility(View.VISIBLE, btnRecordActivity, btnChooseFile, btnLogin, btnRegister, tvChoose, btnTranscriptLive)
            utilities.setVisibility(View.GONE, btnCopy, btnBack)
            tvTranscriptedFile.text = " "
            tvSelectedFile.text = "No file selected"
        }

        btnLogout.setOnClickListener {
            utilities.setVisibility(View.VISIBLE, btnLogin, btnRegister, btnChooseFile, btnRecordActivity)
            utilities.setVisibility(View.GONE, btnHistory, btnLogout, tvHello)
            utilities.setVisibility(View.INVISIBLE, rvTranscriptions)
            tvHello.text = ""
            authClient.Logout()
            Toast.makeText(this, "Successfully logged out.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val fileName = getFileNameFromUri(uri)
                if (fileName != null) {
                    tvSelectedFile.text = "$fileName is being transcripted"
                    lifecycleScope.launch {
                        val filePath = getFilePathFromUri(uri)
                        if (filePath != null) {
                            utilities.uploadRecording(filePath) { transcription ->
                                runOnUiThread {
                                    if (transcription != null) {
                                        utilities.setVisibility(View.GONE, btnRecordActivity, btnChooseFile, btnLogin, btnRegister, tvChoose, btnTranscriptLive)
                                        utilities.setVisibility(View.VISIBLE, btnCopy, btnBack)
                                        tvTranscriptedFile.text = transcription
                                    } else {
                                        Toast.makeText(this@StartActivity, "Transcription failed.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(this@StartActivity, "Unable to get path", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Unable to get file name", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun getFilePathFromUri(uri: Uri): String? {
        var inputStream = contentResolver.openInputStream(uri)
        return if (inputStream != null) {
            val tempFile = File.createTempFile("temp_audio", ".mp3", cacheDir)
            inputStream.copyTo(FileOutputStream(tempFile))
            tempFile.absolutePath // Zwracamy ścieżkę do tymczasowego pliku
        } else {
            null
        }
    }




    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                fileName = cursor.getString(index)
            }
        }
        return fileName
    }

    private fun showTranslationHistory() {
        llFileRecord.visibility = View.GONE
        rvTranscriptions.visibility = View.VISIBLE

        GlobalScope.launch(Dispatchers.IO) {
            val history = authClient.GetTranslations()
            val formattedHistory = history.map { "${it.text}, ${it.timestamp}" }
            withContext(Dispatchers.Main) {
                transcriptionAdapter = TranscriptionAdapter(formattedHistory) { transcription ->
                    Toast.makeText(this@StartActivity, "Clicked: $transcription", Toast.LENGTH_SHORT).show()
                }
                rvTranscriptions.adapter = transcriptionAdapter
            }
        }
    }

}