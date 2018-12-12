package com.backflippedstudios.crypto_ta.customchartmods

import com.backflippedstudios.crypto_ta.frags.DetailedAnalysisFrag
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class VolumeBarDataSet( yVals: List<BarEntry>,label:String): BarDataSet(yVals,label) {
    override fun getEntryIndex(e: BarEntry?): Int {
        return mValues.indexOf(e)
    }

    override fun getColor(index: Int): Int {
        //Look at the data from DetailedAnalysisFrag to see if the volue was buy or sell volume.
        DetailedAnalysisFrag.data.taDataLock.lock()
        if (index < DetailedAnalysisFrag.data.all_ta[DetailedAnalysisFrag.data.saved_time_period].ticksDataArray.size) {
            val open = DetailedAnalysisFrag.data.all_ta[DetailedAnalysisFrag.data.saved_time_period].ticksDataArray[index].openPrice
            val close = DetailedAnalysisFrag.data.all_ta[DetailedAnalysisFrag.data.saved_time_period].ticksDataArray[index].closePrice
            DetailedAnalysisFrag.data.taDataLock.unlock()
            return when {
                open > close -> mColors[0]  // Red for sell
                close > open -> mColors[1]  // Green for buy
                else -> mColors[2]          // Blue for same
            }
        }
        else{
            DetailedAnalysisFrag.data.taDataLock.unlock()
            return mColors.last()
        }
    }

}