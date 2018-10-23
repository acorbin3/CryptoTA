package com.backflippedstudios.crypto_ta

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import com.backflippedstudios.crypto_ta.dropdownmenus.OverlayAdapter
import com.backflippedstudios.crypto_ta.xaxisformats.MultiLineXAxisRenderer
import com.backflippedstudios.crypto_ta.xaxisformats.XAxisValueFormatter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import java.util.ArrayList


/**
 * Created by C0rbin on 11/16/2017.
 */
class ChartStyle(context: Context) {
    private var context: Context = context

    fun updateCandlestickGraph(ta: TechnicalAnalysis,mChart: CombinedChart?){
        val combinedData = CombinedData()
        //Do candlestick data
        val candleDataSet = CandleDataSet(ta.getCandlestickData(Overlay.Kind.CandleStick) as MutableList<CandleEntry>?, "Candlestick Data Set")
        updateCandleDataFormat(candleDataSet)
        candleDataSet.axisDependency = YAxis.AxisDependency.RIGHT

        if(candleDataSet.values.isNotEmpty()) {
            combinedData.setData(CandleData(candleDataSet))
            mChart?.let {
                //            CombinedChartRenderer
                it.data = combinedData
                chartDefaults(it)
                it.fitScreen()
                it.zoom(5F, 0F, it.data.xMax, 0F) //This helps with the initial zoom
                it.moveViewToX(it.data.xMax) // This moves graph to the all the way to the right
                it.invalidate()
            }
        }
    }

    private fun chartDefaults(it: CombinedChart, drawXAxis: Boolean = true) {
        it.legend.setDrawInside(true)
        it.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        it.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        it.legend.orientation = Legend.LegendOrientation.VERTICAL
        it.legend.xOffset = 20F
        it.legend.form = Legend.LegendForm.CIRCLE
        it.legend.textSize = 11F
        it.legend.isWordWrapEnabled = true
        it.legend.resetCustom()
        it.setDrawBorders(true)
        it.setBorderColor(ContextCompat.getColor(context, R.color.md_grey_600))
        it.axisRight.textColor = Color.WHITE
        it.legend.textColor = Color.WHITE
        it.isAutoScaleMinMaxEnabled = true



        // xAxis properties
        // The philosophy of the xAxis will be the only the main graph
        if(drawXAxis) {
            //it.xAxis.setDrawLabels(false)
            it.xAxis.textColor = Color.WHITE
            it.xAxis.valueFormatter = XAxisValueFormatter()
            it.xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
            it.setXAxisRenderer(MultiLineXAxisRenderer(true, it.viewPortHandler, it.xAxis, it.getTransformer(YAxis.AxisDependency.RIGHT)))
        }
//        it.extraBottomOffset = -5F

        it.axisLeft.isEnabled = false

        it.isHighlightPerTapEnabled = true
        it.description.isEnabled = false
        it.axisRight.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
        it.axisRight.yOffset =7F
    }

