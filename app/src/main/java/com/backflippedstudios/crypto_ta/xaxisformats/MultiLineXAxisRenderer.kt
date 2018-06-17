package com.backflippedstudios.crypto_ta.xaxisformats

import android.graphics.Canvas
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.renderer.XAxisRenderer
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler

class MultiLineXAxisRenderer(private val shiftInsideGraph: Boolean, viewPortHandler: ViewPortHandler?, xAxis: XAxis?, trans: Transformer?) : XAxisRenderer(viewPortHandler, xAxis, trans) {
    override fun drawLabel(c: Canvas?, formattedLabel: String?, x: Float, y: Float, anchor: MPPointF?, angleDegrees: Float) {
        val line = formattedLabel?.split("\n")
        var offset = 0F
        if(this.shiftInsideGraph){
            offset = mAxisLabelPaint.textSize
        }
//        println(line.toString() + " " +  line?.size + " " + mAxisLabelPaint.textSize + " shift:" + this.shiftInsideGraph )
        Utils.drawXAxisValue(c, line?.get(0), x , y - offset, mAxisLabelPaint , anchor, angleDegrees)
        if (line?.size!! > 1) {
            Utils.drawXAxisValue(c, line[1], x  , y + mAxisLabelPaint.textSize - offset, mAxisLabelPaint, anchor, angleDegrees)
        }

    }
}