package edu.put.whisper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.grpc.authentication.AuthenticationClient
import io.grpc.authentication.TextHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class StartActivity : AppCompatActivity() {
    private lateinit var btnRecordActivity: Button
    private lateinit var btnChooseFile: Button
    private lateinit var tvSelectedFile: TextView
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
    private lateinit var btnHistory: TextView
    private lateinit var tvHistory: TextView
    private lateinit var llFileRecord: LinearLayout
    private val PICK_FILE_REQUEST_CODE = 1

    private lateinit var authClient: AuthenticationClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val serverUri = Uri.parse("http://100.80.80.156:50051/")
        authClient = AuthenticationClient(serverUri)

        btnRecordActivity = findViewById(R.id.btnRecordActivity)
        btnChooseFile = findViewById(R.id.btnChooseFile)
        tvSelectedFile = findViewById(R.id.tvSelectedFile)
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
        tvHistory = findViewById(R.id.tvHistory)
        llFileRecord = findViewById(R.id.llFileRecord)


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
                            btnLogin.visibility = View.GONE
                            btnRegister.visibility = View.GONE
                            btnHistory.visibility = View.VISIBLE

                            tvHello.text = "Hello $username"
                            tvHello.visibility = View.VISIBLE
                            btnLogout.visibility = View.VISIBLE


                            //showTranslationsHistory()
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
                type = "audio/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(
                Intent.createChooser(intent, "Choose a file"),
                PICK_FILE_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val fileName = getFileNameFromUri(uri)
                if (fileName != null) {
                    tvSelectedFile.text = "$fileName is being transcripted"
                } else {
                    Toast.makeText(this, "Unable to get file name", Toast.LENGTH_SHORT).show()
                }
            }
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

        GlobalScope.launch(Dispatchers.IO) {
            val history = authClient.GetTranslations()  // Załaduj historię
            withContext(Dispatchers.Main) {
                tvHistory.text = history.joinToString("\n")

            }
        }
    }
}
