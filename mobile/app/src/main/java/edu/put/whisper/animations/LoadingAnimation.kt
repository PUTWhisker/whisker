package edu.put.whisper.animations

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import edu.put.whisper.R

class LoadingAnimation @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val dots = mutableListOf<TextView>()
    private val dotCount = 3
    private val animationDuration = 500L
    private val delayBetweenDots = 200L
    private val delayAfterCycle = 500L

    private val handler = Handler(Looper.getMainLooper())

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        val primaryDark = ContextCompat.getColor(context, R.color.primaryDark)

        for (i in 0 until dotCount) {
            val dot = TextView(context).apply {
                text = "●"
                textSize = 24f
                setTextColor(primaryDark)
                gravity = Gravity.CENTER
            }
            addView(dot)
            dots.add(dot)
        }

        startAnimationSequence()
    }

    private fun startAnimationSequence() {
        val animations = dots.map { createBounceAnimation() }
        runAnimationCycle(animations)
    }

    private fun createBounceAnimation(): Animation {
        return TranslateAnimation(0f, 0f, 0f, -50f).apply {
            duration = animationDuration
            repeatMode = Animation.REVERSE
            repeatCount = 1
        }
    }

    private fun runAnimationCycle(animations: List<Animation>) {
        for (i in animations.indices) {
            handler.postDelayed({
                dots[i].startAnimation(animations[i])
            }, i * delayBetweenDots)
        }

        // Przerwa po pełnym cyklu
        handler.postDelayed({
            runAnimationCycle(animations)
        }, dotCount * delayBetweenDots + animationDuration + delayAfterCycle)
    }
}
