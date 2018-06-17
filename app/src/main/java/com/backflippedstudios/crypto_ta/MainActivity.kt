package com.backflippedstudios.crypto_ta

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Matrix
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.github.mikephil.charting.charts.CombinedChart
import org.ta4j.core.Tick
import kotlinx.android.synthetic.main.activity_main.*
import android.os.StrictMode
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Display
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.backflippedstudios.crypto_ta.dropdownmenus.CoinSimpleArrowDropdownAdapter
import com.backflippedstudios.crypto_ta.dropdownmenus.OverlayAdapter
import com.backflippedstudios.crypto_ta.dropdownmenus.SimpleArrowDropdownAdapter
import com.backflippedstudios.crypto_ta.recyclerviews.ChartListAdapter
import com.backflippedstudios.crypto_ta.recyclerviews.RecyclerViewMargin
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


class MainActivity : AppCompatActivity() {


    private var PREFS_FILENAME = "com.backflippedstudios.saved.prefs"

    val TIME_PERIOD = "time_period"
    val COIN_SELECTED = "coin_selected"
    val CURRENCY_SELECTED = "currency_selected"
    private val EXCHANGE_SELETED = "exchange_selected"
    private var spinnerTimeFirstRun: Boolean = true
    private var spinnerCoinFirstRun: Boolean = true
    private var spinnerExchangeFirstRun: Boolean = true
    private var currencyFirstRun: Boolean = true
    private lateinit var indicatorsRecyclerView: RecyclerView
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    var context: Context = this


    object data{
        val all_ta =  Array<TechnicalAnalysis>(DataSource.Interval.values().size,{TechnicalAnalysis(ArrayList<Tick>())})
        var mChart: CombinedChart? = null
        var saved_time_period: Int = 0
        var dataSource: DataSource = DataSource()
        var coinSelected: String = "ETH"
        var exchangeSelected: String ="Gdax"
        var currencySelected: String = "USD"
        var runningTA: Boolean = false
        var endTA: Boolean = false
        var prefs: SharedPreferences? = null
        var chartList : java.util.ArrayList<ChartStatusData> = ArrayList()
        lateinit var rvCharts: RecyclerView
        lateinit var rvOverlays: RecyclerView
        var orientationSwitching: Boolean = false
        var loading = false
        var matrixLocation: Matrix? = null
        var tv_live_price : TextView? = null
        var lastOrientation: Int = Configuration.ORIENTATION_PORTRAIT

    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)


        if (data.lastOrientation != newConfig?.orientation) {
            hideIndicatorsList()
        }
        data.lastOrientation = newConfig?.orientation!!
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("_____ON_CREATE______ orientationSwitching:${data.orientationSwitching}")

        println("Loading from scratch")
        data.loading = true
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)


