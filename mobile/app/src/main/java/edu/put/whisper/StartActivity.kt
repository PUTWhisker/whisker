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
import android.util.Log
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import edu.put.whisper.animations.LoadingAnimation
import edu.put.whisper.utils.Utilities
import edu.put.whisper.utils.isServerAlive
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
    private lateinit var logoWhisper: ImageView
    private lateinit var tvSelectedFile: TextView
    private lateinit var tvTranscriptedFile: TextView
    private lateinit var bottomSheetBehaviorLogin: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetBehaviorRegister: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetLogin: View
    private lateinit var bottomSheetRegister: View
    private lateinit var bottomSheetTitle: TextView
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var btnCancelLog: Button
    private lateinit var btnLoginConfirm: Button

    private lateinit var btnRegisterConfirm: Button
    private lateinit var registerPasswordInput: EditText
    private lateinit var repeatPasswordInput: EditText
    private lateinit var registerNickInput: EditText
    private lateinit var btnCancelRegister: Button


    private lateinit var passwordInput: EditText
    private lateinit var loginInput: EditText
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
    private lateinit var btnAbout: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val serverUrl = getString(R.string.server_url)
        Log.d("ServerStatus", "Server URL: $serverUrl")
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus)

        connectivityReceiver = object : BroadcastReceiver() {
            private val handler = Handler(Looper.getMainLooper())
            private var lastCheckTime = 0L

            override fun onReceive(context: Context?, intent: Intent?) {
                val noConnectivity = intent?.getBooleanExtra(
                    ConnectivityManager.EXTRA_NO_CONNECTIVITY,
                    false
                ) == true
                if (noConnectivity) {
                    tvConnectionStatus.text = "Brak połączenia z internetem"
                    tvConnectionStatus.visibility = View.VISIBLE
                } else {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastCheckTime > 3000) {
                        lastCheckTime = currentTime
                        lifecycleScope.launch {
                            val isAlive = isServerAlive(serverUrl)
                            updateUI(isAlive)
                        }
                    }
                }
            }
        }


        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(connectivityReceiver, filter)

        lifecycleScope.launch {
            val isAlive = isServerAlive(serverUrl)
            updateUI(isAlive)
        }

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
        btnRegister = findViewById(R.id.btnRegister)
        btnLogin = findViewById(R.id.btnLogin)
        btnCancelLog = findViewById(R.id.btnCancelLog)
        btnLoginConfirm = findViewById(R.id.btnLoginConfirm)

        btnRegisterConfirm = findViewById(R.id.btnRegisterIn)
        registerPasswordInput = findViewById(R.id.RegisterPasswordInput)
        registerNickInput = findViewById(R.id.RegisterNickInput)
        repeatPasswordInput = findViewById(R.id.repeatPasswordInput)
        btnCancelRegister = findViewById(R.id.btnCancelRegister)

        bottomSheetTitle = findViewById(R.id.bottomSheetTitle)
        btnHistory = findViewById(R.id.btnHistory)
        btnLogout = findViewById(R.id.btnLogout)
        btnBack = findViewById(R.id.btnBack)
        rvTranscriptions = findViewById(R.id.rvTranscriptions)
        llFileRecord = findViewById(R.id.llFileRecord)
        logoWhisper = findViewById(R.id.logoWhisper)
        rvTranscriptions.layoutManager = LinearLayoutManager(this)
        btnAbout = findViewById(R.id.btnAbout)

        // bottom popup LOGIN
        val bottomSheetL: LinearLayout = findViewById(R.id.bottomSheetL)
        bottomSheetLogin = findViewById(R.id.bottomSheetLogin)

        bottomSheetBehaviorLogin = BottomSheetBehavior.from(bottomSheetL)
        bottomSheetBehaviorLogin.peekHeight = 0
        bottomSheetBehaviorLogin.state = BottomSheetBehavior.STATE_COLLAPSED

        // bottom popup REGISTER
        val bottomSheetR: LinearLayout = findViewById(R.id.bottomSheetR)
        bottomSheetRegister = findViewById(R.id.bottomSheetRegister)

        bottomSheetBehaviorRegister = BottomSheetBehavior.from(bottomSheetR)
        bottomSheetBehaviorRegister.peekHeight = 0
        bottomSheetBehaviorRegister.state = BottomSheetBehavior.STATE_COLLAPSED

        btnAbout.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

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
                btnLoginConfirm.performClick()
                true
            } else {
                false
            }
        }


        btnRegister.setOnClickListener {
            bottomSheetBehaviorRegister.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetRegister.visibility = View.VISIBLE
            //showKeyboard(loginInput)

        }

        btnCancelRegister.setOnClickListener { view ->
            bottomSheetBehaviorRegister.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetRegister.visibility = View.GONE
            registerNickInput.text?.clear()
            registerPasswordInput.text?.clear()

            val inputMethodManager = view.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }

        btnLogin.setOnClickListener {
            bottomSheetBehaviorLogin.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetLogin.visibility = View.VISIBLE
            //showKeyboard(loginInput)
        }
        btnCancelLog.setOnClickListener { view ->
            bottomSheetBehaviorLogin.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetLogin.visibility = View.GONE
            loginInput.text?.clear()
            passwordInput.text?.clear()

            val inputMethodManager = view.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }

        btnRegisterConfirm.setOnClickListener {
            val username = registerNickInput.text.toString()
            val password = registerPasswordInput.text.toString()
            val repeatPassword = repeatPasswordInput.text.toString()
            if (password == repeatPassword) {
                GlobalScope.launch(Dispatchers.IO) {
                    val success = authClient.Register(username, password)
                    withContext(Dispatchers.Main) {
                        if (success) {
                            bottomSheetBehaviorRegister.state = BottomSheetBehavior.STATE_COLLAPSED
                            bottomSheetRegister.visibility = View.GONE
                            Toast.makeText(
                                this@StartActivity,
                                getString(R.string.registration_successful_you_can_now_log_in),
                                Toast.LENGTH_SHORT
                            ).show()
                            registerNickInput.text.clear()
                            registerPasswordInput.text.clear()
                            repeatPasswordInput.text.clear()
                        } else {
                            Toast.makeText(
                                this@StartActivity,
                                getString(R.string.registration_failed_try_again),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.passwords_do_not_match_please_try_again),
                    Toast.LENGTH_SHORT
                ).show()
                registerPasswordInput.text.clear()
                repeatPasswordInput.text.clear()
            }
        }

        btnLoginConfirm.setOnClickListener {
            val username = loginInput.text.toString()
            val password = passwordInput.text.toString()
            GlobalScope.launch(Dispatchers.IO) {
                val success = authClient.Login(username, password)
                withContext(Dispatchers.Main) {
                    if (success) {
                        hideKeyboard()
                        isUserLoggedIn = true
                        bottomSheetBehaviorLogin.state = BottomSheetBehavior.STATE_COLLAPSED
                        bottomSheetLogin.visibility = View.GONE
                        Toast.makeText(
                            this@StartActivity,
                            getString(R.string.login_successful), Toast.LENGTH_SHORT
                        ).show()
                        utilities.setVisibility(View.GONE, btnLogin, btnRegister)
                        utilities.setVisibility(View.VISIBLE, btnLogout)
                        val icHistoryImageView: ImageView = findViewById(R.id.ic_history)
                        val icArrowImageView: ImageView = findViewById(R.id.ic_arrow)
                        icHistoryImageView.backgroundTintList =
                            ContextCompat.getColorStateList(this@StartActivity, R.color.primaryDark)
                        icHistoryImageView.imageTintList =
                            ContextCompat.getColorStateList(this@StartActivity, R.color.white)
                        findViewById<TextView>(R.id.historyText).text = "History"
                        findViewById<TextView>(R.id.historyText).setTextColor(
                            ContextCompat.getColor(
                                this@StartActivity,
                                R.color.primaryDark
                            )
                        )
                        icArrowImageView.imageTintList =
                            ContextCompat.getColorStateList(this@StartActivity, R.color.primaryDark)


                    } else {
                        Toast.makeText(
                            this@StartActivity,
                            getString(R.string.login_failed_try_again), Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        btnHistory.setOnClickListener{
            if(isUserLoggedIn){
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
            } else {
                Toast.makeText(this,
                    getString(R.string.please_log_in_to_view_history), Toast.LENGTH_SHORT).show()
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
            utilities.setVisibility(View.GONE, btnLogout)
            utilities.setVisibility(View.INVISIBLE, rvTranscriptions)
            isUserLoggedIn = false
            loginInput.text.clear()
            passwordInput.text.clear()
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

                val contentResolver = contentResolver
                val mimeType = contentResolver.getType(uri)
                // sprawdzenie czy uzytkownik wgrywa odpowiedni typ pliku
                if (mimeType?.startsWith("audio/") != true) {
                    Toast.makeText(this, getString(R.string.wrong_file_format), Toast.LENGTH_SHORT).show()
                    return
                }
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
    private fun updateUI(isServerAvailable: Boolean) {
        tvConnectionStatus.text = if (isServerAvailable) {
            "Serwer dostępny"
        } else {
            "Serwer niedostępny"
        }
        tvConnectionStatus.visibility = View.VISIBLE
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
