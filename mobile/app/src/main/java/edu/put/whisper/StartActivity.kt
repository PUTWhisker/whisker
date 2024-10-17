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
    private val PICK_FILE_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

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


        val bottomSheetL: LinearLayout = findViewById(R.id.bottomSheetL)
        bottomSheetLogin = findViewById(R.id.bottomSheetLogin)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetL)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        btnRegister.setOnClickListener{
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetLogin.visibility = View.VISIBLE
            bottomSheetTitle.setText("Register")
            repeatPasswordInput.visibility = View.VISIBLE

            // TODO dwa razy wpisać hasło
            // TODO lista zeby przeglądać historię
            // funkcja ktora przerzuca z inne threada na main thread zeby layout zmieniac
        }

        btnLogin.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetLogin.visibility = View.VISIBLE
            bottomSheetTitle.setText("Log in")
        }
        btnCancelLog.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetLogin.visibility = View.GONE

        }

        btnSubmit.setOnClickListener {
            val password = passwordInput.text.toString()
            val repeatPassword = repeatPasswordInput.text.toString()
            val title = bottomSheetTitle.text.toString()
            if(title == "Register") {

                // sprawdzenie czy hasła są takie same
                if (password == repeatPassword) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    bottomSheetLogin.visibility = View.GONE
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()

                    loginInput.text.clear()
                    passwordInput.text.clear()
                    repeatPasswordInput.text.clear()
                    repeatPasswordInput.visibility = View.GONE
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
            startActivityForResult(Intent.createChooser(intent, "Choose a file"), PICK_FILE_REQUEST_CODE)
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
}