//        orientationEventListener = object : OrientationEventListener(
//                this, SensorManager.SENSOR_DELAY_NORMAL) {
//
//            override fun onOrientationChanged(orientation: Int) {
//                //checking if device was rotated
//                data.orientationSwitching = true
//                println("Orientation changed to ${orientation}")
//
//                if(lastOrientation != orientation)
//                    hideIndicatorsList()
//                lastOrientation = orientation
//            }
//        }
//        orientationEventListener.enable()

        setContentView(R.layout.activity_main)
        //Get last selected pre-sets
        data.prefs = this.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        data.saved_time_period = data.prefs!!.getInt(TIME_PERIOD, 0)
        data.currencySelected = data.prefs!!.getString(CURRENCY_SELECTED, "USD")
        data.coinSelected = data.prefs!!.getString(COIN_SELECTED, "ETH")
        data.exchangeSelected = data.prefs!!.getString(EXCHANGE_SELETED, "Gdax")

        b_drawer.setImageResource(R.drawable.menu_red)
        b_collapse_arrow.alpha = 0F

        //TODO: when clicked, removed indicatorsRecyclerView
        main_frame_layout.setOnClickListener({
            if (indicatorsRecyclerView.visibility == View.VISIBLE) {
                hideIndicatorsList()
            }
        })

        indicators_recycler_view.setOnClickListener({
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this.currentFocus.windowToken, 0)
        })

        //set up spinner_time_period
        val timePeriods = ArrayList<String>(resources.getStringArray(R.array.time_periods).asList())
        val spinnerAdapter = SimpleArrowDropdownAdapter(this, R.layout.spinner_dropdown_main_view_with_arrow, timePeriods)
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_main_view_no_arrow)
        spinner_time_period.adapter = spinnerAdapter
        spinner_time_period.setSelection(data.saved_time_period)
        spinner_time_period.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {
                if (spinnerTimeFirstRun) {
                    spinnerTimeFirstRun = false
                    return
                }
                //Update shared preferences
                val editor = data.prefs!!.edit()
                editor.putInt(TIME_PERIOD, position)
                editor.apply()

                if(data.saved_time_period != position){
                    data.saved_time_period = position
                    //Update the chart with the latest data from the web
                    println("update graph from period change")
                    updateCurrentGraphFromWebData(position, data.coinSelected, data.exchangeSelected, data.currencySelected, false)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) = Unit

        }

        //Update the coin's
        data.dataSource.initCoins()
        data.dataSource.initExchangesForCoin(data.coinSelected.toLowerCase())


        //Dropdown for coins
        val validCoins: Map<String, DataSource.Asset> = DataSource.data.coins.filter { !it.value.FiatLegalTender }
        val strListCoins: ArrayList<String> = ArrayList()
        for (coin in validCoins.toSortedMap().iterator()) {
            //if the symbol and name are the same, just print symbol
            if (coin.value.symboal.toUpperCase() != coin.value.name.toUpperCase()) {
                strListCoins.add(coin.value.symboal.toUpperCase() + "-" + coin.value.name.capitalize())
            } else {
                strListCoins.add(coin.value.symboal.toUpperCase())
            }
        }
        (strListCoins).sort()
        val coinAdapter = CoinSimpleArrowDropdownAdapter(this, R.layout.spinner_dropdown_main_view_with_arrow, strListCoins)
        coinAdapter.setDropDownViewResource(R.layout.spinner_dropdown_main_view_no_arrow)

        //Get index for selected coin
        val coinIndex: Int = strListCoins
                .firstOrNull { it.contains(data.coinSelected, false) }
                ?.let { strListCoins.indexOf(it) }
                ?: 0
        spinner_coin_type.adapter = coinAdapter
        spinner_coin_type.setSelection(coinIndex)
        spinner_coin_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {
                if (spinnerCoinFirstRun) {
                    spinnerCoinFirstRun = false
                    return
                }
                if (data.runningTA) data.endTA = true
                //Update shared preferences
                val editor = data.prefs!!.edit()
                val coin = p1?.findViewById<TextView>(R.id.tvHeader)?.text as String?
                editor.putString(COIN_SELECTED, coin)
                editor.apply()

                if (data.coinSelected != coin.toString()) {

                    var len = resources.getStringArray(R.array.time_periods).size - 1
                    println("Clearing all TA 0..$len")
                    for (i in 0..len) {
                        data.all_ta[i].clearAll()
                    }
                    data.coinSelected = coin.toString()
                    data.dataSource.initExchangesForCoin(data.coinSelected.toLowerCase())
                    updateExchangeDueToCoinUpdate()
                    updateCurrencyList()
//                println("update graph from coin change")
                    //Update the chart with the latest data from the web
                    updateCurrentGraphFromWebData(data.saved_time_period, data.coinSelected, data.exchangeSelected, data.currencySelected, true)

                }


            }

            override fun onNothingSelected(p0: AdapterView<*>?) = Unit

        }

        //Dropdown for exchanges
        updateExchangeDueToCoinUpdate()
        spinner_exchange_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {
                if (spinnerExchangeFirstRun) {
                    spinnerExchangeFirstRun = false
                    return
                }
                if (data.runningTA) data.endTA = true
                //Update shared preferences
                val editor = data.prefs!!.edit()
                val row = p1?.findViewById<TextView>(R.id.tvHeader)?.text as String?
                editor.putString(EXCHANGE_SELETED, row)
                editor.apply()
                println("Changing exchange to ${row.toString()} from ${data.exchangeSelected}")
                if (data.exchangeSelected != row.toString()) {

                    var len = resources.getStringArray(R.array.time_periods).size - 1
                    println("Clearing all TA 0..$len")
                    for (i in 0..len) {
                        data.all_ta[i].clearAll()
                    }
                    data.exchangeSelected = row.toString()
                    //Update the currancies
                    updateCurrencyList()
                    println("update graph from exchange change")
                    //Update the chart with the latest data from the web
                    updateCurrentGraphFromWebData(data.saved_time_period, data.coinSelected, data.exchangeSelected, data.currencySelected, true)
                }


            }

            override fun onNothingSelected(p0: AdapterView<*>?) = Unit

        }

        //Dropdown for Currency
        updateCurrencyList()
        spinner_base_currancy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {
                if (currencyFirstRun) {
                    currencyFirstRun = false
                    return
                }
                if (data.runningTA) data.endTA = true
                //Update shared preferences
                val editor = data.prefs!!.edit()
                val currency = p1?.findViewById<TextView>(R.id.tvHeader)?.text as String?
                editor.putString(CURRENCY_SELECTED, currency)
                editor.apply()
                if (data.currencySelected != currency.toString()) {

                    var len = resources.getStringArray(R.array.time_periods).size - 1
                    println("Clearing all TA 0..$len")
                    for (i in 0..len) {
                        data.all_ta[i].clearAll()
                    }

                    data.currencySelected = currency.toString()
                    //Update the chart with the latest data from the web
                    println("Currency changes to ${data.currencySelected}, attempting to update graphs. Loading: ${data.loading}")
                    updateCurrentGraphFromWebData(data.saved_time_period, data.coinSelected, data.exchangeSelected, data.currencySelected, true)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) = Unit

        }

        //Initial value of live price
        tv_live_price.text = data.dataSource.getCurrentValue(
                data.coinSelected,
                data.exchangeSelected,
                data.currencySelected).toString()
        //Populate exchange data for BTC so we can look up data for USD conversion
        if(data.coinSelected.toLowerCase() != "btc")
            data.dataSource.initExchangesForCoin("btc")
        tv_usd_value.text = "$" + data.dataSource.getUSDValue(
                data.coinSelected,
                data.exchangeSelected).toString()

        //Create task to update every 5 seconds
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                val newPrice = data.dataSource.getCurrentValue(data.coinSelected,
                        data.exchangeSelected,
                        data.currencySelected)
                var newUSDPrice = data.dataSource.getUSDValue(
                        data.coinSelected,
                        data.exchangeSelected)
                var color: Int = ContextCompat.getColor(context, R.color.md_white_1000)
                var oldPrice : Float = (tv_live_price.text.toString()).toFloat()
                if (oldPrice == newPrice) {
                    color = ContextCompat.getColor(context, R.color.md_white_1000)
                }
                else if (oldPrice > newPrice) {
                    color = ContextCompat.getColor(context, R.color.md_red_500)
                }
                else if (oldPrice < newPrice) {
                    color = ContextCompat.getColor(context, R.color.md_green_500)
                }
                runOnUiThread({

                    tv_live_price.alpha = 0F
                    tv_usd_value.alpha = 0F

                    tv_live_price.animate()
                            .alpha(0F).duration = 500
                    tv_usd_value.animate()
                            .alpha(0F).duration = 500

                    tv_live_price.setTextColor(color)
                    tv_usd_value.setTextColor(color)

                    tv_live_price.text = newPrice.toString()
                    if(newUSDPrice > 1){
                        newUSDPrice = "%.2f".format(newUSDPrice).toFloat()
                    }
                    tv_usd_value.text = "$" + newUSDPrice.toString()

                    tv_live_price.animate()
                            .alpha(1F)
                            .setStartDelay(500)
                            .duration = 500

                    tv_usd_value.animate()
                            .alpha(1F)
                            .setStartDelay(500)
                            .duration = 500
                })
            }
        }, 0, 5000); //it executes this every 5000ms

        //Add list for Overlay Adapter
        val list = ArrayList<Overlay>()
        for (item in Overlay.Kind.values()) {
            println("item " + item.name)
            var overlay = Overlay(item)
            if(overlay.kindData.visible) {
                overlay.updateColors(context)
                list.add(overlay)
            }
        }
        //Init dropdown overlays

        viewAdapter = OverlayAdapter(this, list)
        viewManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        indicatorsRecyclerView = findViewById<RecyclerView>(R.id.indicators_recycler_view).apply {
            setHasFixedSize(true)

            layoutManager = viewManager

            adapter = viewAdapter
        }
        indicatorsRecyclerView.addItemDecoration(DividerItemDecoration(this,LinearLayoutManager.VERTICAL))
        data.rvOverlays = indicatorsRecyclerView

        //Set the initial style for the chart
        if (data.chartList.isEmpty()) {
            data.chartList.add(data.chartList.size, ChartStatusData(ChartStatusData.Status.LOADING, ChartStatusData.Type.MAIN_CHART))
            for(overlay in OverlayAdapter.data.list){
                if(overlay.separateChart and overlay.selected){
                    data.chartList.add(data.chartList.size, ChartStatusData(ChartStatusData.Status.LOADING, overlay.chartType))
                }
            }
        } else {
            updateChartStatus(ChartStatusData.Status.LOADING, ChartStatusData.Type.MAIN_CHART)
            for(overlay in OverlayAdapter.data.list){
                if(overlay.separateChart and overlay.selected){
                    updateChartStatus(ChartStatusData.Status.LOADING, overlay.chartType)
                    data.chartList.add(data.chartList.size, ChartStatusData(ChartStatusData.Status.LOADING, overlay.chartType))
                }
            }
        }

        all_charts_recycler_view.adapter = ChartListAdapter(this, data.chartList)
        all_charts_recycler_view.layoutManager = LinearLayoutManager(this)
        all_charts_recycler_view.adapter.notifyDataSetChanged()
        all_charts_recycler_view.addItemDecoration(RecyclerViewMargin(this))
        data.rvCharts = all_charts_recycler_view

        swipe_to_refresh_layout.setOnRefreshListener {
            println("Updating Graph")
            if (!data.loading) {
                updateCurrentGraphFromWebData(data.saved_time_period, data.coinSelected, data.exchangeSelected, data.currencySelected, true)
            }
        }
        b_drawer.setOnClickListener {
            if(indicators_recycler_view.visibility == View.VISIBLE){
                hideIndicatorsList()
            }else{
                displayIndicatorList()
            }

        }
        b_drawer.isClickable = false

        swipe_to_refresh_layout.isRefreshing = true