    fun updateOverlays(overlays: ArrayList<Overlay>, ta: TechnicalAnalysis, mChart: CombinedChart?){
        val combinedData = CombinedData()
        val allScatter_Data = ArrayList<IScatterDataSet>()

        //Do candlestick data
        if(ta.getCandlestickData(Overlay.Kind.CandleStick).size > 0) {
            val candleDataSet = CandleDataSet(ta.getCandlestickData(Overlay.Kind.CandleStick), "Candlestick Data Set")
            updateCandleDataFormat(candleDataSet)

            candleDataSet.axisDependency = YAxis.AxisDependency.RIGHT
            if(candleDataSet.values.isNotEmpty()) {
                combinedData.setData(CandleData(candleDataSet))
            }
        }

        //Create chart data for all overlays that are selected and then refresh the chart
        val allLine_Data = ArrayList<ILineDataSet>()

        for (overlay in overlays){
            if (overlay.selected && !overlay.separateChart) {
                var list = ta.getData(overlay.kind)
                if(list.isEmpty()){
                    continue
                }
                if(overlay.kind != Overlay.Kind.Ichimoku_Cloud) {
                    for (i in list.indices) {
                        when (overlay.allIndicatorInfo[i].type) {
                            Overlay.IndicatorType.Line -> {
                                if(!list[i].isEmpty()) {
                                    AddOneLine(list[i], overlay.allIndicatorInfo[i].label, overlay.allIndicatorInfo[i].color, allLine_Data)
                                }
                            }
                            Overlay.IndicatorType.Piviot_Line ->{
                                if(!list[i].isEmpty()) {
                                    var segment  = ArrayList<Entry>()
                                    var lastEntry = list[i].first()
                                    //loop over and find segments
                                    //when transition to new segment add new line and clear segment
                                    list[i].forEachIndexed{ index, entry ->
                                        if(!entry.y.isNaN()){
                                            if(lastEntry.y != entry.y || index == list[i].lastIndex){
                                                if(index == list[i].lastIndex){
                                                    segment.add(entry)
                                                }
//                                                println("Doesnt match, adding to list. Segmet size ${segment.size}")
                                                AddOneLine(segment, overlay.allIndicatorInfo[i].label, overlay.allIndicatorInfo[i].color, allLine_Data)
                                                segment = ArrayList<Entry>()
                                            }
//                                            println("Adding ${entry.x},${entry.y}")
                                            segment.add(entry)
                                            lastEntry = entry
                                        }
                                    }
                                }
                            }
                            Overlay.IndicatorType.Scatter -> {
                                val scatterDataSet = ScatterDataSet(list[i], overlay.allIndicatorInfo[i].label)
                                scatterDataSet.setDrawIcons(false)
                                scatterDataSet.color = overlay.allIndicatorInfo[i].color
                                scatterDataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE)
                                scatterDataSet.scatterShapeSize = 8F
                                scatterDataSet.axisDependency = YAxis.AxisDependency.RIGHT
                                if(scatterDataSet.values.isNotEmpty()) {
                                    allScatter_Data.add(scatterDataSet)
                                }
                            }
                        }

                    }
                }else{
                    if (ta.closePrice.timeSeries.tickCount > 52) {
                        val lineDataSet = LineDataSet(list[0], overlay.allIndicatorInfo[0].label)
                        lineDataSet.setDrawIcons(false)
                        lineDataSet.setDrawCircles(false)
                        lineDataSet.setDrawCircleHole(false)
                        lineDataSet.color = overlay.allIndicatorInfo[0].color
                        lineDataSet.axisDependency = YAxis.AxisDependency.RIGHT
                        lineDataSet.setDrawFilled(true)


                        val lineDataSet2 = LineDataSet(list[1], overlay.allIndicatorInfo[1].label)
                        lineDataSet2.setDrawIcons(false)
                        lineDataSet2.setDrawCircles(false)
                        lineDataSet2.setDrawCircleHole(false)
                        lineDataSet2.color = overlay.allIndicatorInfo[1].color
                        lineDataSet2.axisDependency = YAxis.AxisDependency.RIGHT
                        lineDataSet2.setDrawFilled(true)
                        lineDataSet2.fillColor = overlay.allIndicatorInfo[1].filledColor
                        lineDataSet2.fillFormatter = MyFillFormatter(lineDataSet)

                        lineDataSet.fillColor = overlay.allIndicatorInfo[0].filledColor
                        lineDataSet.fillFormatter = MyFillFormatter(lineDataSet2)

                        AddOneLine(list[2], overlay.allIndicatorInfo[2].label, overlay.allIndicatorInfo[2].color, allLine_Data)
                        AddOneLine(list[3], overlay.allIndicatorInfo[3].label, overlay.allIndicatorInfo[3].color, allLine_Data)
                        AddOneLine(list[4], overlay.allIndicatorInfo[4].label, overlay.allIndicatorInfo[4].color, allLine_Data)
                        if(lineDataSet.values.isNotEmpty()) {
                            allLine_Data.add(lineDataSet)
                        }
                        if(lineDataSet2.values.isNotEmpty()) {
                            allLine_Data.add(lineDataSet2)
                        }

                    }
                }
            }

            if(!allLine_Data.isEmpty()) {
                combinedData.setData(LineData(allLine_Data))
            }
            if(!allScatter_Data.isEmpty()) {
                combinedData.setData(ScatterData(allScatter_Data))
            }

        }

