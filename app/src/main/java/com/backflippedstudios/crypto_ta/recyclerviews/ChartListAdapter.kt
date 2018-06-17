package com.backflippedstudios.crypto_ta.recyclerviews

import android.content.Context
import android.graphics.Matrix
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.backflippedstudios.crypto_ta.*
import com.backflippedstudios.crypto_ta.dropdownmenus.OverlayAdapter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.CombinedChart
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import com.github.mikephil.charting.charts.LineChart


class ChartListAdapter(var context: Context, var list: ArrayList<ChartStatusData>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var parentHeight: Int? = 0

    class CombinedViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var chart: CombinedChart = view.findViewById(R.id.combined_chart)
    }

    object data {
        var charts: HashMap<ChartStatusData.Type, Any> = HashMap()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
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
            sizeRatio = 0.65F
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
            sizeRatio = 0.116666F
        }
        return sizeRatio
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].type.ordinal
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        println("Binding charts ${list[position].type.name}- ${list[position].status}")
        val srcVals = FloatArray(9)
        val dstMatrix: Matrix
        val dstVals = FloatArray(9)
        val combinedViewHolder = holder as CombinedViewHolder
        if(list[position].type == ChartStatusData.Type.MAIN_CHART) {
            combinedViewHolder.itemView.minimumHeight = (this.parentHeight?.times(calculateMainChartRatio()))?.toInt() ?: 0
        }
        else{
            combinedViewHolder.itemView.minimumHeight = (this.parentHeight?.times(calculateOtherChartRatio()))?.toInt() ?: 0
        }
        when (list[position].status) {
            ChartStatusData.Status.LOADING -> {
                combinedViewHolder.chart.clear()
                combinedViewHolder.chart.setNoDataText("Retrieving Data from web")
            }
            ChartStatusData.Status.UPDATE_FAILED -> {
                combinedViewHolder.chart.setNoDataText("Coin failed, please choose another coin/exchange/currency")
            }
            ChartStatusData.Status.UPDATE_CANDLESTICKS -> {
                ChartStyle(context).updateCandlestickGraph(
                        MainActivity.data.all_ta[MainActivity.data.saved_time_period],
                        combinedViewHolder.chart)
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
                            allLineGraphStyle.add(ChartStyle.LineGraphStyle(MainActivity.data.all_ta[MainActivity.data.saved_time_period].aroonOscillatorData,
                                    ChartStyle.LineStyle(
                                            lineLabel = "Aroon Oscillator",
                                            lineColor = ContextCompat.getColor(context,R.color.md_cyan_500),
                                            filled = true,
                                            filledColor = ContextCompat.getColor(context,R.color.md_cyan_100)
                                    )
                            ))

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
        combinedViewHolder.chart.invalidate()
    }

    private fun linkGestures() {
        for (orgChart in data.charts) {
            val destCharts: ArrayList<Chart<*>> = ArrayList()
            for (destChart in data.charts) {
                if (orgChart.key != destChart.key) {
                    destCharts.add(destChart.value as Chart<*>)
                }
            }
            val chart = orgChart.value as CombinedChart
            chart.onChartGestureListener = MirrorChartGestureListener(chart, destCharts)
        }
    }
}
