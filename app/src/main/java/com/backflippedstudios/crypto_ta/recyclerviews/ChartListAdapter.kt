package com.backflippedstudios.crypto_ta.recyclerviews

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Color
import android.graphics.Matrix
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import com.backflippedstudios.crypto_ta.*
import com.backflippedstudios.crypto_ta.data.DataSource
import com.backflippedstudios.crypto_ta.dropdownmenus.OverlayAdapter
import com.backflippedstudios.crypto_ta.xaxisformats.XAxisValueFormatter
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.CombinedChart
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import org.ta4j.core.Tick
import kotlin.math.absoluteValue


class ChartListAdapter(var context: Context, var list: ArrayList<ChartStatusData>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var parentHeight: Int? = 0

    class CombinedViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var chart: CombinedChart = view.findViewById(R.id.combined_chart)
        var logo: ImageView = view.findViewById(R.id.main_logo)
    }


    object data {
        var charts: HashMap<Overlay.Kind, Any> = HashMap()
        var status: ChartStatusData.Status = ChartStatusData.Status.LOADING
        var maxCharts: Int = 8
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        lateinit var recyclerViewHolder: RecyclerView.ViewHolder
        println("Creating charts")
        parentHeight = parent.height

        val view = LayoutInflater.from(parent.context).inflate(R.layout.combinedchart_item_view, parent, false) as View
        recyclerViewHolder = CombinedViewHolder(view)
        var sizeRatio = 0.0F
        sizeRatio = if (viewType == ChartStatusData.Type.MAIN_CHART.ordinal) {
            calculateMainChartRatio()
        } else {
            calculateOtherChartRatio()
        }
        recyclerViewHolder.itemView.minimumHeight = (parent.height * sizeRatio).toInt()
        return recyclerViewHolder
    }

    private fun calculateMainChartRatio(): Float {
        var sizeRatio = 1F
        if (MainActivity.data.chartList.size == 2) {
            sizeRatio = 0.8F
        } else if (MainActivity.data.chartList.size == 3) {
            sizeRatio = 0.65F
        } else if (MainActivity.data.chartList.size == 4) {
            sizeRatio = 0.58F
        } else if (MainActivity.data.chartList.size == 5) {
            sizeRatio = 0.44F
        } else if (MainActivity.data.chartList.size == 6) {
            sizeRatio = 0.44F
        } else if (MainActivity.data.chartList.size == 7) {
            sizeRatio = 0.44F
        } else if (MainActivity.data.chartList.size == 8) {
            sizeRatio = 0.4F
        }
        return sizeRatio
    }

    private fun calculateOtherChartRatio(): Float {
        var sizeRatio = 1F
        if (MainActivity.data.chartList.size == 2) {
            sizeRatio = 0.2F
        } else if (MainActivity.data.chartList.size == 3) {
            sizeRatio = 0.175F
        } else if (MainActivity.data.chartList.size == 4) {
            sizeRatio = 0.14F
        } else if (MainActivity.data.chartList.size == 5) {
            sizeRatio = 0.14F
        } else if (MainActivity.data.chartList.size == 6) {
            sizeRatio = 0.112F
        } else if (MainActivity.data.chartList.size == 7) {
            sizeRatio = 0.09333F
        } else if (MainActivity.data.chartList.size == 8) {
            sizeRatio = 0.08571F
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
        println("Binding charts ${list[position].type.name} - ${list[position].kind.name} - ${list[position].status}")
        val srcVals = FloatArray(9)
        val dstMatrix: Matrix
        val dstVals = FloatArray(9)
        val combinedViewHolder = holder as CombinedViewHolder
        combinedViewHolder.logo.visibility = View.INVISIBLE

        if (list[position].type == ChartStatusData.Type.MAIN_CHART) {
            combinedViewHolder.logo.visibility = View.VISIBLE
            combinedViewHolder.itemView.minimumHeight = (this.parentHeight?.times(calculateMainChartRatio()))?.toInt() ?: 0
        } else {
            combinedViewHolder.itemView.minimumHeight = (this.parentHeight?.times(calculateOtherChartRatio()))?.toInt() ?: 0
        }
        println("this.parentHeight: ${this.parentHeight} + minHeigth: ${combinedViewHolder.itemView.minimumHeight}")
        combinedViewHolder.itemView.layoutParams.height = combinedViewHolder.itemView.minimumHeight
        data.status = list[position].status
        when (list[position].status) {
            ChartStatusData.Status.LOADING -> {
                combinedViewHolder.chart.clear()
                combinedViewHolder.chart.setNoDataText("Retrieving Data from web")

            }
            ChartStatusData.Status.UPDATE_FAILED -> {
                combinedViewHolder.chart.setNoDataText("Coin failed, please choose another coin/exchange")
                data.status = ChartStatusData.Status.LOADING_COMPLETE
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
//                MainActivity.data.all_ta[MainActivity.data.saved_time_period].recalculateData(list[position].kind)
                ChartStyle(context).updateOverlays(
                        OverlayAdapter.data.list,
                        MainActivity.data.all_ta[MainActivity.data.saved_time_period],
                        combinedViewHolder.chart)
                list[position].status = ChartStatusData.Status.LOADING_COMPLETE

                //Check to see if Piviot points are on, if so then we need to create a custom legend on main graph
                if(OverlayAdapter.data.all[Overlay.Kind.Piviot_Point]?.selected!!){
                    val legendList: MutableList<LegendEntry> = arrayListOf()
                    var updatedText: String?
                    for (kind in Overlay.Kind.values()) {
                        val item = OverlayAdapter.data.all[kind]!!
                        if (!item.separateChart && ((item.selected && item.kindData.hasData) || (item.kindData.hasData && OverlayAdapter.isParentSelected(item.kindData.parentKind)))) {
                            val color: Int = OverlayAdapter.getColor(item.kind, item.kindData.parentKind, item.kindData.colorIndex)!!
                            updatedText = OverlayAdapter.getLegendText(item.kind, item.kindData.parentKind, item.kindData.colorIndex)
                            legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))

                        }
                    }
                    if(legendList.isNotEmpty()) {
                        //Strange that we have index of kind which is really used for the seperate charts
                        // but for the main Chart it is just None
                        (data.charts[Overlay.Kind.None] as CombinedChart).legend.setCustom(legendList)
                    }
                }

            }
            ChartStatusData.Status.INITIAL_LOAD -> {
                when (list[position].kind) {

                    Overlay.Kind.Volume_Bars -> {
                        ChartStyle(context).updateVolumeGraph(
                                MainActivity.data.all_ta[MainActivity.data.saved_time_period],
                                combinedViewHolder.chart,
                                true
                        )
                    }
                    else -> {
                        combinedViewHolder.chart.clear()
                        combinedViewHolder.chart.setNoDataText("Calculating data")
                    }
                }
            }
            ChartStatusData.Status.UPDATE_CHART -> {
                MainActivity.data.all_ta[MainActivity.data.saved_time_period].recalculateData(list[position].kind)
                when (list[position].kind) {
                    Overlay.Kind.Volume_Bars -> {
                        ChartStyle(context).updateVolumeGraph(
                                MainActivity.data.all_ta[MainActivity.data.saved_time_period],
                                combinedViewHolder.chart,
                                true
                        )
                    }
                    else -> {
                        val allLineGraphStyle: ArrayList<ChartStyle.LineGraphStyle> = ArrayList()

                        var entryData: ArrayList<Entry>

                        for (dItem in OverlayAdapter.data.all.values) {
                            if (list[position].kind == dItem.kindData.parentKind
                                    && dItem.kindData.hasData) {
                                println("Adding line: " + dItem.kind)
                                val color = OverlayAdapter.getColor(dItem.kind, dItem.kindData.parentKind, dItem.kindData.colorIndex)
                                val label = OverlayAdapter.getLabel(dItem.kind, dItem.kindData.parentKind, dItem.kindData.colorIndex)
                                val filled = OverlayAdapter.getfilled(dItem.kind, dItem.kindData.parentKind, dItem.kindData.colorIndex)
                                val filledColor = OverlayAdapter.getfilledColor(dItem.kind, dItem.kindData.parentKind, dItem.kindData.colorIndex)
                                entryData = MainActivity.data.all_ta[MainActivity.data.saved_time_period].getEntryData(dItem.kind)
                                if (entryData.isNotEmpty()) {
                                    allLineGraphStyle.add(ChartStyle.LineGraphStyle(entryData,
                                            ChartStyle.LineStyle(
                                                    lineLabel = label!!,
                                                    lineColor = color!!,
                                                    filled = filled!!,
                                                    filledColor = filledColor!!
                                            )
                                    ))
                                }
                            }
                        }
                        if (list[position].kind == Overlay.Kind.AroonUpDown) {
                            combinedViewHolder.chart.fillInbetweenLines = false

                        } else if (list[position].kind == Overlay.Kind.Stoch_Oscill) {
                            val lineLimit = LimitLine(80F)
                            lineLimit.lineColor = ContextCompat.getColor(context, R.color.md_light_green_300)
                            val lineLimit2 = LimitLine(20F)
                            lineLimit2.lineColor = ContextCompat.getColor(context, R.color.md_light_green_300)
                            combinedViewHolder.chart.axisRight.addLimitLine(lineLimit)
                            combinedViewHolder.chart.axisRight.addLimitLine(lineLimit2)
                        } else if(list[position].kind == Overlay.Kind.Williams__R){
                            val lineLimit = LimitLine(-80F)
                            lineLimit.lineColor = ContextCompat.getColor(context, R.color.md_yellow_600)
                            val lineLimit2 = LimitLine(-20F)
                            lineLimit2.lineColor = ContextCompat.getColor(context, R.color.md_yellow_600)
                            combinedViewHolder.chart.axisRight.addLimitLine(lineLimit)
                            combinedViewHolder.chart.axisRight.addLimitLine(lineLimit2)
                        }

                        ChartStyle(context).updateLineGraph(
                                allLineGraphStyle,
                                combinedViewHolder.chart
                        )
                        combinedViewHolder.chart.notifyDataSetChanged()

                    }
                }

                list[position].status = ChartStatusData.Status.LOADING_COMPLETE
                data.status = ChartStatusData.Status.LOADING_COMPLETE
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

        data.charts[list[position].kind] = combinedViewHolder.chart
        linkGestures()

        combinedViewHolder.chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {

                if (e != null) {
                    hideIndicatorsList(combinedViewHolder.chart.rootView)

                    for (chartStatusData in list) {
                        var updatedText: String
                        val legendList: MutableList<LegendEntry> = arrayListOf()
                        when (chartStatusData.type) {
                            ChartStatusData.Type.MAIN_CHART -> {
                                if (MainActivity.data.all_ta[MainActivity.data.saved_time_period].getCandlestickData(Overlay.Kind.CandleStick).size > e.x.toInt()) {
                                    val values = MainActivity.data.all_ta[MainActivity.data.saved_time_period].getCandlestickData(Overlay.Kind.CandleStick)[e.x.toInt()]
                                    updatedText = "Candle Stick O ${values.open} H ${values.high} L ${values.low} C ${values.close}"
                                    legendList.add(LegendEntry(updatedText, Legend.LegendForm.NONE, 9f, Float.NaN, null, Color.WHITE))
                                }
                                for (kind in Overlay.Kind.values()) {
                                    val item = OverlayAdapter.data.all[kind]!!
                                    if (!item.separateChart && (item.selected || item.kindData.hasData && OverlayAdapter.isParentSelected(item.kindData.parentKind))) {

                                        if (item.kindData.hasData) {
                                            if (MainActivity.data.all_ta[MainActivity.data.saved_time_period].getEntryData(item.kind).size > e.x.toInt()) {

                                                if (!item.kindData.detailed) {
                                                    var color = 0
                                                    val values = MainActivity.data.all_ta[MainActivity.data.saved_time_period].getEntryData(item.kind)[e.x.toInt()]
                                                    //Parent item that has data
                                                    updatedText = item.allIndicatorInfo[0].selectedLegendLabel + " " + values.y
                                                    color = item.allIndicatorInfo[0].color
                                                    legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))
                                                }else{
                                                    var index = e.x.toInt()
                                                    if (item.kind == Overlay.Kind.D_Ich_Cloud_Lead_A || item.kind == Overlay.Kind.D_Ich_Cloud_Lead_B) {
                                                        var laggingPeriod = OverlayAdapter.getLaggingPeriod(Overlay.Kind.Ichimoku_Cloud).toInt()
                                                        index = e.x.toInt() - laggingPeriod
                                                    } else if (item.kind == Overlay.Kind.Keltner_Channel) {
                                                        val size = MainActivity.data.all_ta[MainActivity.data.saved_time_period].getEntryData(Overlay.Kind.D_KC_Lower).size
                                                        if (size > 0) {
                                                            val lastVal = MainActivity.data.all_ta[MainActivity.data.saved_time_period].getEntryData(Overlay.Kind.D_KC_Lower).last().x
                                                            val offset = lastVal - size
                                                            index = ((e.x.toInt() - offset.absoluteValue).toInt())
                                                        }
                                                    }
                                                    if (MainActivity.data.all_ta[MainActivity.data.saved_time_period].getEntryData(item.kind).size > index && index > 0) {
                                                        try {
                                                            val color: Int = OverlayAdapter.getColor(item.kind, item.kindData.parentKind, item.kindData.colorIndex)!!
                                                            val index = e.x.toInt()
                                                            val values = MainActivity.data.all_ta[MainActivity.data.saved_time_period].getEntryData(item.kind)[index]
                                                            updatedText = OverlayAdapter.getSelectedLegendText(item.kind, item.kindData.parentKind, item.kindData.colorIndex) + " " + values.y
                                                            legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))
                                                        } catch (exception: Exception) {

                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        when (item.kind) {


                                            Overlay.Kind.ZigZag -> {
                                                if (MainActivity.data.all_ta[MainActivity.data.saved_time_period].getEntryData(Overlay.Kind.ZigZag).size > 0) {
                                                    lateinit var candidateBefore: Entry
                                                    lateinit var candidateAfter: Entry
                                                    var foundAfter = false
                                                    for (entry in MainActivity.data.all_ta[MainActivity.data.saved_time_period].getEntryData(Overlay.Kind.ZigZag)) {
                                                        if (entry.x > e.x.toInt()) {
                                                            candidateAfter = entry
                                                            foundAfter = true
                                                            break
                                                        }
                                                        candidateBefore = entry
                                                    }

                                                    if (foundAfter) {
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

                                            Overlay.Kind.Exponential_MA_Ribbon -> {
                                                if (MainActivity.data.all_ta[MainActivity.data.saved_time_period].getEntryDataList(Overlay.Kind.Exponential_MA_Ribbon).size > 0) {
                                                    MainActivity.data.all_ta[MainActivity.data.saved_time_period].getEntryDataList(Overlay.Kind.Exponential_MA_Ribbon).forEachIndexed { index, arrayList ->
                                                        if (arrayList.size > e.x.toInt()) {
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
                            ChartStatusData.Type.SEPARATE_CHART -> {
                                for (overlay in OverlayAdapter.data.list) {
                                    if (overlay.selected && overlay.separateChart && chartStatusData.kind == overlay.kind) {

                                        if (overlay.kindData.hasData) {
                                            if (MainActivity.data.all_ta[MainActivity.data.saved_time_period].getEntryData(chartStatusData.kind).size > e.x.toInt() && e.x.toInt() > 0) {

                                                if (!overlay.kindData.detailed) {
                                                    var color = 0
                                                    val values = MainActivity.data.all_ta[MainActivity.data.saved_time_period].getEntryData(chartStatusData.kind)[e.x.toInt()]
                                                    //Parent item that has data
                                                    updatedText = overlay.allIndicatorInfo[0].selectedLegendLabel + " " + values.y
                                                    color = overlay.allIndicatorInfo[0].color
                                                    legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))
                                                }
                                            }
                                        } else {
                                            for (dItem in OverlayAdapter.data.all.values) {
                                                if (overlay.kind == dItem.kindData.parentKind && dItem.kindData.hasData) {
                                                    var index = e.x.toInt()
                                                    if (MainActivity.data.all_ta[MainActivity.data.saved_time_period].getEntryData(dItem.kind).size > index && index > 0) {

                                                        var color = 0
                                                        val values = MainActivity.data.all_ta[MainActivity.data.saved_time_period].getEntryData(dItem.kind)[index]
                                                        updatedText = OverlayAdapter.getSelectedLegendText(dItem.kind, dItem.kindData.parentKind, dItem.kindData.colorIndex) + " " + values.y
                                                        color = OverlayAdapter.getColor(dItem.kind, dItem.kindData.parentKind, dItem.kindData.colorIndex)!!
                                                        legendList.add(LegendEntry(updatedText, Legend.LegendForm.CIRCLE, 9f, Float.NaN, null, color))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                            }


                        }
//                        print(updatedText)

                        if (legendList.isNotEmpty()) {
                            var timeStr = "date"
                            if (MainActivity.data.all_ta[MainActivity.data.saved_time_period].ts?.endIndex!! > e.x.toInt()) {
                                val curTick: Tick? = MainActivity.data.all_ta[MainActivity.data.saved_time_period].ts?.getTick(e.x.toInt())

                                when (MainActivity.data.saved_time_period) {
                                    DataSource.Interval._1MIN.ordinal, DataSource.Interval._3MIN.ordinal,
                                    DataSource.Interval._5MIN.ordinal, DataSource.Interval._15MIN.ordinal,
                                    DataSource.Interval._30MIN.ordinal, DataSource.Interval._1HOUR.ordinal,
                                    DataSource.Interval._2HOUR.ordinal, DataSource.Interval._4HOUR.ordinal,
                                    DataSource.Interval._6HOUR.ordinal -> {
                                        timeStr = XAxisValueFormatter.formatMonthDateTime(curTick)
                                    }

                                    DataSource.Interval._12HOUR.ordinal, DataSource.Interval._1DAY.ordinal,
                                    DataSource.Interval._3DAY.ordinal, DataSource.Interval._1WEEK.ordinal -> {
                                        timeStr = XAxisValueFormatter.formatMonthDateYearTime(curTick)
                                    }
                                }
                                legendList.add(LegendEntry(timeStr, Legend.LegendForm.NONE, 9f, Float.NaN, null, 0))
                            }
                            (data.charts[chartStatusData.kind] as CombinedChart).legend.setCustom(legendList)
                            (data.charts[chartStatusData.kind] as CombinedChart).notifyDataSetChanged()

                        }

                    }



                    for ((key, chart) in data.charts) {
                        chart as CombinedChart
                        chart.highlightValue(0.0F, -1, false)
                        chart.xAxis.limitLines.removeAll(chart.xAxis.limitLines)
                        var lineLimit = LimitLine(e.x)
                        lineLimit.lineColor = ContextCompat.getColor(context, R.color.md_yellow_700)
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
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        } catch (e: Exception) {

        }

    }

    private fun linkGestures() {
        for (orgChart in data.charts) {
//            println("OrgChar: ${orgChart.key} ")
            val destCharts: ArrayList<Chart<*>> = ArrayList()
            for (destChart in data.charts) {
                if (orgChart.key != destChart.key) {
//                    println(" ${destChart.key}")
                    destCharts.add(destChart.value as Chart<*>)
                }
            }

            val chart = orgChart.value as CombinedChart
            chart.onChartGestureListener = MirrorChartGestureListener(orgChart.key, chart, destCharts)
        }
    }
}
