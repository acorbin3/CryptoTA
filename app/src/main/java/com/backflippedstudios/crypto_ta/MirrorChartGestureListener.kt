package com.backflippedstudios.crypto_ta

import android.graphics.Matrix
import android.view.MotionEvent
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import android.graphics.Matrix.MTRANS_X
import android.graphics.Matrix.MSCALE_X
import android.view.View


class MirrorChartGestureListener(var srcChart: Chart<*>, var destCharts:ArrayList<Chart<*>>): OnChartGestureListener{
    override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
        syncCharts()
    }

    override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
        syncCharts()
    }

    override fun onChartLongPressed(me: MotionEvent?) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        syncCharts()
    }

    override fun onChartDoubleTapped(me: MotionEvent?) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        syncCharts()
    }

    override fun onChartSingleTapped(me: MotionEvent?) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        syncCharts()
    }

    override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {
        syncCharts()
    }

    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
        syncCharts()
    }

    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
        syncCharts()
    }

    private fun syncCharts() {
        val srcMatrix: Matrix = srcChart.viewPortHandler.matrixTouch
        val srcVals = FloatArray(9)
        var dstMatrix: Matrix
        val dstVals = FloatArray(9)
        MainActivity.data.matrixLocation = srcMatrix

        // get src chart translation matrix:
        srcMatrix.getValues(srcVals)
//        print("Matrix $srcVals")
//        for(va in srcVals){
//            print(" $va")
//        }
//        println()
        // apply X axis scaling and position to dst charts:
        for (dstChart in destCharts) {
            if (dstChart.visibility == View.VISIBLE) {
                dstMatrix = dstChart.viewPortHandler.matrixTouch
                dstMatrix.getValues(dstVals)
                dstVals[Matrix.MSCALE_X] = srcVals[Matrix.MSCALE_X]
                dstVals[Matrix.MTRANS_X] = srcVals[Matrix.MTRANS_X]
                dstMatrix.setValues(dstVals)
                dstChart.viewPortHandler.refresh(dstMatrix, dstChart, true)
            }
        }
    }
}