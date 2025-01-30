package edu.put.whisper

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    private lateinit var tvAboutWhisker: TextView
    private lateinit var logoWhisper: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        logoWhisper = findViewById(R.id.logoWhisper)

        logoWhisper.post {
            logoWhisper.alpha = 0f

            val fadeAnimator = ObjectAnimator.ofFloat(logoWhisper, "alpha", 1f)
            fadeAnimator.duration = 800
            fadeAnimator.startDelay = 300
            fadeAnimator.start()
        }
    }
}