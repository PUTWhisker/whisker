package edu.put.whisper

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo: ImageView = findViewById(R.id.logo)
        val appName: TextView = findViewById(R.id.appName)

        logo.alpha = 1f
        appName.alpha = 1f

        val fadeOutLogo = ObjectAnimator.ofFloat(logo, "alpha", 1f, 0f).apply {
            duration = 1500
        }
        val fadeOutText = ObjectAnimator.ofFloat(appName, "alpha", 1f, 0f).apply {
            duration = 1500
        }
        val fadeOutSet = AnimatorSet().apply {
            playTogether(fadeOutLogo, fadeOutText)
        }

        Handler(Looper.getMainLooper()).postDelayed({

            fadeOutSet.start()

            fadeOutSet.doOnEnd {
                val intent = Intent(this, StartActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 2000)
    }
}
