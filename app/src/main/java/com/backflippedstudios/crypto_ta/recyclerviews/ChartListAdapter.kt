package com.backflippedstudios.crypto_ta.recyclerviews

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Color
import android.graphics.Matrix
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import com.backflippedstudios.crypto_ta.*
import com.backflippedstudios.crypto_ta.dropdownmenus.OverlayAdapter
import com.backflippedstudios.crypto_ta.xaxisformats.XAxisValueFormatter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.CombinedChart
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import kotlinx.android.synthetic.main.activity_main.*
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import org.ta4j.core.Tick
import java.util.jar.Attributes


class ChartListAdapter(var context: Context, var list: ArrayList<ChartStatusData>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var parentHeight: Int? = 0

    class CombinedViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var chart: CombinedChart = view.findViewById(R.id.combined_chart)
        var logo: ImageView = view.findViewById(R.id.main_logo)
    }

    object data {
        var charts: HashMap<ChartStatusData.Type, Any> = HashMap()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        lateinit var recyclerViewHolder: RecyclerView.ViewHolder
        println("Creating charts")
        parentHeight = parent?.height

        val view = LayoutInflater.from(parent?.context).inflate(R.layout.combinedchart_item_view, parent, false) as View
        recyclerViewHolder = CombinedViewHolder(view)
        if (parent != null) {
            var sizeRatio = 0.0F
            if(viewType == ChartStatusData.Type.MAIN_CHART.ordinal){
                sizeRatio = calculateMainChartRatio()
            }else{
                sizeRatio = calculateOtherChartRatio()
            }
            recyclerViewHolder.itemView.minimumHeight = (parent.height * sizeRatio).toInt()
        }
        return recyclerViewHolder
    }

    private fun calculateMainChartRatio(): Float {
        var sizeRatio = 1F
        if (MainActivity.data.chartList.size == 2) {
            sizeRatio = 0.8F
        }
        else if (MainActivity.data.chartList.size == 3) {
            sizeRatio = 0.65F
        }
        else if (MainActivity.data.chartList.size == 4) {
            sizeRatio = 0.58F
        }
        return sizeRatio
    }

    private fun calculateOtherChartRatio(): Float {
        var sizeRatio = 1F
        if (MainActivity.data.chartList.size == 2) {
            sizeRatio = 0.2F
        }
        else if (MainActivity.data.chartList.size == 3) {
            sizeRatio = 0.175F
        }
        else if (MainActivity.data.chartList.size == 4) {
            sizeRatio = 0.14F
        }
        return sizeRatio
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].type.ordinal
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        println("Binding charts ${list[position].type.name}- ${list[position].status}")
        val srcVals = FloatArray(9)
        val dstMatrix: Matrix
        val dstVals = FloatArray(9)
        val combinedViewHolder = holder as CombinedViewHolder
        combinedViewHolder.logo.visibility = View.INVISIBLE

        if(list[position].type == ChartStatusData.Type.MAIN_CHART) {
            combinedViewHolder.logo.visibility = View.VISIBLE
            combinedViewHolder.itemView.minimumHeight = (this.parentHeight?.times(calculateMainChartRatio()))?.toInt() ?: 0
        }
        else{
            combinedViewHolder.itemView.minimumHeight = (this.parentHeight?.times(calculateOtherChartRatio()))?.toInt() ?: 0
        }
        println("this.parentHeight: ${this.parentHeight} + minHeigth: ${combinedViewHolder.itemView.minimumHeight}")
        combinedViewHolder.itemView.layoutParams.height = combinedViewHolder.itemView.minimumHeight
        when (list[position].status) {
            ChartStatusData.Status.LOADING -> {
                combinedViewHolder.chart.clear()
                combinedViewHolder.chart.setNoDataText("Retrieving Data from web")
            }
            ChartStatusData.Status.UPDATE_FAILED -> {
                combinedViewHolder.chart.setNoDataText("Coin failed, please choose another coin/exchange/currency")
            }
            ChartStatusData.Status.INTERNET_OUT -> {
                combinedViewHolder.chart.setNoDataText("Not connected to the internet")
            }
            ChartStatusData.Status.UPDATE_CANDLESTICKS -> {
                ChartStyle(context).updateCandlestickGraph(
                        MainActivity.data.all_ta[MainActivity.data.saved_time_period],
                        combinedViewHolder.chart)
                //Next line syncs the inital zoom on all charts
                MainActivity.data.matrixLocation = combinedViewHolder.chart.viewPortHandler.matrixTouch
            }
            ChartStatusData.Status.UPDATE_OVERLAYS -> {
                println("Updating Overlays from ChartList Adapter")
                combinedViewHolder.chart.fillInbetweenLines = true
                ChartStyle(context).updateOverlays(
                        OverlayAdapter.data.list,
                        MainActivity.data.all_ta[MainActivity.data.saved_time_period],
                        combinedViewHolder.chart)

            }
            ChartStatusData.Status.INITIAL_LOAD ->{
                when(list[position].type){
                    ChartStatusData.Type.VOLUME_CHART ->{
                        ChartStyle(context).updateVolumeGraph(
                                MainActivity.data.all_ta[MainActivity.data.saved_time_period],
                                combinedViewHolder.chart,
                                true
                        )
                    }
                    ChartStatusData.Type.AROON_UP_DOWN_CHART,ChartStatusData.Type.AROON_OSCI_CHART ->{
                        combinedViewHolder.chart.clear()
                        combinedViewHolder.chart.setNoDataText("Calculating data")
                    }
                }
            }
            ChartStatusData.Status.UPDATE_CHART -> {

                when(list[position].type){
                    ChartStatusData.Type.VOLUME_CHART ->{
                        ChartStyle(context).updateVolumeGraph(
                                MainActivity.data.all_ta[MainActivity.data.saved_time_period],
                                combinedViewHolder.chart,
                                true
                        )
                    }
                    ChartStatusData.Type.AROON_UP_DOWN_CHART,ChartStatusData.Type.AROON_OSCI_CHART ->{
                        var allLineGraphStyle: ArrayList<ChartStyle.LineGraphStyle> = ArrayList()
                        if(list[position].type == ChartStatusData.Type.AROON_OSCI_CHART){
                            if(MainActivity.data.all_ta[MainActivity.data.saved_time_period].aroonOscillatorData.size > 0) {
                                allLineGraphStyle.add(ChartStyle.LineGraphStyle(MainActivity.data.all_ta[MainActivity.data.saved_time_period].aroonOscillatorData,
                                        ChartStyle.LineStyle(
                                                lineLabel = "Aroon Oscillator",
                                                lineColor = ContextCompat.getColor(context, R.color.md_cyan_500),
                                                filled = true,
                                                filledColor = ContextCompat.getColor(context, R.color.md_cyan_100)
                                        )
                                ))
                            }
                            else{
                                println("Somehow we got zero size for arronOscillatorData")
                            }

                        }
                        else if (list[position].type == ChartStatusData.Type.AROON_UP_DOWN_CHART){
                            combinedViewHolder.chart.fillInbetweenLines = false

                            allLineGraphStyle.add(ChartStyle.LineGraphStyle(MainActivity.data.all_ta[MainActivity.data.saved_time_period].aroonUpIndicatorData,
                                    ChartStyle.LineStyle(
                                            lineLabel = "Aroon Up",
                                            lineColor = ContextCompat.getColor(context,R.color.md_green_300)
                                    )
                            ))
                            allLineGraphStyle.add(ChartStyle.LineGraphStyle(MainActivity.data.all_ta[MainActivity.data.saved_time_period].aroonDownIndicatorData,
                                    ChartStyle.LineStyle(
                                            lineLabel = "Aroon Down",
                                            lineColor = ContextCompat.getColor(context,R.color.md_red_300)
                                    )
                            ))
                        }

                        ChartStyle(context).updateLineGraph(
                                allLineGraphStyle,
                                combinedViewHolder.chart
                        )
                    }
                }
            }
        }

        //Update valueIndex of graph
        MainActivity.data.matrixLocation?.getValues(srcVals)
        dstMatrix = combinedViewHolder.chart.viewPortHandler.matrixTouch
        dstMatrix.getValues(dstVals)
        dstVals[Matrix.MSCALE_X] = srcVals[Matrix.MSCALE_X]
        dstVals[Matrix.MTRANS_X] = srcVals[Matrix.MTRANS_X]
        dstMatrix.setValues(dstVals)
        combinedViewHolder.chart.setViewPortOffsets(0F, 0F, 0F, 0F)
        combinedViewHolder.chart.viewPortHandler.refresh(dstMatrix, combinedViewHolder.chart, true)

        data.charts[list[position].type] = combinedViewHolder.chart
        linkGestures()

        combinedViewHolder.chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {

                if (e != null) {
                    hideIndicatorsList(combinedViewHolder.chart.rootView)

                    for(item in list){
                        var updatedText = ""
                        val legendList: MutableList<LegendEntry> = arrayListOf()
                        when(item.type){
                            ChartStatusData.Type.MAIN_CHART ->{
                                if( MainActivity.data.all_ta[MainActivity.data.saved_time_period].candlestickData.size >= e.x.toInt()) {
                                    val values = MainActivity.data.all_ta[MainActivity.data.saved_time_period].candlestickData[e.x.toInt()]
                                    updatedText = "Candle Stick O ${values.open} H ${values.high} L ${values.low} C ${values.close}"
                                    legendList.add(LegendEntry(updatedText, Legend.LegendForm.NONE, 9f, Float.NaN, null, Color.WHITE))
                                }
                                for(item in OverlayAdapter.data.list){
                                    if(item.selected){
                                        when(item.kind){
                                            Overlay.Kind.Bollinger_Bands ->{
                                                if(MainActivity.data.all_ta[MainActivity.data.saved_time_period].bbMiddleData.size >= e.x.toInt()) {
                                                    val valuesMiddle = MainActivity.data.all_ta[MainActivity.data.saved_time_period].bbMiddleData[e.x.toInt()]
                                                    val valuesUpper = MainActivity.data.all_ta[MainActivity.data.saved_time_period].bbUpperData[e.x.toInt()]
                                                    val valuesLower = MainActivity.data.all_ta[MainActivity.data.saved_time_period].bbLowerData[e.x.toInt()]
                                                    updatedText = "Bollinger Bands - M ${valuesMiddle.y} "
                                                    var color = item.allIndicatorInfo[0].color
                                                    legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))
                                                    updatedText = "Bollinger Bands -U ${valuesUpper.y}"
                                                    color = item.allIndicatorInfo[1].color
                                                    legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))
                                                    updatedText = "Bollinger Bands - L ${valuesLower.y} "
                                                    color = item.allIndicatorInfo[2].color
                                                    legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))
                                                }
                                            }

                                            Overlay.Kind.Keltner_Channel->{
                                                if(!MainActivity.data.all_ta[MainActivity.data.saved_time_period].keltnerChannelLowerData.isEmpty()) {
                                                    val offset = MainActivity.data.all_ta[MainActivity.data.saved_time_period].keltnerChannelLowerData.last().x - MainActivity.data.all_ta[MainActivity.data.saved_time_period].keltnerChannelLowerData.size
                                                    val index = (e.x.toInt() - offset).toInt()
                                                    if (MainActivity.data.all_ta[MainActivity.data.saved_time_period].keltnerChannelLowerData.last().x >= e.x.toInt()
                                                            && index > 0
                                                            && index < MainActivity.data.all_ta[MainActivity.data.saved_time_period].keltnerChannelLowerData.size) {
                                                        //We need to offset becuase the x value is not alignt. And this is because we didnt add some values on the front end
                                                        // of the graph becuase they made the graph not visible.

                                                        val valuesMiddle = MainActivity.data.all_ta[MainActivity.data.saved_time_period].keltnerChannelMiddleData[index]
                                                        val valuesUpper = MainActivity.data.all_ta[MainActivity.data.saved_time_period].keltnerChannelUpperData[index]
                                                        val valuesLower = MainActivity.data.all_ta[MainActivity.data.saved_time_period].keltnerChannelLowerData[index]
                                                        updatedText = "Keltner Channel - M ${valuesMiddle.y}"
                                                        var color = item.allIndicatorInfo[0].color
                                                        legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))
                                                        updatedText = "Keltner Channel - U ${valuesUpper.y}"
                                                        color = item.allIndicatorInfo[1].color
                                                        legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))
                                                        updatedText = "Keltner Channel - L ${valuesLower.y} "
                                                        color = item.allIndicatorInfo[2].color
                                                        legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))
                                                    }
                                                }
                                            }

                                            Overlay.Kind.Simple_Moving_Avg->{
                                                if(MainActivity.data.all_ta[MainActivity.data.saved_time_period].smaData.size >= e.x.toInt()) {
                                                    val values = MainActivity.data.all_ta[MainActivity.data.saved_time_period].smaData[e.x.toInt()]
                                                    updatedText = "Simple MA ${values.y} "
                                                    val color = item.allIndicatorInfo[0].color
                                                    legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))
                                                }
                                            }

                                            Overlay.Kind.Exponential_MA->{
                                                if(MainActivity.data.all_ta[MainActivity.data.saved_time_period].emaData.size >= e.x.toInt()) {
                                                    val values = MainActivity.data.all_ta[MainActivity.data.saved_time_period].emaData[e.x.toInt()]
                                                    updatedText = "Exponential MA ${values.y} "
                                                    val color = item.allIndicatorInfo[0].color
                                                    legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))
                                                }
                                            }

                                            Overlay.Kind.Parabolic_SAR->{
                                                if(MainActivity.data.all_ta[MainActivity.data.saved_time_period].parabolicSAR_Data.size >= e.x.toInt()) {
                                                    val values = MainActivity.data.all_ta[MainActivity.data.saved_time_period].parabolicSAR_Data[e.x.toInt()]
                                                    updatedText = "Parabolic SAR ${values.y} "
                                                    val color = item.allIndicatorInfo[0].color
                                                    legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))
                                                }
                                            }

                                            Overlay.Kind.Chandelier_Exit->{
                                                if(MainActivity.data.all_ta[MainActivity.data.saved_time_period].chandelierExitData.size >= e.x.toInt()) {
                                                    val values = MainActivity.data.all_ta[MainActivity.data.saved_time_period].chandelierExitData[e.x.toInt()]
                                                    updatedText = "Chandelier Exit ${values.y} "
                                                    val color = item.allIndicatorInfo[0].color
                                                    legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))
                                                }
                                            }

                                            Overlay.Kind.Ichimoku_Cloud->{
                                                if(MainActivity.data.all_ta[MainActivity.data.saved_time_period].ichCloud_BaseData.size > 0) {
                                                    var valuesBase = Entry()
                                                    if(MainActivity.data.all_ta[MainActivity.data.saved_time_period].ichCloud_BaseData.size >= e.x.toInt() ) {
                                                        valuesBase = MainActivity.data.all_ta[MainActivity.data.saved_time_period].ichCloud_BaseData[e.x.toInt()]
                                                    }
                                                    var valuesConv = Entry()
                                                    if(MainActivity.data.all_ta[MainActivity.data.saved_time_period].ichCloud_ConversionData.size >= e.x.toInt() ) {
                                                        valuesConv = MainActivity.data.all_ta[MainActivity.data.saved_time_period].ichCloud_ConversionData[e.x.toInt()]
                                                    }
                                                    var valuesLagg = Entry()
                                                    if(MainActivity.data.all_ta[MainActivity.data.saved_time_period].ichCloud_LaggingData.size >= e.x.toInt() ){
                                                        valuesLagg = MainActivity.data.all_ta[MainActivity.data.saved_time_period].ichCloud_LaggingData[e.x.toInt()]
                                                    }
                                                    var valuesLeadA = Entry()
                                                    var laggingPeriod = 0
                                                    for(item in OverlayAdapter.data.list){
                                                        if(item.kind == Overlay.Kind.Ichimoku_Cloud){
                                                            laggingPeriod = item.values[item.laggingPeriod].value.toInt()
                                                        }
                                                    }
                                                    var index = e.x.toInt() - laggingPeriod
                                                    if(MainActivity.data.all_ta[MainActivity.data.saved_time_period].ichCloud_LeadingAData.size >= index && index > 0 ) {
                                                        valuesLeadA = MainActivity.data.all_ta[MainActivity.data.saved_time_period].ichCloud_LeadingAData[index]
                                                    }
                                                    var valuesLeadB = Entry()
                                                    if(MainActivity.data.all_ta[MainActivity.data.saved_time_period].ichCloud_LeadingBData.size >= index && index > 0) {

                                                        valuesLeadB = MainActivity.data.all_ta[MainActivity.data.saved_time_period].ichCloud_LeadingBData[index]
                                                    }

                                                    updatedText = "Ich Cloud - LeadA ${valuesLeadA.y}"
                                                    var color = item.allIndicatorInfo[0].color
                                                    legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))
                                                    updatedText = "Ich Cloud - LeadB ${valuesLeadB.y}"
                                                    color = item.allIndicatorInfo[1].color
                                                    legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))
                                                    updatedText = "Ich Cloud - Conv ${valuesConv.y}"
                                                    color = item.allIndicatorInfo[2].color
                                                    legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))
                                                    updatedText = "Ich Cloud - Base ${valuesBase.y}"
                                                    color = item.allIndicatorInfo[3].color
                                                    legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))
                                                    updatedText = "Ich Cloud - Lagg ${valuesLagg.y}"
                                                    color = item.allIndicatorInfo[4].color
                                                    legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))

                                                }
                                            }

                                            Overlay.Kind.ZigZag->{
                                                if(MainActivity.data.all_ta[MainActivity.data.saved_time_period].zigZagData.size >= 0) {
                                                    lateinit var candidateBefore: Entry
                                                    lateinit var candidateAfter: Entry
                                                    var foundAfter = false
                                                    for(entry in MainActivity.data.all_ta[MainActivity.data.saved_time_period].zigZagData){
                                                        if(entry.x > e.x.toInt()){
                                                            candidateAfter = entry
                                                            foundAfter = true
                                                            break
                                                        }
                                                        candidateBefore = entry
                                                    }

                                                    if(foundAfter) {
                                                        val slope = (candidateAfter.y - candidateBefore.y) / (candidateAfter.x - candidateBefore.x)
//                                                    print("x:${e.x} y:${e.y} before: x:${candidateBefore.x} y:${candidateBefore.y}  after:x:${candidateAfter.x} y:${candidateAfter.y} slope:$slope")
                                                        val b = candidateAfter.y - (slope * candidateAfter.x)
                                                        val yIntercept = (slope * e.x) + b
//                                                    println("Zig Zag yIntercept: $yIntercept")
                                                        updatedText = "Zig Zag $yIntercept "
                                                        val color = item.allIndicatorInfo[0].color
                                                        legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))
                                                    }
                                                }
                                            }

                                            Overlay.Kind.Exponential_MA_Ribbon->{
                                                if(MainActivity.data.all_ta[MainActivity.data.saved_time_period].emaRibbonData.size >= 0) {
                                                    MainActivity.data.all_ta[MainActivity.data.saved_time_period].emaRibbonData.forEachIndexed { index, arrayList ->
                                                        if(arrayList.size < e.x.toInt()) {
                                                            val values = arrayList[e.x.toInt()]
                                                            updatedText = "EMA $index ${values.y} "
                                                            val color = item.allIndicatorInfo[index].color
                                                            legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))
                                                        }

                                                    }

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            ChartStatusData.Type.VOLUME_CHART ->{
                                val values = MainActivity.data.all_ta[MainActivity.data.saved_time_period].volumeBarData[e.x.toInt()]
                                 updatedText = "V ${values.y}"
                                legendList.add(LegendEntry(updatedText, Legend.LegendForm.NONE, 9f, Float.NaN, null, Color.WHITE))
                            }
                            ChartStatusData.Type.AROON_OSCI_CHART ->{
                                if(!MainActivity.data.all_ta[MainActivity.data.saved_time_period].aroonOscillatorData.isEmpty()) {
                                    val values = MainActivity.data.all_ta[MainActivity.data.saved_time_period].aroonOscillatorData[e.x.toInt()]
                                    var color = 0
                                    for(item in OverlayAdapter.data.list){
                                        if(item.kind == Overlay.Kind.AroonOsci){
                                            color = item.allIndicatorInfo[0].color
                                        }
                                    }
                                    updatedText = "Aroon Oscillator ${values.y}"
                                    legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))
                                }
                            }
                            ChartStatusData.Type.AROON_UP_DOWN_CHART ->{
                                if(!MainActivity.data.all_ta[MainActivity.data.saved_time_period].aroonUpIndicatorData.isEmpty()) {
                                    val valuesDown = MainActivity.data.all_ta[MainActivity.data.saved_time_period].aroonDownIndicatorData[e.x.toInt()]
                                    val valuesUp = MainActivity.data.all_ta[MainActivity.data.saved_time_period].aroonUpIndicatorData[e.x.toInt()]
                                    var colorDown = 0
                                    var colorUp = 0
                                    for(item in OverlayAdapter.data.list){
                                        if(item.kind == Overlay.Kind.AroonUpDown){
                                            colorUp = item.allIndicatorInfo[0].color
                                            colorDown = item.allIndicatorInfo[1].color
                                        }
                                    }
                                    updatedText = "Aroon Up ${valuesUp.y}"
                                    legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, colorUp))
                                    updatedText = "Aroon Down ${valuesDown.y}"
                                    legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, colorDown))
                                }
                            }
                        }
