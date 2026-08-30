package com.backflippedstudios.crypto_ta.customchartmods

import android.graphics.Matrix
import android.view.MotionEvent
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import android.view.View
import com.backflippedstudios.crypto_ta.Overlay
import com.backflippedstudios.crypto_ta.frags.DetailedAnalysisFrag


class MirrorChartGestureListener(val sourceKey: Overlay.Kind, var srcChart: Chart<*>, var destCharts:ArrayList<Chart<*>>): OnChartGestureListener{
    override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
//        println("GestureStart $sourceKey")
        DetailedAnalysisFrag.data.lastMainTouchChart = sourceKey
//        syncCharts()
    }

    override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
//        println("GestureEnd $sourceKey")
//        syncCharts()
    }

    override fun onChartLongPressed(me: MotionEvent?) {
//        println("Long Pressed $sourceKey")
        syncCharts()
    }

    override fun onChartDoubleTapped(me: MotionEvent?) {
//        println("Double tapped $sourceKey")
        syncCharts()
    }

    override fun onChartSingleTapped(me: MotionEvent?) {
//        println("Single Tapped $sourceKey")
        syncCharts()
    }

    override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {
//        println("Fling $sourceKey")
//        syncCharts()
    }

    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
//        println("Scale $sourceKey")
        syncCharts()
    }

    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
//        println("Translate $sourceKey")
        syncCharts()
    }

    private fun syncCharts() {

        val srcVals = FloatArray(9)
        var dstMatrix: Matrix
        val dstVals = FloatArray(9)

//        println("main: $sourceKey Syncing. Num of destCharts: ${destCharts.size}")
        var srcMatrix: Matrix? = srcChart.viewPortHandler.matrixTouch

        if(DetailedAnalysisFrag.data.lastMainTouchChart != sourceKey){
            srcMatrix = DetailedAnalysisFrag.data.matrixLocation
            srcMatrix?.getValues(srcVals)
            dstMatrix = srcChart.viewPortHandler.matrixTouch
            dstMatrix.getValues(dstVals)
            dstVals[Matrix.MSCALE_X] = srcVals[Matrix.MSCALE_X]
            dstVals[Matrix.MTRANS_X] = srcVals[Matrix.MTRANS_X]
            dstMatrix.setValues(dstVals)
            srcChart.viewPortHandler.refresh(dstMatrix, srcChart, true)
        }else{
            DetailedAnalysisFrag.data.matrixLocation = srcMatrix
            // get src chart translation matrix:
            srcMatrix?.getValues(srcVals)

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
}