package com.backflippedstudios.crypto_ta.dropdownmenus

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.backflippedstudios.crypto_ta.*
import com.backflippedstudios.crypto_ta.recyclerviews.ChartListAdapter
import com.github.mikephil.charting.charts.CombinedChart
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import com.skydoves.colorpickerpreference.ColorPickerDialog


class OverlayAdapter(context: Context, private val overlayList: ArrayList<Overlay>):
        RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var isFromView: Boolean = false
    private var isFromViewET1: Boolean = false
    private var isFromViewET2: Boolean = false
    private var isFromViewET3: Boolean = false
    private var isFromViewET4: Boolean = false
    private var lContext: Context = context

    private val DETAILED_ITEM: Int = 1
    private val COMPACT_ITEM: Int = 0

    object data{
        var list: ArrayList<Overlay> = ArrayList()
    }
    init {
        data.list = overlayList
        //Load indicators from restart
        for(item in data.list){
            //Adding Notifications as initially on
            if(item.kind == Overlay.Kind.Notifications
                    && !MainActivity.data.prefs?.contains(Overlay.Kind.Notifications.name)!!){
                item.selected = true
                val editor = MainActivity.data.prefs!!.edit()
                editor.putBoolean(item.kind.name, true)
                editor.apply()
                FirebaseMessaging.getInstance().subscribeToTopic("notifications")
            }else {
                item.selected = MainActivity.data.prefs?.getBoolean(item.kind.name, false)!!
            }
        }
    }

    override fun getItemCount(): Int {
//        println("data.list.size " + data.list.size)
        return data.list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if(data.list[position].kindData.detailed){
            DETAILED_ITEM
        }
        else{
            COMPACT_ITEM
        }
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) =
            if(!data.list[position].kindData.detailed) {
                vh as OverlayAdapter.ViewHolder
                vh.indicatorTitle.text = data.list[position].kind.name

                val onKeyListenerEditText1 = View.OnKeyListener { v, keyCode, _ ->
                    var cameFromET: Boolean = isFromViewET1
                    val valuesIndex: Int = 0
                    var et = v as EditText?
                    if(et?.text.toString().isNotEmpty()) {
                        if (triggerTextEditUpdate(
                                        keyCode,
                                        v.tag as Overlay.Kind,
                                        et?.text.toString().toDouble(),
                                        valuesIndex,
                                        cameFromET,
                                        true,
                                        v
                                )) return@OnKeyListener true
                        false
                    }
                    false
                }
                val onKeyListenerEditText2 = View.OnKeyListener { v, keyCode, _ ->
                    var cameFromET: Boolean = isFromViewET2
                    val valuesIndex: Int = 1
                    var et = v as EditText?
                    if(et?.text.toString().isNotEmpty()) {
                        if (triggerTextEditUpdate(
                                        keyCode,
                                        v.tag as Overlay.Kind,
                                        et?.text.toString().toDouble(),
                                        valuesIndex,
                                        cameFromET,
                                        true,
                                        v
                                )) return@OnKeyListener true
                        false
                    }
                    false
                }
                val onKeyListenerEditText3 = View.OnKeyListener { v, keyCode, _ ->
                    var cameFromET: Boolean = isFromViewET3
                    val valuesIndex: Int = 2
                    var et = v as EditText?
                    if(et?.text.toString().isNotEmpty()) {
                        if (triggerTextEditUpdate(
                                        keyCode,
                                        v.tag as Overlay.Kind,
                                        et?.text.toString().toDouble(),
                                        valuesIndex,
                                        cameFromET,
                                        true,
                                        v
                                )) return@OnKeyListener true
                        false
                    }
                    false
                }
                val onKeyListenerEditText4 = View.OnKeyListener { v, keyCode, _ ->
                    var cameFromET: Boolean = isFromViewET4
                    val valuesIndex: Int = 3
                    var et = v as EditText?
                    if(et?.text.toString().isNotEmpty()) {
                        if (triggerTextEditUpdate(
                                        keyCode,
                                        v.tag as Overlay.Kind,
                                        et?.text.toString().toDouble(),
                                        valuesIndex,
                                        cameFromET,
                                        true,
                                        v
                                )) return@OnKeyListener true
                        false
                    }
                    false
                }

                if(!data.list[position].kindData.hasChildren){
                    vh.ivDetailedDropdown.visibility = View.INVISIBLE
                }

                isFromView = true
                vh.switch.isChecked = data.list[position].selected
                isFromView = false

                vh.switch.tag = data.list[position].kind

                vh.indicatorTitle.text = data.list[position].title

                if (position == itemCount) {
                    vh.switch.visibility = View.INVISIBLE
                } else {
                    vh.switch.visibility = View.VISIBLE
                }

                vh.et1.tag = data.list[position].kind
                vh.et2.tag = data.list[position].kind
                vh.et3.tag = data.list[position].kind
                vh.et4.tag = data.list[position].kind



                vh.et1.visibility = View.INVISIBLE
                vh.et2.visibility = View.INVISIBLE
                vh.et3.visibility = View.INVISIBLE
                vh.et4.visibility = View.INVISIBLE
                vh.ivDetailedDropdown.visibility = View.INVISIBLE

                if (getValue(position,0) > -1) {
                    isFromViewET1 = true
                    vh.et1.setText(getTextValue(position, 0))
//                    print("vh.et1 ${vh.et1.text}")
                    isFromViewET1 = false
                    vh.et1.visibility = View.VISIBLE
                    vh.et1.setOnKeyListener(onKeyListenerEditText1)
                }
                if (getValue(position,1) > -1) {
                    isFromViewET2 = true
                    vh.et2.setText(getTextValue(position, 1))
                    isFromViewET2 = false
                    vh.et2.visibility = View.VISIBLE
                    vh.et2.setOnKeyListener(onKeyListenerEditText2)
                }
                if (getValue(position,2) > -1) {
                    vh.et3.visibility = View.VISIBLE
                    isFromViewET3 = true
                    vh.et3.setText(getTextValue(position, 2))
                    isFromViewET3 = false
                    vh.et3.setOnKeyListener(onKeyListenerEditText3)
                }
                if (getValue(position,3) > -1) {
                    vh.et4.visibility = View.VISIBLE
                    isFromViewET4 = true
                    vh.et4.setText(getTextValue(position, 3))
                    isFromViewET4 = false
                    vh.et4.setOnKeyListener(onKeyListenerEditText4)
                }
                if(data.list[position].kindData.hasChildren){
                    vh.ivDetailedDropdown.visibility = View.VISIBLE
                }
                vh.ivDetailedDropdown.tag = data.list[position].kind
                vh.ivDetailedDropdown.setOnClickListener {

                    val kind = it.tag
                    var dropPosition = getPositionFromKind(kind)

//                    println("Position: $valueIndex dropPosition $dropPosition")

                    toggleDetailedItems(dropPosition, vh)
                }

                vh.switch.setOnCheckedChangeListener { switchView, isChecked ->
                    val getPosition = getPositionFromKind(switchView.tag as Overlay.Kind)
                    if (!isFromView) {
                        data.list[getPosition].selected = isChecked
                        val editor = MainActivity.data.prefs!!.edit()
                        editor.putBoolean(data.list[getPosition].kind.name, isChecked)
                        editor.apply()
                        println("Switching overlay " + data.list[getPosition].title + " to " + isChecked)
                        var bundle = Bundle()
                        bundle.putString("uuid", MainActivity.data.uuid)
                        bundle.putString("switching_overlay", data.list[getPosition].title)
                        if(isChecked){
                            bundle.putString("switching_on_off", "On")
                        }else{
                            bundle.putString("switching_on_off", "Off")
                        }
                        MainActivity.data.mFirebaseAnalytics.logEvent("changing_overlay", bundle)

                        if(data.list[getPosition].kind == Overlay.Kind.Notifications) {
                            if(isChecked){
                                FirebaseMessaging.getInstance().subscribeToTopic("notifications")
                            }else{
                                FirebaseMessaging.getInstance().unsubscribeFromTopic("notifications")
                            }

                        }else{
                            if (data.list[getPosition].separateChart) {
                                if (!isChecked) {
                                    removeChartItem(data.list[getPosition].chartType)
                                } else {
                                    MainActivity.data.chartList.add(MainActivity.data.chartList.size, ChartStatusData(ChartStatusData.Status.UPDATE_CHART, data.list[getPosition].chartType))
                                }
                            }

                            //Update the chart with updated overlay selection
                            if (data.list[getPosition].kind == Overlay.Kind.Ichimoku_Cloud) {
                                MainActivity.data.all_ta[MainActivity.data.saved_time_period].updateIndividualChartData()

                                if (data.list[getPosition].selected) {
                                    updateChartStatus(ChartStatusData.Status.UPDATE_CHART, data.list[getPosition].chartType)
                                }
                            }

                            updateChartStatus(ChartStatusData.Status.UPDATE_OVERLAYS, ChartStatusData.Type.MAIN_CHART)
                            MainActivity.data.rvCharts.adapter.notifyDataSetChanged()
                            //Reset legends
                            for ((key, chart) in ChartListAdapter.data.charts) {
                                chart as CombinedChart
                                chart.xAxis.limitLines.removeAll(chart.xAxis.limitLines)
                                chart.legend.resetCustom()
                            }

                            //Collapse detailed items
                            if (!isChecked) {
                                toggleDetailedItems(getPosition, vh, true)
                            }
                        }
                    }
                    else{

                    }

                }

            }else{
                val onKeyListenerEditText1 = View.OnKeyListener { v, keyCode, _ ->
                    val cameFromET: Boolean = isFromViewET1
                    val valuesIndex: Int = -1
                    val et = v as EditText?
                    if(et?.text.toString().isNotEmpty()) {
                        if (triggerTextEditUpdate(
                                        keyCode,
                                        v.tag as Overlay.Kind,
                                        et?.text.toString().toDouble(),
                                        valuesIndex,
                                        cameFromET,
                                        true,
                                        v
                                ))return@OnKeyListener true
                        false
                    }
                    false

                }

                vh as OverlayAdapter.DetailedViewHolder
                vh.detailedTitle.text = data.list[position].allIndicatorInfo[0].label
                if(getValue(position,data.list[position].kindData.valueIndex) > -1) {
                    vh.et1Detail.visibility = View.VISIBLE
                    vh.seekBar.visibility = View.VISIBLE
                    isFromViewET1 = true
                    vh.et1Detail.setText(getTextValue(position, data.list[position].kindData.valueIndex))
                    isFromViewET1 = false
                }else{
                    vh.et1Detail.visibility = View.INVISIBLE
                    vh.seekBar.visibility = View.INVISIBLE
                }
                vh.et1Detail.tag = data.list[position].kind
                vh.et1Detail.setOnKeyListener(onKeyListenerEditText1)

                vh.seekBar.tag = data.list[position].kind
                val scaleFactor = getScalingFactor(position)
                val currentValue = getValue(position)
                vh.seekBar.max = (getMax(position) * scaleFactor ).toInt()
                //vh.seekBar.min = data.list[valueIndex].values[0].min.toInt() //this increases the Min API
                val scaledValue = currentValue * scaleFactor
                vh.seekBar.progress = scaledValue.toInt()
                println("${vh.seekBar.tag} max: ${vh.seekBar.max} Initial progress: ${vh.seekBar.progress}" +
                        " Scalefactor:${scaleFactor} value: ${currentValue} Scaled value: $scaledValue")
                vh.seekBar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
                    //Scale progress bar

                    override fun onStartTrackingTouch(p0: SeekBar?) {
                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {
                       //Change text value & trigger TA update
                        println("ONStop value is ${p0?.progress?.toDouble()} ${p0?.tag.toString()}")
                        val pos = getPositionFromKind(p0?.tag as Overlay.Kind)
                        var scalingFactor: Int = getScalingFactor(pos)
                        triggerTextEditUpdate(
                                KeyEvent.KEYCODE_ENTER,
                                p0?.tag as Overlay.Kind,
                                p0.progress.toDouble()/scalingFactor ,
                                -1,
                                false,
                                true,
                                null,
                                true
                        )
                    }

                    override fun onProgressChanged(p0: SeekBar?, p1: Int, fromUser: Boolean) {
                        println("${p0?.tag.toString()} value: $p1 FromUser: $fromUser")
                        val pos = getPositionFromKind(p0?.tag as Overlay.Kind)
                        var scalingFactor: Int = getScalingFactor(pos)

                        if (fromUser) {
                            // We just want to update the text
                            setValue(pos, p1.toDouble()/ scalingFactor)

                            if (data.list[pos].valuesAreInts) {
                                vh.et1Detail.setText((p1.toDouble() / scalingFactor).toInt().toString())
                            }
                            else {
                                vh.et1Detail.setText((p1.toDouble() / scalingFactor).toString())
                            }

                        }
                        else{
                            println("Resetting progress to ${(getValue(pos) * scalingFactor).toInt()}")
                            p0?.progress = (getValue(pos) * scalingFactor).toInt()
                        }

                    }


                })

                //Update the color box for the color picker and add the click listner
                if (data.list[position].kind == data.list[position].kindData.parentKind) {
                } else {
                    for (i in data.list.indices) {
                        if (data.list[i].kind == data.list[position].kindData.parentKind
                                && data.list[position].kindData.colorIndex == -1){

                            vh.colorPicker.visibility = View.INVISIBLE
                        }
                        if (data.list[i].kind == data.list[position].kindData.parentKind
                                && data.list[position].kindData.colorIndex >= 0) {
                            println("Color Index: ${data.list[position].kindData.colorIndex}")
                            vh.colorPicker.setBackgroundColor(data.list[i].allIndicatorInfo[data.list[position].kindData.colorIndex].color)
                            vh.colorPicker.visibility = View.VISIBLE
                            vh.colorPicker.tag = position
                            vh.colorPicker.setOnClickListener {
                                val pos = vh.colorPicker.tag as Int
                                val builder = ColorPickerDialog.Builder(lContext, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                                builder.setTitle("ColorPicker Dialog")
                                builder.setFlagView(CustomFlag(lContext, R.layout.layout_flag))
                                //Check to see if pref exist, if it doesnt set the default value to the color from the Overlay Defaults
                                builder.setPreferenceName(data.list[pos].kind.toString())
//                                builder.colorPickerView.setSavedColor(Color.RED)

                                builder.setPositiveButton("Ok") { colorEnvelope ->
                                    setColor(pos, colorEnvelope.color)
                                    updateChartStatus(ChartStatusData.Status.UPDATE_OVERLAYS, ChartStatusData.Type.MAIN_CHART)
                                    val activity = lContext as Activity
                                    activity.runOnUiThread {
                                        MainActivity.data.rvCharts.adapter.notifyDataSetChanged()
                                        MainActivity.data.rvIndicatorsOverlays.adapter.notifyItemChanged(pos)
                                    }
                                    builder.colorPickerView.saveData()
                                }

                                builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })

                                builder.setNeutralButton("Reset Color", DialogInterface.OnClickListener{dialogInterface, i ->
                                    val pos = vh.colorPicker.tag as Int
                                    val defaultColor = getDefaultColor(pos)
                                    setColor(pos, defaultColor)
                                    builder.colorPickerView.setSavedColor(defaultColor)
                                    updateChartStatus(ChartStatusData.Status.UPDATE_OVERLAYS, ChartStatusData.Type.MAIN_CHART)
                                    val activity = lContext as Activity
                                    activity.runOnUiThread {
                                        MainActivity.data.rvCharts.adapter.notifyDataSetChanged()
                                        MainActivity.data.rvIndicatorsOverlays.adapter.notifyItemChanged(pos)
                                    }
                                })
                                val alertDialog: AlertDialog = builder.create()
                                alertDialog.show()
                            }
                            break
                        }
                    }
                }
            }

    private fun toggleDetailedItems(dropPosition: Int, vh: ViewHolder, onlyCollapse: Boolean = false) {
        val mainKind = data.list[dropPosition].kind
        var itemsToAdd: ArrayList<Overlay> = ArrayList()
        var positionsToRemove: ArrayList<Int> = ArrayList()
        for (i in data.list.indices) {
            var item = data.list[i]
            //When items is already visible
    //                        println("$i Parent: ${item.kindData.parentKind} Mainkind: $mainKind, item.kind:${item.kind} & visible: ${item.kindData.visible}")
            if ((item.kindData.parentKind == mainKind) and (item.kind != mainKind) and item.kindData.visible) {
    //                            print("removing item $i ${item.kind}")
                positionsToRemove.add(i)
            }
        }

        if (!positionsToRemove.isEmpty()) {

            for (i in positionsToRemove.reversed()) {
    //                            println("Removing at $i size: ${data.list.size}")
                data.list.removeAt(i)
            }
    //                        println("start: ${positionsToRemove[0]} size: ${positionsToRemove.size}")
            MainActivity.data.rvIndicatorsOverlays.adapter.notifyItemRangeRemoved(
                    positionsToRemove[0], positionsToRemove.size)

            vh.ivDetailedDropdown.animate()
                    .rotation(0F)
                    .duration = 200
        } else {
            if(!onlyCollapse) {
                var insertPosition: Int = dropPosition + 1
                for (item in Overlay.Kind.values()) {
                    var overlay = Overlay(item)
                    //                            println("Overlay.kind: ${overlay.kind} parentKind: ${overlay.kindData.parentKind} mainkind: $mainKind")
                    if ((overlay.kindData.parentKind == mainKind) and (overlay.kind != mainKind)) {
                        println("Adding ${overlay.kind}")
                        overlay.kindData.visible = true
                        itemsToAdd.add(overlay)
                    }
                }
                // Need to update items from start insert postion till the end of the list to
                for (item in itemsToAdd) {
                    data.list.add(insertPosition, item)
                    MainActivity.data.rvIndicatorsOverlays.adapter.notifyItemInserted(insertPosition)
                    insertPosition += 1

                }

                vh.ivDetailedDropdown.animate()
                        .rotation(180F)
                        .duration = 200
            }
        }
    }

    private fun setColor(pos: Int, color: Int){
        for (i in data.list.indices) {
            if (data.list[i].kind == data.list[pos].kindData.parentKind
                    && data.list[pos].kindData.colorIndex >= 0) {
                data.list[i].allIndicatorInfo[data.list[pos].kindData.colorIndex].color = color
                break
            }
        }
    }

    private fun getScalingFactor(pos: Int): Int{
        for (i in data.list.indices) {
            if (data.list[i].kind == data.list[pos].kindData.parentKind) {
                return data.list[i].valuesScaleFactor
            }
        }
        return 1
    }

    private fun getDefaultColor(pos: Int): Int{
        var color: Int = 0
        for (i in data.list.indices) {
            if (data.list[i].kind == data.list[pos].kindData.parentKind
                    && data.list[pos].kindData.colorIndex >= 0) {
                color = data.list[i].allIndicatorInfo[data.list[pos].kindData.colorIndex].colorDefault
                break
            }
        }
        return color
    }

    private fun isInts(pos: Int): Boolean{
            for (i in data.list.indices) {
                if (data.list[i].kind == data.list[pos].kindData.parentKind) {
                    return data.list[i].valuesAreInts
                }
            }
        return true
    }

    private fun getValue(pos: Int, valuesIndex: Int = -1): Double{
        if(data.list[pos].kindData.valueIndex > -1) {
            for (i in data.list.indices) {
                if (data.list[i].kind == data.list[pos].kindData.parentKind) {
                    return data.list[i].values[data.list[pos].kindData.valueIndex].value
                }
            }
        }
        if(valuesIndex > -1){
            return data.list[pos].values[valuesIndex].value
        }
        return -1.0
    }

    private fun setValue(pos: Int, newValue: Double, valuesIndex: Int = -1){
        for (i in data.list.indices) {
            if (data.list[i].kind == data.list[pos].kindData.parentKind) {
                if(data.list[pos].kindData.valueIndex == -1) {
                    data.list[i].values[valuesIndex].value = newValue
                }
                else{
                    data.list[i].values[data.list[pos].kindData.valueIndex].value = newValue
                }
            }
        }
    }

    private fun getMax(pos: Int): Double{
        if(data.list[pos].kindData.valueIndex > -1) {
            for (i in data.list.indices) {
                if (data.list[i].kind == data.list[pos].kindData.parentKind) {
                    return data.list[i].values[data.list[pos].kindData.valueIndex].max
                }
            }
        }
        return 0.0
    }


    private fun getPositionFromKind(kind: Any?): Int {
        var dropPosition1 = 0
        for (i in data.list.indices) {
            if (data.list[i].kind == kind) {
                dropPosition1 = i
                break
            }
        }
        return dropPosition1
    }

    private fun triggerTextEditUpdate(
            keyCode: Int,
            kind: Overlay.Kind,
            editValue: Double,
            valuesIndex: Int,
            cameFromET: Boolean,
            runTA: Boolean = true,
            v: View? = null,
            forceUpdate: Boolean = false

    ): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            //Perform Code
            val position: Int = getPositionFromKind(kind)

            var valuesChanged = if (data.list[position].valuesAreInts) {
                getValue(position, valuesIndex).toInt() != editValue.toInt()
            } else {
                getValue(position, valuesIndex) != editValue
            }

            if (!cameFromET && (valuesChanged or forceUpdate)) {
                //Update the edit text that was edited by the user
                setValue(position, editValue, valuesIndex)

                var updateIndex = position
                //We are updating parent & and we might need to update the detailed items
                if(data.list[position].kind == data.list[position].kindData.parentKind){
                    updateIndex = position
                    for(i in data.list.indices){
                        if((data.list[i].kindData.parentKind == data.list[position].kind)
                        and (data.list[i].kindData.valueIndex == valuesIndex)){
                            MainActivity.data.rvIndicatorsOverlays.adapter.notifyItemChanged(i)
                            break

                        }
                    }
                }
                else{
                    //This is the case we are updating the detailed and we need to update the parent
                    for(i in data.list.indices){
                        if(data.list[i].kind == data.list[position].kindData.parentKind){
                            updateIndex = i
                            MainActivity.data.rvIndicatorsOverlays.adapter.notifyItemChanged(updateIndex)
                            break
                        }
                    }
                    //Quick update of current item. This is to update the seekbar
                    //Enhancement to combine the forloop above
                    for(i in data.list.indices){
                        if(data.list[i].kind == kind){
                            MainActivity.data.rvIndicatorsOverlays.adapter.notifyItemChanged(i)
                            break
                        }
                    }
                }
                println("Updating " + data.list[updateIndex].title + " to " + editValue.toString())
                val imm = lContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v?.windowToken, 0)

                //Update TA
                if(runTA) {
                    AsyncTask.execute {

                        val startTime: Long = System.currentTimeMillis()
                        println("Running TA")
                        if (data.list[updateIndex].separateChart) {
                            MainActivity.data.all_ta[MainActivity.data.saved_time_period].updateIndividualChartData()
                        } else {
                            MainActivity.data.all_ta[MainActivity.data.saved_time_period].updateOverlay(data.list[updateIndex])
                        }
                        val endTime: Long = System.currentTimeMillis()
                        println("updateOverlay took: " + (endTime - startTime))
                        //Update chart
                        updateChartStatus(ChartStatusData.Status.UPDATE_OVERLAYS, ChartStatusData.Type.MAIN_CHART)
                        var activity = lContext as Activity
                        activity.runOnUiThread { MainActivity.data.rvCharts.adapter.notifyDataSetChanged() }
                    }
                }
            }
            return true
        }
        return false
    }

    private fun getTextValue(position: Int, valuePosition: Int = -1): String {
        return if (isInts(position)) {

            getValue(position, valuePosition).toInt().toString()
        } else {
            getValue(position, valuePosition).toString()
        }
    }

    fun removeChartItem(type: ChartStatusData.Type ){
        var chartToRemoveIndex: Int = 0
        for(chart in MainActivity.data.chartList){
            if (chart.type == type){
                break
            }
            chartToRemoveIndex += 1
        }
        MainActivity.data.chartList.removeAt(chartToRemoveIndex)

    }
    fun updateChartStatus(status: ChartStatusData.Status, type: ChartStatusData.Type ){
        for(chart in MainActivity.data.chartList){
            if (chart.type == type){
                chart.status = status
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        lateinit var recyclerViewHolder: RecyclerView.ViewHolder
//        println("Creating View Holder")
        if(viewType == COMPACT_ITEM) {
            val view = LayoutInflater.from(parent?.context).inflate(R.layout.spinner_overlays, parent, false) as View
            recyclerViewHolder = ViewHolder(view)
            return recyclerViewHolder
        }else{
            val view = LayoutInflater.from(parent?.context).inflate(R.layout.detailed_overlay_item, parent, false) as View
            recyclerViewHolder = DetailedViewHolder(view)
            return recyclerViewHolder
        }
    }


    class ViewHolder(val view: View): RecyclerView.ViewHolder(view){
        val switch: Switch = view.findViewById(R.id.sOverlaySelect)
        val indicatorTitle: TextView = view.findViewById(R.id.tvOverlayType)
        val et1 : EditText = view.findViewById(R.id.et1)
        val et2 : EditText = view.findViewById(R.id.et2)
        val et3 : EditText = view.findViewById(R.id.et3)
        val et4 : EditText = view.findViewById(R.id.et4)
        val ivDetailedDropdown: ImageView = view.findViewById(R.id.ivDetailedDropdown)
    }
    class DetailedViewHolder(val view: View): RecyclerView.ViewHolder(view){
        val detailedTitle: TextView = view.findViewById(R.id.tvDetailedHeader)
        val et1Detail: EditText = view.findViewById(R.id.et1Detail)
        val seekBar: SeekBar = view.findViewById(R.id.sbDetail)
        val colorPicker: LinearLayout = view.findViewById(R.id.colorBox)
    }
}