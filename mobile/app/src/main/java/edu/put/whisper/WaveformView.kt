package edu.put.whisper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View


class WaveformView(context: Context?, attrs: AttributeSet?): View(context, attrs) {

    private var paint = Paint()
    private var amplitudes = ArrayList<Float>()
    private var spikes = ArrayList<RectF>()

    private var radius = 6f
    private var w = 9f
    private var d = 6f

    private var sw = 0f
    private var sh = 300f
    private var maxSpikes = 0


    init {
        paint.color = Color.parseColor("#6A42C2")

        sw = resources.displayMetrics.widthPixels.toFloat()

        maxSpikes = (sw / (w+d)).toInt()
    }

    fun addAmplitude(amp: Float){
        var norm = Math.min(amp.toInt()/7, 300).toFloat()
        amplitudes.add(norm)


        spikes.clear()
        var amps = amplitudes.takeLast(maxSpikes)
        for (i in amps.indices){
            var left = sw - i*(w+d)
            var top = sh/2 - amps[i]/2
            var right = left + w
            var bottom = top + amps[i]
            spikes.add(RectF(left, top, right, bottom))
        }



        invalidate()
    }


    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        spikes.forEach{
            canvas.drawRoundRect(it, radius, radius, paint)
        }
    }

    fun clearAmplitudes() {
        amplitudes.clear()
        spikes.clear()
        invalidate()
    }

    fun getAmplitudes(): List<Float> {
        return amplitudes.toList()
    }

}