//                        print(updatedText)

                        if(legendList.isNotEmpty()) {
                            var timeStr = "date"
                            if(MainActivity.data.all_ta[MainActivity.data.saved_time_period].ts?.endIndex!! > e.x.toInt()){
                                val curTick: Tick? = MainActivity.data.all_ta[MainActivity.data.saved_time_period].ts?.getTick(e.x.toInt())

                                when (MainActivity.data.saved_time_period) {
                                    DataSource.Interval._1MIN.ordinal,  DataSource.Interval._3MIN.ordinal,
                                    DataSource.Interval._5MIN.ordinal,  DataSource.Interval._15MIN.ordinal,
                                    DataSource.Interval._30MIN.ordinal, DataSource.Interval._1HOUR.ordinal,
                                    DataSource.Interval._2HOUR.ordinal, DataSource.Interval._4HOUR.ordinal,
                                    DataSource.Interval._6HOUR.ordinal -> {
                                        timeStr = XAxisValueFormatter.formatMonthDateTime(curTick)
                                    }

                                    DataSource.Interval._12HOUR.ordinal, DataSource.Interval._1DAY.ordinal,
                                    DataSource.Interval._3DAY.ordinal,   DataSource.Interval._1WEEK.ordinal-> {
                                        timeStr = XAxisValueFormatter.formatMonthDateYearTime(curTick)
                                    }
                                }
                                legendList.add(LegendEntry(timeStr, Legend.LegendForm.NONE, 9f, Float.NaN, null, 0))
                            }
                            (data.charts[item.type] as CombinedChart).legend.setCustom(legendList)
                            (data.charts[item.type] as CombinedChart).notifyDataSetChanged()

                        }

                    }



                    for((key,chart) in data.charts){
                        chart as CombinedChart
                        chart.highlightValue(0.0F,-1,false)
                        chart.xAxis.limitLines.removeAll(chart.xAxis.limitLines)
                        var lineLimit = LimitLine(e.x)
                        lineLimit.lineColor = ContextCompat.getColor(context,R.color.md_yellow_700)
                        chart.xAxis.addLimitLine(lineLimit)

                        chart.invalidate()

                    }

                }
            }

            override fun onNothingSelected() {

            }
        })

        combinedViewHolder.chart.invalidate()

    }

    private fun hideIndicatorsList(view: View?) {
        println("Hiding indicators list from chart click")
        MainActivity.data.ivDrawer.animate()
                .rotation(0F)
                .alpha(1F)
                .duration = 200
        MainActivity.data.ivCollapseArrow.animate()
                .rotation(0F)
                .alpha(0F)
                .duration = 200

        MainActivity.data.rvIndicatorsOverlays.animate()
                .alpha(0.0F)
                .translationX(MainActivity.data.displayWidth.toFloat())
                .setDuration(500)
                .setInterpolator(OvershootInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        MainActivity.data.rvIndicatorsOverlays.visibility = View.GONE
                    }
                }
                )
        try {
            view?.clearFocus()
            val imm = view?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken,0)
        }catch (e: Exception){

        }

    }
    private fun linkGestures() {
        for (orgChart in data.charts) {
            println("OrgChar: ${orgChart.key} ")
            val destCharts: ArrayList<Chart<*>> = ArrayList()
            for (destChart in data.charts) {
                if (orgChart.key != destChart.key) {
                    print(" ${destChart.key}")
                    destCharts.add(destChart.value as Chart<*>)
                }
            }
            println()
            val chart = orgChart.value as CombinedChart
            chart.onChartGestureListener = MirrorChartGestureListener(orgChart.key, chart, destCharts)
        }
    }
}
