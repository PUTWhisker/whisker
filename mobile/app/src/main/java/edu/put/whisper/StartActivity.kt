package edu.put.whisper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.provider.Settings.Global
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import edu.put.whisper.animations.LoadingAnimation
import edu.put.whisper.utils.Utilities
import edu.put.whisper.utils.isConnectedToInternet
import edu.put.whisper.utils.isServerAlive
import io.grpc.authentication.AuthenticationClient
import io.grpc.soundtransfer.SoundTransferClient
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
    private lateinit var logoWhisper: ImageView
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
    private lateinit var llFileRecord: LinearLayout
    private lateinit var utilities: Utilities
    private val PICK_FILE_REQUEST_CODE = 1
    private lateinit var authClient: AuthenticationClient
    private var isUserLoggedIn = false
    private var isReturningFromFileSelection = false
    private lateinit var tvConnectionStatus: TextView
    private lateinit var connectivityReceiver: BroadcastReceiver
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val serverUrl = getString(R.string.server_url)
        Log.d("ServerStatus", "Server URL: $serverUrl")
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus)

        val connectivityReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val noConnectivity = intent?.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false) == true
                if (noConnectivity) {
                    tvConnectionStatus.text = "Brak połączenia z internetem"
                    tvConnectionStatus.visibility = TextView.VISIBLE
                } else {
                    checkConnectionAndServerStatus(serverUrl)
                }
            }
        }
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(connectivityReceiver, filter)
        checkConnectionAndServerStatus(serverUrl)


        val serverUri = Uri.parse(getString(R.string.server_url))
        authClient = AuthenticationClient(serverUri, this)
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
        logoWhisper = findViewById(R.id.logoWhisper)
        rvTranscriptions.layoutManager = LinearLayoutManager(this)

        val bottomSheetL: LinearLayout = findViewById(R.id.bottomSheetL)
        bottomSheetLogin = findViewById(R.id.bottomSheetLogin)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetL)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        loginInput.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                passwordInput.isEnabled = true
                passwordInput.requestFocus()
                passwordInput.postDelayed({
                    passwordInput.requestFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(passwordInput, InputMethodManager.SHOW_IMPLICIT)
                }, 50)
                true
            } else {
                false
            }
        }

        passwordInput.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                btnSubmit.performClick()
                true
            } else {
                false
            }
        }


        btnRegister.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetLogin.visibility = View.VISIBLE
            bottomSheetTitle.setText("Register to see transcription history")
            bottomSheetTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            repeatPasswordInput.visibility = View.VISIBLE
            btnSubmit.text = "Register"
            showKeyboard(loginInput)

        }

        btnLogin.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetLogin.visibility = View.VISIBLE
            bottomSheetTitle.setText("Log in")
            btnLogin.setText("Log in")
            showKeyboard(loginInput)
        }
        btnCancelLog.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetLogin.visibility = View.GONE
            loginInput.text.clear()
            passwordInput.text.clear()
            repeatPasswordInput.text.clear()
            repeatPasswordInput.visibility = View.GONE
            hideKeyboard()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        }

        btnSubmit.setOnClickListener {
            val username = loginInput.text.toString()
            val password = passwordInput.text.toString()
            val repeatPassword = repeatPasswordInput.text.toString()
            val title = bottomSheetTitle.text.toString()

            hideKeyboard()
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
            } else if (title == "Log in") {
                GlobalScope.launch(Dispatchers.IO) {
                    val success = authClient.Login(username, password)
                    withContext(Dispatchers.Main) {
                        if (success) {
                            isUserLoggedIn = true
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                            bottomSheetLogin.visibility = View.GONE
                            Toast.makeText(this@StartActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                            utilities.setVisibility(View.GONE, btnLogin, btnRegister)
                            tvHello.text = "Hello $username"
                            utilities.setVisibility(View.VISIBLE, btnLogout, tvHello)
                            val icHistoryImageView: ImageView = findViewById(R.id.ic_history)
                            val icArrowImageView: ImageView = findViewById(R.id.ic_arrow)
                            icHistoryImageView.backgroundTintList = ContextCompat.getColorStateList(this@StartActivity, R.color.primaryDark)
                            icHistoryImageView.imageTintList = ContextCompat.getColorStateList(this@StartActivity, R.color.white)
                            findViewById<TextView>(R.id.historyText).text = "History"
                            findViewById<TextView>(R.id.historyText).setTextColor(ContextCompat.getColor(this@StartActivity, R.color.primaryDark))
                            icArrowImageView.imageTintList = ContextCompat.getColorStateList(this@StartActivity, R.color.primaryDark)


                        } else {
                            Toast.makeText(this@StartActivity, "Login failed. Try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        btnHistory.setOnClickListener{
            if(isUserLoggedIn){
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
            } else {
                Toast.makeText(this, "Please log in to view history.", Toast.LENGTH_SHORT).show()
            }
        }

        btnRecordActivity.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btnChooseFile.setOnClickListener {
            val startLayout = findViewById<ScrollView>(R.id.start_layout)
            startLayout.visibility = View.GONE
            val loadingAnimation = findViewById<LoadingAnimation>(R.id.LoadingAnimationStart)
            loadingAnimation.visibility = View.VISIBLE
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "audio/*"  // Tylko pliki audio
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
        }
        btnTranscriptLive.setOnClickListener {
            val intent = Intent(this, LiveTranscriptionActivity::class.java)
            startActivity(intent)

        }

        btnBack.setOnClickListener {
            utilities.setVisibility(View.VISIBLE, btnRecordActivity, btnChooseFile, btnLogin, btnRegister, tvChoose, btnTranscriptLive)
            utilities.setVisibility(View.GONE, btnCopy, btnBack)
            tvTranscriptedFile.text = " "
            tvSelectedFile.text = "No file selected"
        }

        btnLogout.setOnClickListener {
            utilities.setVisibility(View.VISIBLE, btnLogin, btnRegister, btnChooseFile, btnRecordActivity)
            utilities.setVisibility(View.GONE, btnLogout, tvHello)
            utilities.setVisibility(View.INVISIBLE, rvTranscriptions)
            isUserLoggedIn = false
            loginInput.text.clear()
            passwordInput.text.clear()
            tvHello.text = ""
            val icHistoryImageView: ImageView = findViewById(R.id.ic_history)
            val icArrowImageView: ImageView = findViewById(R.id.ic_arrow)
            icHistoryImageView.backgroundTintList = ContextCompat.getColorStateList(this@StartActivity, R.color.grayDarkDisabled)
            icHistoryImageView.imageTintList = ContextCompat.getColorStateList(this@StartActivity, R.color.white)
            findViewById<TextView>(R.id.historyText).text = "Log in to view history"
            findViewById<TextView>(R.id.historyText).setTextColor(ContextCompat.getColor(this@StartActivity, R.color.grayDarkDisabled))
            icArrowImageView.imageTintList = ContextCompat.getColorStateList(this@StartActivity, R.color.gray)
            authClient.Logout()
            Toast.makeText(this, "Successfully logged out.", Toast.LENGTH_SHORT).show()
        }




    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            isReturningFromFileSelection = true
            data?.data?.let { uri ->
                val fileName = getFileNameFromUri(uri)
                if (fileName != null) {
                    tvSelectedFile.text = "$fileName is being transcripted"
                    Log.d("DEBUG", "File selected: $fileName")

                    val startLayout = findViewById<ScrollView>(R.id.start_layout)
                    startLayout.visibility = View.GONE
                    val loadingAnimation = findViewById<LoadingAnimation>(R.id.LoadingAnimationStart)
                    loadingAnimation.visibility = View.VISIBLE

                    lifecycleScope.launch {
                        val filePath = getFilePathFromUri(uri)
                        Log.d("DEBUG", "File path resolved: $filePath")

                        if (filePath != null) {
                            utilities.uploadRecording(filePath, "en") { transcription ->
                                Log.d("DEBUG", "Transcription result: $transcription")
                                runOnUiThread {
                                    val intent = Intent(this@StartActivity, TranscriptionDetailActivity::class.java)
                                    if (transcription != null) {
                                        intent.putExtra("EXTRA_TRANSCRIPTION_TEXT", transcription)
                                        intent.putExtra("EXTRA_FILE_PATH", filePath)
                                        intent.putExtra("EXTRA_LANGUAGE", "en")
                                    } else {
                                        intent.putExtra("EXTRA_ERROR_MESSAGE", "Transcription failed.")
                                        Log.e("DEBUG", "Transcription failed: Result was null")
                                    }
                                    startActivity(intent)
                                }
                            }
                        } else {
                            Log.e("DEBUG", "Failed to get file path from URI")
                            val intent = Intent(this@StartActivity, TranscriptionDetailActivity::class.java)
                            intent.putExtra("EXTRA_ERROR_MESSAGE", "Unable to get path")
                            startActivity(intent)
                        }
                    }
                } else {
                    Log.e("DEBUG", "Failed to get file name from URI")
                    val intent = Intent(this@StartActivity, TranscriptionDetailActivity::class.java)
                    intent.putExtra("EXTRA_ERROR_MESSAGE", "Unable to get file name")
                    startActivity(intent)
                }
            }
        }
    }

    private fun getFilePathFromUri(uri: Uri): String? {
        var inputStream = contentResolver.openInputStream(uri)
        return if (inputStream != null) {
            val tempFile = File.createTempFile("temp_audio", ".mp3", cacheDir)
            inputStream.copyTo(FileOutputStream(tempFile))
            tempFile.absolutePath
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

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun showKeyboard(editText: EditText) {
        editText.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }



    private fun checkConnectionAndServerStatus(serverUrl: String) {
        if (!isConnectedToInternet(this)) {
            tvConnectionStatus.text = "Brak połączenia z internetem"
            tvConnectionStatus.visibility = TextView.VISIBLE
        } else {
            if (!isServerAlive(serverUrl)) {
                tvConnectionStatus.text = "Serwer jest niedostępny"
                tvConnectionStatus.visibility = TextView.VISIBLE

            } else {
                tvConnectionStatus.visibility = TextView.GONE
            }
        }
    }



    override fun onResume() {
        super.onResume()
        if (!isReturningFromFileSelection) {
            val mainContent = findViewById<ScrollView>(R.id.start_layout)
            mainContent.visibility = View.VISIBLE

            val loadingAnimation = findViewById<LoadingAnimation>(R.id.LoadingAnimationStart)
            loadingAnimation.visibility = View.GONE
        } else {
            isReturningFromFileSelection = false
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(connectivityReceiver)
    }

}
