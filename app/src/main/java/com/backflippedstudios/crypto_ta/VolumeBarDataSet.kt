package com.backflippedstudios.crypto_ta

import android.os.Parcel
import android.os.Parcelable
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class VolumeBarDataSet( yVals: List<BarEntry>,label:String): BarDataSet(yVals,label) {
    override fun getEntryIndex(e: BarEntry?): Int {
        return mValues.indexOf(e)
    }

    override fun getColor(index: Int): Int {
        //Look at the data from MainActivity to see if the volue was buy or sell volume.
        if (index < MainActivity.data.all_ta[MainActivity.data.saved_time_period].ticksDataArray.size) {
            val open = MainActivity.data.all_ta[MainActivity.data.saved_time_period].ticksDataArray[index].openPrice
            val close = MainActivity.data.all_ta[MainActivity.data.saved_time_period].ticksDataArray[index].closePrice

            return when {
                open > close -> mColors[0]  // Red for sell
                close > open -> mColors[1]  // Green for buy
                else -> mColors[2]          // Blue for same
            }
        }
        else{
            return mColors.last()
        }
    }

}