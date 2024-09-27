package edu.put.whisper

import android.os.Handler
import android.widget.TextView

class Timer(private val handler: Handler, private val tvTimer: TextView) {
    private var startTime: Long = 0L
    private var elapsedTime: Long = 0L

    private val timerRunnable: Runnable = object : Runnable {
        override fun run() {
            val currentTime = System.currentTimeMillis()
            elapsedTime = currentTime - startTime
            val minutes = (elapsedTime / 60000).toInt() % 60
            val seconds = (elapsedTime / 1000).toInt() % 60
            val milliseconds = (elapsedTime % 1000) / 10
            tvTimer.text = String.format("%02d:%02d:%02d", minutes, seconds, milliseconds)
            handler.postDelayed(this, 10)
        }
    }

    fun startTimer() {
        startTime = System.currentTimeMillis()
        handler.post(timerRunnable)
    }

    fun stopTimer() {
        handler.removeCallbacks(timerRunnable)
    }

    fun resetTimer() {
        elapsedTime = 0L
        tvTimer.text = "00:00:00"
    }

    fun getElapsedTime(): Long {
        return elapsedTime
    }
}