        if(combinedData.allData.size > 0) {
            mChart?.let {
                it.data = combinedData
                chartDefaults(it)
                it.fitScreen()
                it.zoom(5F,0F,it.data.xMax,0F)
                it.invalidate()
            }
        }
    }

    //line data, how it wants to be displayed (filled, colors, dotted line?),label
    data class LineGraphStyle(var lineData: ArrayList<Entry>,var lineStyle: LineStyle )
    data class LineStyle(
            var lineLabel: String = "",
            var lineColor: Int = 0,
            var filled: Boolean = false,
            var filledColor: Int = 0,
            var drawValues: Boolean = false,
            var axisDependency: YAxis.AxisDependency = YAxis.AxisDependency.RIGHT,
            var displayText: Boolean = false,
            var textSize: Float = 10F,
            var textColor: Int = 0
    )

    fun updateLineGraph(allLineGraphStyle: ArrayList<LineGraphStyle>, mChart: CombinedChart?, moveViewToEnd: Boolean = false){
        val allLineData = ArrayList<ILineDataSet>()
        val combinedData = CombinedData()
        for(lineGraphStyle in allLineGraphStyle){
            AddOneLine(lineGraphStyle.lineData,lineGraphStyle.lineStyle,allLineData)
        }

        if(allLineData.isNotEmpty()) {
            combinedData.setData(LineData(allLineData))
            mChart?.let{
                it.data = combinedData
                chartDefaults(it,false)
                it.fitScreen()
                it.zoom(5F,0F,it.data.xMax,0F)
                if(moveViewToEnd){
                    it.moveViewToX(it.data.xMax)
                }
                it.invalidate()
            }
        }else{
            mChart?.let{
                chartDefaults(it,false)
                it.fitScreen()
                it.invalidate()
            }
        }
    }
    fun updateVolumeGraph(ta: TechnicalAnalysis,mChart: CombinedChart?, moveViewToEnd: Boolean = false){
        val allBar_Data = ArrayList<IBarDataSet>()
        val barDataSet = VolumeBarDataSet(ta.data[Overlay.Kind.Volume_Bars]?.data as ArrayList<BarEntry>,"Volume")
        val combinedData = CombinedData()
        barDataSet.setColors(
                ContextCompat.getColor(context, R.color.md_red_500),
                ContextCompat.getColor(context, R.color.md_green_500),
                ContextCompat.getColor(context, R.color.md_blue_400)
        )
        println("Vol Bar data size: ${ta.data[Overlay.Kind.Volume_Bars]?.data?.size}")
        barDataSet.axisDependency = YAxis.AxisDependency.RIGHT
        barDataSet.setDrawValues(false)
        println("Sizie of bar dataset: ${barDataSet.stackSize}")
        if(barDataSet.values.isNotEmpty()) {
            allBar_Data.add(barDataSet)
            combinedData.setData(BarData(allBar_Data))
            mChart?.let {
                it.data = combinedData
                chartDefaults(it, false)
                it.fitScreen()
                it.zoom(5F, 0F, it.data.xMax, 0F)
                if (moveViewToEnd) {
                    it.moveViewToX(it.data.xMax) // This moves graph to the all the way to the right
                }
                it.invalidate()
            }
        }
    }
    private fun updateCandleDataFormat(candleDataSet: CandleDataSet) {
        candleDataSet.setDrawIcons(false)
        candleDataSet.axisDependency = YAxis.AxisDependency.RIGHT
        candleDataSet.formLineWidth = 3F
        candleDataSet.shadowColorSameAsCandle = true
        candleDataSet.shadowWidth = 1f
        candleDataSet.decreasingColor = ContextCompat.getColor(context, R.color.md_red_500)
        candleDataSet.decreasingPaintStyle = Paint.Style.FILL
        candleDataSet.increasingColor = ContextCompat.getColor(context, R.color.md_green_500)
        candleDataSet.increasingPaintStyle = Paint.Style.FILL
        candleDataSet.neutralColor = ContextCompat.getColor(context, R.color.md_blue_400)
        candleDataSet.barSpace = .25F
        candleDataSet.setDrawValues(false)
//        candleDataSet.setValueTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
//        candleDataSet.isHighlightEnabled = true
//        candleDataSet.highLightColor = ContextCompat.getColor(context, R.color.md_white_1000)
    }

    private fun AddOneLine(ticks: ArrayList<Entry>, label: String, color: Int, lineData: ArrayList<ILineDataSet>){
        val lineDataSet = LineDataSet(ticks,label)
        lineDataSet.setDrawIcons(false)
        lineDataSet.setDrawCircles(false)
        lineDataSet.setDrawCircleHole(false)
        lineDataSet.color = color
        lineDataSet.axisDependency = YAxis.AxisDependency.RIGHT
        if(lineDataSet.values.isNotEmpty()) {
            lineData.add(lineDataSet)
        }
    }
    private fun AddOneLine(ticks: ArrayList<Entry>, lineStyle: LineStyle , lineData: ArrayList<ILineDataSet>){
        try {
            val lineDataSet = LineDataSet(ticks, lineStyle.lineLabel)

            lineDataSet.valueTextSize = lineStyle.textSize
            lineDataSet.valueTextColor = lineStyle.textColor
            lineDataSet.isHighlightEnabled = true
//        lineDataSet.setDrawHighlightIndicators(true)
//        lineDataSet.highLightColor = color
            lineDataSet.setDrawValues(lineStyle.drawValues)
            lineDataSet.setDrawIcons(false)
            lineDataSet.setDrawCircles(false)
            lineDataSet.setDrawCircleHole(false)
            lineDataSet.setDrawFilled(lineStyle.filled)
            lineDataSet.fillColor = lineStyle.filledColor
            lineDataSet.color = lineStyle.lineColor
            lineDataSet.axisDependency = lineStyle.axisDependency
            if (lineDataSet.values.isNotEmpty()) {
                lineData.add(lineDataSet)
            }
        } catch (e: Exception) {
        }
    }
}