//        main_frame_layout.setOnClickListener({
//            if(indicatorsRecyclerView.visibility == View.VISIBLE){
//                hideIndicatorsList()
//            }
//        })


        indicatorsRecyclerView.setBackgroundColor(ContextCompat.getColor(this, R.color.md_grey_700))
        indicatorsRecyclerView.alpha = 0F
        indicatorsRecyclerView.setHasFixedSize(true)

        indicatorsRecyclerView.isDrawingCacheEnabled = true
        indicatorsRecyclerView.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        indicatorsRecyclerView.layoutParams.height = 550

        //Update the initial graph
        data.loading = false
        println("Finished loading: ${data.loading}")

        updateCurrentGraphFromWebData(data.saved_time_period,data.coinSelected,data.exchangeSelected,data.currencySelected,false)


        //Get the rest of the data in the background so swapping between intervals is fast
//        val readThread = HandlerThread("Getting Data")
//        readThread.start()
//        var myHandler = Handler(readThread.looper)
//        myHandler.postDelayed( Runnable {
//            for (i in DataSource.Interval.values().iterator()){
//                if(i.ordinal != data.saved_time_period){
//                    val dataSource = DataSource()
//                    val ticks = dataSource.getData(i)
//                    println("Doing TA" + i.ordinal)
//                    data.all_ta[i.ordinal] = TechnicalAnalysis(ticks);
//                }
//            }
//            readThread.quit();
//        },3000)
    }

    fun updateChartStatus(status: ChartStatusData.Status, type: ChartStatusData.Type ){
        for(chart in data.chartList){
            if (chart.type == type){
                chart.status = status
            }
        }

    }
    private fun updateCurrentPrices(){
        val newPrice = data.dataSource.getCurrentValue(data.coinSelected,
                data.exchangeSelected,
                data.currencySelected)
        var newUSDPrice = data.dataSource.getUSDValue(
                data.coinSelected,
                data.exchangeSelected)

        tv_live_price.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
        tv_usd_value.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))

        tv_live_price.text = newPrice.toString()
        if(newUSDPrice > 1){
            newUSDPrice = "%.2f".format(newUSDPrice).toFloat()
        }
        tv_usd_value.text = "$" + newUSDPrice.toString()
    }

    private fun displayIndicatorList() {

        b_drawer.animate()
                .rotation(90F)
                .alpha(0F)
                .duration = 200
        b_collapse_arrow.animate()
                .rotation(540F)
                .alpha(1F)
                .duration = 200


        indicatorsRecyclerView.visibility = View.VISIBLE
        val x = windowManager.defaultDisplay.width - indicatorsRecyclerView.width
        indicatorsRecyclerView.animate()
                .alpha(1.0F)
                .translationX(x.toFloat())
                .setDuration(500)
                .setInterpolator(OvershootInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        indicatorsRecyclerView.visibility = View.VISIBLE
                    }
                }
                )
    }

    private fun hideIndicatorsList() {

        b_drawer.animate()
                .rotation(0F)
                .alpha(1F)
                .duration = 200
        b_collapse_arrow.animate()
                .rotation(0F)
                .alpha(0F)
                .duration = 200

        val x = windowManager.defaultDisplay.width * 2
        indicatorsRecyclerView.animate()
                .alpha(0.0F)
                .translationX(x.toFloat())
                .setDuration(500)
                .setInterpolator(OvershootInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        indicatorsRecyclerView.visibility = View.GONE
                    }
                }
                )
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if(this.currentFocus != null)
            imm.hideSoftInputFromWindow(this.currentFocus.windowToken,0)
    }

    private fun updateIndicatorTitle() {
        if (data.runningTA){
            println("TA Running, making title Loading")
            hideIndicatorsList()
            b_drawer.setImageResource(R.drawable.menu_yellow)
            b_drawer.isClickable = false
        }
        else{
            println("TA not running, making  title Indicators")
            b_drawer.setImageResource(R.drawable.menu_white)
            b_drawer.isClickable = true
        }
    }

    private fun updateCurrencyList() {
        val listOfAllCurrency: List<DataSource.Exchange>? = DataSource.data.coins[data.coinSelected.toLowerCase()].let { it?.exchanges?.filter { it.exchange == data.exchangeSelected.toLowerCase() } }
        println("Exchange: ${data.exchangeSelected} all currency: $listOfAllCurrency")
        val strListCurrencies: ArrayList<String> = ArrayList()
        for (i in listOfAllCurrency!!.iterator()) {
            println("adding: ${i.paring.substringAfter(data.coinSelected.toLowerCase()).toUpperCase()}")
            strListCurrencies.add(i.paring.substringAfter(data.coinSelected.toLowerCase()).toUpperCase())
        }
        (strListCurrencies).sort()
        val currencyAdapter = SimpleArrowDropdownAdapter(this, R.layout.spinner_dropdown_main_view_with_arrow, strListCurrencies)
        currencyAdapter.setDropDownViewResource(R.layout.spinner_dropdown_main_view_no_arrow)
        spinner_base_currancy.adapter = currencyAdapter

        //Check if selected currency is in list, otherwise select first one in the list
        currencyFirstRun = true // Reset flag so we dont have to refresh the graph 2 times
        if (strListCurrencies.contains(data.currencySelected)) {
            spinner_base_currancy.setSelection(strListCurrencies.indexOf(data.currencySelected))
        } else {
            spinner_base_currancy.setSelection(0)
            if (strListCurrencies.size > 0) {
                val editor = data.prefs!!.edit()
                editor.putString(CURRENCY_SELECTED, strListCurrencies[0])
                editor.apply()
                data.currencySelected = strListCurrencies[0]
            }
        }
    }

    private fun updateExchangeDueToCoinUpdate() {
        val exchanges: List<DataSource.Exchange>?
                = DataSource.data.coins[data.coinSelected.toLowerCase()].let {
            it?.exchanges
        }
        var strListExchanges: ArrayList<String> = ArrayList()
        for (i in exchanges!!.iterator()) {
            strListExchanges.add(i.exchange.capitalize())
        }
        val strSetExchanges: HashSet<String> = HashSet(strListExchanges)
        strListExchanges = ArrayList<String>(strSetExchanges)
        Collections.sort(strListExchanges)
        val exchangeAdapter = SimpleArrowDropdownAdapter(this, R.layout.spinner_dropdown_main_view_with_arrow, strListExchanges)
        exchangeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_main_view_no_arrow)

        spinner_exchange_type.adapter = exchangeAdapter
        //reset first selection on Exchange spinner so we dont update the graph 2 times
        spinnerExchangeFirstRun = true
        //Check if previous exchange is still in the list, if not select the first one
        if(strListExchanges.contains(data.exchangeSelected)) {
            spinner_exchange_type.setSelection(strListExchanges.indexOf(data.exchangeSelected))
        }else{
            spinner_exchange_type.setSelection(0)
            val editor = data.prefs!!.edit()
            editor.putString(EXCHANGE_SELETED, strListExchanges[0])
            editor.apply()
            data.exchangeSelected = strListExchanges[0]
        }
    }

    private fun updateCurrentGraphFromWebData(position: Int, coin: String, exchange: String, currency: String, forceUpdate: Boolean) {
        println("Attempting  to update graph, Pos:$position, Coin:$coin, exchange:$exchange, Currency:$currency, forceUpdate:$forceUpdate")
        swipe_to_refresh_layout.isRefreshing = true
        updateCurrentPrices()
        AsyncTask.execute({
            println("Task running")
            spinner_base_currancy.isEnabled = false
            spinner_coin_type.isEnabled = false
            spinner_exchange_type.isEnabled = false
            spinner_time_period.isEnabled = false
            if (data.all_ta[position].candlestickData.size == 0 || forceUpdate) {
                runOnUiThread({
                    updateChartStatus(ChartStatusData.Status.LOADING, ChartStatusData.Type.MAIN_CHART)
//                    data.chartList[0] = ChartStatusData(ChartStatusData.Status.LOADING,ChartStatusData.Type.MAIN_CHART)
                    for(overlay in OverlayAdapter.data.list){
                        if((overlay.kind == Overlay.Kind.Volume_Bars )and overlay.selected){
                            updateChartStatus(ChartStatusData.Status.LOADING, overlay.chartType)
                        }
                    }
                    all_charts_recycler_view.adapter.notifyDataSetChanged()
                })


                val interval = DataSource.Interval.values()[position]
                println("Getting data from web")
                val ticks = data.dataSource.getData(coin, exchange,currency, interval)
                Log.d("DEBUG","Finished getting data")
                if(ticks.size < 10){
                    runOnUiThread({
                        updateChartStatus(ChartStatusData.Status.UPDATE_FAILED, ChartStatusData.Type.MAIN_CHART)
                        for(overlay in OverlayAdapter.data.list){
                            if(overlay.separateChart and overlay.selected){
                                updateChartStatus(ChartStatusData.Status.UPDATE_FAILED, overlay.chartType)
                            }
                        }

                        all_charts_recycler_view.adapter.notifyDataSetChanged()
                    })
                    b_drawer.setImageResource(R.drawable.menu_red)
                    b_drawer.isClickable = false

                    swipe_to_refresh_layout.isRefreshing = false
                    spinner_base_currancy.isEnabled = true
                    spinner_coin_type.isEnabled = true
                    spinner_exchange_type.isEnabled = true
                    spinner_time_period.isEnabled = true

                    return@execute
                }
                else {
                    println("Doing TA $position")
                    data.runningTA = true
                    data.all_ta[position].clearAll()
                    data.all_ta[position].updateCandlestickData(ticks,addTimeSeriesData = true)

                    runOnUiThread({
                        updateChartStatus(ChartStatusData.Status.UPDATE_CANDLESTICKS, ChartStatusData.Type.MAIN_CHART)
                        for(overlay in OverlayAdapter.data.list){
                            if(overlay.separateChart and overlay.selected){
                                updateChartStatus(ChartStatusData.Status.INITIAL_LOAD, overlay.chartType)
                            }
                        }
                        all_charts_recycler_view.adapter.notifyDataSetChanged()
                    })


                    //Disabling because it looks redundant
                    runOnUiThread( {updateIndicatorTitle()})
                    data.all_ta[position] = TechnicalAnalysis(ticks)
                    data.endTA = false

                    data.runningTA = false
                    println("TA Finished")
                    runOnUiThread( {
                        updateIndicatorTitle()
                    })
                }
            }


            runOnUiThread( {
                println("TA Done, time to update recycler")
                runOnUiThread( {
                    updateIndicatorTitle()
                })

                updateChartStatus(ChartStatusData.Status.UPDATE_OVERLAYS, ChartStatusData.Type.MAIN_CHART)
                data.all_ta[position].updateIndividualChartData()
                for(overlay in OverlayAdapter.data.list){
                    if(overlay.separateChart and overlay.selected){
                        updateChartStatus(ChartStatusData.Status.UPDATE_CHART, overlay.chartType)
                    }
                }
                all_charts_recycler_view.adapter.notifyDataSetChanged()

                spinner_base_currancy.isEnabled = true
                spinner_coin_type.isEnabled = true
                spinner_exchange_type.isEnabled = true
                spinner_time_period.isEnabled = true
                swipe_to_refresh_layout.isRefreshing = false
            })



        })
    }
}
