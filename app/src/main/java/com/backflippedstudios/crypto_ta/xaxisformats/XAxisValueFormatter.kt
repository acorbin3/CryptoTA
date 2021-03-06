package com.backflippedstudios.crypto_ta.xaxisformats

import android.os.Parcel
import android.os.Parcelable
import com.backflippedstudios.crypto_ta.data.DataSource
import com.backflippedstudios.crypto_ta.MainActivity
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import org.ta4j.core.Tick
import java.text.DecimalFormat

/**
 * Created by C0rbin on 12/7/2017.
 */
class XAxisValueFormatter() : IAxisValueFormatter, Parcelable {
    //Value is the x position of what was inserted
    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        try {
            if(MainActivity.data.all_ta[MainActivity.data.saved_time_period].ts != null
                    && MainActivity.data.all_ta[MainActivity.data.saved_time_period].ts?.isEmpty == false
                    && value.toInt() < MainActivity.data.all_ta[MainActivity.data.saved_time_period].ts?.tickCount!!
                    && MainActivity.data.all_ta[MainActivity.data.saved_time_period].ts?.tickCount!! > 0) {
    //            println("Value: $value Tick Count: ${MainActivity.data.all_ta[MainActivity.data.saved_time_period]?.ts?.tickCount}")


                    val curTick: Tick? = MainActivity.data.all_ta[MainActivity.data.saved_time_period].ts?.getTick(value.toInt())
                    val xLabelInterval: Int = 20
                    var timeStr = ""


                    //Format time
                    if (value.rem(xLabelInterval) == 0F) {


    //                val hour = curTick?.beginTime?.hour
    //                val min = curTick?.beginTime?.minute

    //                println(" i $value time: ${curTick?.beginTime.toString()}" )


                        when (MainActivity.data.saved_time_period) {
                            DataSource.Interval._1MIN.ordinal,  DataSource.Interval._3MIN.ordinal,
                            DataSource.Interval._5MIN.ordinal,  DataSource.Interval._15MIN.ordinal,
                            DataSource.Interval._30MIN.ordinal, DataSource.Interval._1HOUR.ordinal,
                            DataSource.Interval._2HOUR.ordinal, DataSource.Interval._4HOUR.ordinal,
                            DataSource.Interval._6HOUR.ordinal -> {
                                timeStr = formatMonthDateTime(curTick)
                            }

                            DataSource.Interval._12HOUR.ordinal, DataSource.Interval._1DAY.ordinal,
                            DataSource.Interval._3DAY.ordinal,   DataSource.Interval._1WEEK.ordinal-> {
                                timeStr = formatMonthDateYearTime(curTick)
                            }

                        }

                        return timeStr
                    } else {
                        return ""
                    }


            }
            else{
                println("TS was null or tickCount was bad")
                if(MainActivity.data.all_ta[MainActivity.data.saved_time_period].ts == null){
                    println("TS was null!!")
                }
                return ""
            }
        }catch (e: Exception){
            return ""
        }
    }

    constructor(parcel: Parcel) : this()

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<XAxisValueFormatter> {
        override fun createFromParcel(parcel: Parcel): XAxisValueFormatter {
            return XAxisValueFormatter(parcel)
        }

        override fun newArray(size: Int): Array<XAxisValueFormatter?> {
            return arrayOfNulls(size)
        }

        fun formatMonthDateTime(curTick: Tick?): String {
            var timeStr1 = ""
            val df = DecimalFormat("00")
            val monthStr = curTick?.beginTime?.month.toString() + "-" + curTick?.beginTime?.dayOfMonth.toString()
            timeStr1 = df.format(hour12H(curTick?.beginTime?.hour)).toString() + ":" + df.format(curTick?.beginTime?.minute).toString()
            var amPM = "AM"
            if(curTick?.beginTime?.hour!! > 11){
                amPM = "PM"
            }
            timeStr1 = "$timeStr1 $amPM\n$monthStr"
            return timeStr1
        }

        fun hour12H(hour: Int?): Int{
            var convertedHour: Int? = hour?.plus(1) //This is because time starts at 00:00

            if(convertedHour!! >12){
                return convertedHour.minus(12)
            }else{
                return convertedHour
            }
        }

        fun formatMonthDateYearTime(curTick: Tick?): String {
            var timeStr1 = ""
            val df = DecimalFormat("00")
            val monthStr = curTick?.beginTime?.month.toString() +
                    "-" + curTick?.beginTime?.dayOfMonth.toString() +
                    "-" + curTick?.beginTime?.year
            timeStr1 = df.format(CREATOR.hour12H(curTick?.beginTime?.hour)).toString() + ":" + df.format(curTick?.beginTime?.minute).toString()
            var amPM = "AM"
            if(curTick?.beginTime?.hour!! > 11){
                amPM = "PM"
            }
            timeStr1 = "$timeStr1 $amPM\n$monthStr"
            return timeStr1
        }
    }
}