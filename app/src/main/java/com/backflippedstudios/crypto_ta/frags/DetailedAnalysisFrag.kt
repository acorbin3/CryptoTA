package com.backflippedstudios.crypto_ta.frags

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.ConnectivityManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import com.backflippedstudios.crypto_ta.*
import com.backflippedstudios.crypto_ta.customchartmods.ChartStatusData
import com.backflippedstudios.crypto_ta.data.DataSource
import com.backflippedstudios.crypto_ta.dropdownmenus.CoinSimpleArrowDropdownAdapter
import com.backflippedstudios.crypto_ta.dropdownmenus.OverlayAdapter
import com.backflippedstudios.crypto_ta.dropdownmenus.SimpleArrowDropdownAdapter
import com.backflippedstudios.crypto_ta.recyclerviews.ChartListAdapter
import com.github.mikephil.charting.charts.CombinedChart
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.detail_analysis_main_layout.*
import kotlinx.android.synthetic.main.detail_analysis_main_layout.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.rm3l.maoni.Maoni
import org.ta4j.core.Tick
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.io.File
import java.io.FileOutputStream
import java.lang.Thread.sleep
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet


class DetailedAnalysisFrag : Fragment() {


    private var PREFS_FILENAME = "com.backflippedstudios.saved.prefs"

    val TIME_PERIOD = "time_period"
    val COIN_SELECTED = "coin_selected"
    val CURRENCY_SELECTED = "currency_selected"
    private val EXCHANGE_SELETED = "exchange_selected"
    val USER_UUID = "user_uuid"
    private var spinnerTimeFirstRun: Boolean = true
    private var spinnerCoinFirstRun: Boolean = true
    private var spinnerExchangeFirstRun: Boolean = true
    private lateinit var layout: View
    val MY_PERMISSIONS_REQUEST_WRITE_FILE = 0
    val MY_PERMISSIONS_REQUEST_READ_FILE = 0
    private val regressionTesting = false

    lateinit var mainView: View
    val title = "Detail Analsis"


    object data {
        val all_ta = Array<TechnicalAnalysis>(DataSource.Interval.values().size, { TechnicalAnalysis(ArrayList<Tick>()) })
        var mChart: CombinedChart? = null
        var saved_time_period: Int = 0
        var coinSelected: String = "ETH"
        var exchangeSelected: String = "Gdax"
        var currencySelected: String = "USD"
        var runningTA: Lock = ReentrantLock()
        var endTA: Boolean = false
        var prefs: SharedPreferences? = null
        var chartList: java.util.ArrayList<ChartStatusData> = ArrayList()
        lateinit var rvCharts: RecyclerView
        lateinit var rvIndicatorsOverlays: RecyclerView
        var loading = false
        var matrixLocation: Matrix? = null
        var tv_live_price: TextView? = null
        var lastOrientation: Int = Configuration.ORIENTATION_PORTRAIT
        var runningOrientationLoad: Boolean = false
        var isInitialLoadComplete: Boolean = false
        lateinit var ivDrawer: ImageView
        lateinit var ivCollapseArrow: ImageView
        var displayWidth: Int = 0
        var lastMainTouchChart: Overlay.Kind? = null
        var uuid: String = ""
        lateinit var mFirebaseAnalytics: FirebaseAnalytics
        val systemUIVisibilityPermissions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE
//                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        var currentCoinRatio: Float = 0.0F
        var currentUSDValue: Float = 0.0F

    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        if (data.lastOrientation != newConfig?.orientation) {
            data.runningOrientationLoad = true
        }
        data.lastOrientation = newConfig?.orientation!!

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return super.onCreateView(inflater, container, savedInstanceState)
        data.endTA = true

        data.lastOrientation = resources.configuration.orientation
        println("_____ON_CREATE______ data.runningOrientationLoad: ${data.runningOrientationLoad}")

        val openPlayStore: Boolean? = activity?.intent?.getStringExtra("openPlayStore")?.toBoolean()
        if (openPlayStore != null && openPlayStore) {
            println("Found open play store")
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${activity?.packageName}")))
            } catch (anfe: android.content.ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${activity?.packageName}")))
            }
        }



        println("Loading from scratch")
        data.displayWidth = activity?.windowManager?.defaultDisplay?.width!! * 2
        data.loading = true
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        activity?.window?.decorView?.systemUiVisibility = data.systemUIVisibilityPermissions
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        mainView = inflater.inflate(R.layout.detail_analysis_main_layout, container, false)
        layout = mainView.findViewById(R.id.main_frame_layout)
        //Get last selected pre-sets
        data.prefs = activity?.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        data.saved_time_period = data.prefs!!.getInt(TIME_PERIOD, 0)
        data.currencySelected = data.prefs!!.getString(CURRENCY_SELECTED, "USD")
        data.coinSelected = data.prefs!!.getString(COIN_SELECTED, "ETH")
        data.exchangeSelected = data.prefs!!.getString(EXCHANGE_SELETED, "Gdax")

        data.uuid = data.prefs!!.getString(USER_UUID, UUID.randomUUID().toString())
        mainView.b_drawer.setImageResource(R.drawable.menu_red)
        mainView.b_collapse_arrow.alpha = 0F
        data.ivDrawer = mainView.b_drawer
        data.ivCollapseArrow = mainView.b_collapse_arrow

        val internetOn = isInternetOn()
        if (internetOn) {
            mainView.iv_share_screenshot.setOnClickListener {

                requestWritePermission()

            }
        }
        mainView.iv_feedback.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("uuid", data.uuid)
            data.mFirebaseAnalytics.logEvent("FeebackSelected", bundle)
            var mMaoniBuilder = Maoni.Builder(activity,"${BuildConfig.APPLICATION_ID}.fileprovider")
                    .withSharedPreferences("${activity?.packageName}_preferences")
            var mMaoni = mMaoniBuilder.withScreenCapturingFeature(true)
                    .withDefaultToEmailAddress("acorbin3@gmail.com")
                    .withLogsCapturingFeature(true)
                    .build()
            mMaoni.start(activity)


        }

        //Init Cloudinary
//        var config = HashMap<String,String>()
//        config.put("cloud_name", "dqevz3bfx");
//        MediaManager.init(activity, config);

        mainView.swipe_to_refresh_market_cap.isRefreshing = true
//        var resultFirestoreItemCount: Task<QuerySnapshot>? = null
        if (internetOn) {
            //set up spinner_time_period
            val timePeriods = ArrayList<String>(resources.getStringArray(R.array.time_periods).asList())
            val spinnerAdapter = SimpleArrowDropdownAdapter(activity, R.layout.spinner_dropdown_main_view_with_arrow, timePeriods)
            spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_main_view_no_arrow)
            mainView.spinner_time_period.adapter = spinnerAdapter
            mainView.spinner_time_period.setSelection(data.saved_time_period)
            mainView.spinner_time_period.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {
                    if (spinnerTimeFirstRun) {
                        spinnerTimeFirstRun = false
                        return
                    }


                    //Update shared preferences
                    val editor = data.prefs!!.edit()
                    editor.putInt(TIME_PERIOD, position)
                    editor.apply()

                    if (data.saved_time_period != position) {
                        data.saved_time_period = position
                        data.endTA = true
                        data.runningTA.lock()
                        data.runningTA.unlock()
                        //Update the chart with the latest data from the web
                        println("update graph from period change")
                        updateCurrentGraphFromWebData(position, data.coinSelected, data.exchangeSelected, data.currencySelected, false)
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) = Unit

            }

            //Update the coin's
            if (data.chartList.size == 0 && internetOn && !data.isInitialLoadComplete) {//Have not been loaded before
//                data.dataSource.initCoinsV2()

                //result = data.dataSource.intCoins3()
//                println("$$$$$$$\$Getting FirestoreCount")
//                resultFirestoreItemCount = data.dataSource.getFirestoreItemCount(activity?.applicationContext!!)
//                println("Firestore coutn $resultFirestoreItemCount")


//                data.dataSource.initExchangesForCoin(data.coinSelected.toLowerCase())
            }
        }
        if (!data.isInitialLoadComplete) {
//            println("Wait for lock Detailed: ${DataSource.data.isCoinsPopulatedLOCK.tryLock()}")
//            DataSource.data.isCoinsPopulatedLOCK.lock()
//            DataSource.data.isCoinsPopulatedLOCK.unlock()
//            processInit(internetOn)
//            resultFirestoreItemCount?.addOnSuccessListener { it ->
////                DataSource.data.isCoinsPopulatedLOCK.lock()
////                println("Locking: ${DataSource.data.isCoinsPopulatedLOCK.tryLock()}")
//                println("$$$$$$$\$Getting DAO count")
//                var daoCount = data.dataSource.getDAOItemCount(activity?.applicationContext!!)
//                it.forEach {
//                    println("DAO count:$daoCount FirestoreCount: ${it.data["count"].toString()}")
//                    if (it.data["count"].toString().toInt() == daoCount) {
//                        data.dataSource.loadFromDAO(activity?.applicationContext!!)
////                        DataSource.data.isCoinsPopulatedLOCK.unlock()
//                        processInit(internetOn)
//                    } else {
//                        data.dataSource.clearDAO(activity?.applicationContext!!)
//                        var result = data.dataSource.intCoins3(activity?.applicationContext!!)
//                        result.addOnSuccessListener {
////                            DataSource.data.isCoinsPopulatedLOCK.unlock()
//                            processInit(internetOn)
//                        }
//                    }
//                    return@addOnSuccessListener
//                }
//            }
        } else {
//            for (overlay in OverlayAdapter.data.list) {
//                if (!overlay.separateChart and overlay.selected) {
//                    updateChartStatus(ChartStatusData.Status.UPDATE_CHART, ChartStatusData.Type.MAIN_CHART, overlay.kind)
//                }
//            }
            processInit(internetOn)
        }

        return mainView
    }

    fun processInit(internetOn: Boolean) = runBlocking{
        var strListCoins: ArrayList<String> = ArrayList()
        //Dropdown for coins
        var strListCoins1 = strListCoins
        if (internetOn) {

            val validCoins: Map<String, DataSource.Asset> = DataSource.data.coins.filter { !it.value.FiatLegalTender }
            var coinPairs: HashSet<String> = HashSet()
            for (coin in validCoins.toSortedMap().iterator()) {
                //if the symbol and name are the same, just print symbol
                //                        println("Coin: ${coin.value.name}")
                for (exchange in coin.value.exchanges) {
                    //                            println("\tpair: ${exchange.paring}")
                    if (exchange.paring.contains(coin.value.symbol)) {
                        //                        println(exchange.paring.replace(coin.value.symbol,coin.value.symbol+"/"))
                        coin.value.name.replace("/", "_")
                        var name = coin.value.name
                        name = name.replace("/", "_")
                        //                                println("name: $name")
                        if (coin.value.symbol.toUpperCase() != name.toUpperCase()) {
                            coinPairs.add(exchange.paring.toUpperCase().replaceFirst(coin.value.symbol.toUpperCase(),
                                    coin.value.symbol.toUpperCase() + "-" + name.capitalize() + "/"))
                        } else {
                            coinPairs.add(exchange.paring.replace(coin.value.symbol, coin.value.symbol + "/").toUpperCase())
                        }

                    }
                }

            }

            strListCoins1 = ArrayList(coinPairs)
            (strListCoins1).sort()
            println(strListCoins1)


            val coinAdapter = CoinSimpleArrowDropdownAdapter(activity?.applicationContext, R.layout.spinner_dropdown_main_view_with_arrow, strListCoins1)
            coinAdapter.setDropDownViewResource(R.layout.spinner_dropdown_main_view_no_arrow)

            //Get index for selected coin
            val coinIndex: Int = coinPairs

                    .firstOrNull {
                        var coinSelect = it.substringBefore("/")
                        if (coinSelect.contains("-")) {
                            coinSelect = coinSelect.substringBefore("-")
                        }
                        val currencySelect = it.substringAfter("/")
                        //                                println("$coinSelect == ${data.coinSelected}; $currencySelect == ${data.currencySelected}")
                        coinSelect.contentEquals(data.coinSelected) && currencySelect.contentEquals(data.currencySelected)
                    }
                    ?.let { strListCoins1.indexOf(it) }
                    ?: 0
            mainView.spinner_coin_type.adapter = coinAdapter
            mainView.spinner_coin_type.setSelection(coinIndex)
            mainView.spinner_coin_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {
                    if (spinnerCoinFirstRun) {
                        spinnerCoinFirstRun = false
                        return
                    }
                    //Update shared preferences
                    val editor = data.prefs!!.edit()
                    val coinPair = p1?.findViewById<TextView>(R.id.tvHeader)?.text as String?

                    //Remove first coin description
                    //                            val dashIndex = coinPair?.indexOf("-")
                    //                            val slashIndex = coinPair?.indexOf("/")
                    //                            if(dashIndex!! > 0) {
                    //                                coinPair = coinPair?.removeRange(dashIndex!!, slashIndex!!)
                    //                            }
                    //Get the selected coin and currency
                    var selectedCoin = coinPair?.substringBefore("/")!!
                    if (selectedCoin.contains("-")) {
                        selectedCoin = selectedCoin.substringBefore("-")
                    }
                    val selectedCurrency = coinPair.substringAfter("/")
                    editor.putString(COIN_SELECTED, selectedCoin)
                    editor.putString(CURRENCY_SELECTED, selectedCurrency)
                    editor.apply()
                    val bundle = Bundle()
                    bundle.putString("uuid", data.uuid)
                    bundle.putString("base_coin", selectedCoin)
                    bundle.putString("currency", selectedCurrency)
                    data.mFirebaseAnalytics.logEvent("changing_coin", bundle)

                    if (data.coinSelected != selectedCoin || data.currencySelected != selectedCurrency) {

                        var len = resources.getStringArray(R.array.time_periods).size - 1
                        println("Clearing all TA 0..$len")
                        for (i in 0..len) {
                            data.all_ta[i].clearAll()
                        }
                        data.coinSelected = selectedCoin
                        data.currencySelected = selectedCurrency
                        //                        data.dataSource.initExchangesForCoin(data.coinSelected.toLowerCase())
                        updateExchangeDueToCoinUpdate()
                        //                        updateCurrencyList()
                        //                println("update graph from coin change")
                        //Update the chart with the latest data from the web
                        println("Trying lock")
                        data.endTA = true
                        data.runningTA.lock()
                        data.runningTA.unlock()
                        println("Time to end ta: ${data.endTA}")
                        updateCurrentGraphFromWebData(data.saved_time_period, data.coinSelected, data.exchangeSelected, data.currencySelected, true && !data.isInitialLoadComplete)

                    }


                }

                override fun onNothingSelected(p0: AdapterView<*>?) = Unit

            }
        }
        //Dropdown for exchanges
        if (internetOn) {
            updateExchangeDueToCoinUpdate()
            mainView.spinner_exchange_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {
                    if (spinnerExchangeFirstRun) {
                        spinnerExchangeFirstRun = false
                        return
                    }

                    //Update shared preferences
                    val editor = data.prefs!!.edit()
                    val row = p1?.findViewById<TextView>(R.id.tvHeader)?.text as String?
                    editor.putString(EXCHANGE_SELETED, row)
                    editor.apply()
                    val bundle = Bundle()
                    bundle.putString("uuid", data.uuid)
                    bundle.putString("select_exchange", row.toString())
                    data.mFirebaseAnalytics.logEvent("changing_exchange", bundle)
                    println("Changing exchange to ${row.toString()} from ${data.exchangeSelected}")
                    if (data.exchangeSelected != row.toString()) {

                        var len = resources.getStringArray(R.array.time_periods).size - 1
                        println("Clearing all TA 0..$len")
                        for (i in 0..len) {
                            data.all_ta[i].clearAll()
                        }
                        data.exchangeSelected = row.toString()
                        //Update the currancies
                        //                        updateCurrencyList()
                        println("update graph from exchange change")
                        //Update the chart with the latest data from the web
                        data.endTA = true
                        data.runningTA.lock()
                        data.runningTA.unlock()
                        updateCurrentGraphFromWebData(data.saved_time_period, data.coinSelected, data.exchangeSelected, data.currencySelected, true)
                    }


                }

                override fun onNothingSelected(p0: AdapterView<*>?) = Unit

            }

        }

        //Live price
        if (internetOn) {

            //Initial value of live price
            mainView.tv_live_price.text = MainActivity.data.dataSource.getCurrentValue(
                    data.coinSelected,
                    data.exchangeSelected,
                    data.currencySelected).toString()
            //Populate exchange data for BTC so we can look up data for USD conversion
            if (data.coinSelected.toLowerCase() != "btc")
                MainActivity.data.dataSource.initExchangesForCoin("btc")
            if (!data.currencySelected.toLowerCase().contains("usd")) {
                mainView.tv_usd_value.text = "$" + MainActivity.data.dataSource.getUSDValue(
                        data.coinSelected,
                        data.exchangeSelected).toString()
            } else {
                //Case for when we are already looking at BTC to USD, no need to do conversion
                mainView.tv_usd_value.text = "$" + MainActivity.data.dataSource.getCurrentValue(
                        data.coinSelected,
                        data.exchangeSelected,
                        data.currencySelected).toString()
            }

            //Create task to update every 5 seconds
            val timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    try {
                        val newPrice = MainActivity.data.dataSource.getCurrentValue(data.coinSelected,
                                data.exchangeSelected,
                                data.currencySelected)
                        newPrice.toString().replace(",", ".").toFloat()
                        var newUSDPrice = MainActivity.data.dataSource.getUSDValue(
                                data.coinSelected,
                                data.exchangeSelected)
                        newUSDPrice.toString().replace(",", ".").toFloat()

                        var color: Int = ContextCompat.getColor(activity?.applicationContext!!, R.color.md_white_1000)
                        val oldPrice: Float = data.currentCoinRatio
                        if (oldPrice == newPrice) {
                            color = ContextCompat.getColor(activity?.applicationContext!!, R.color.md_white_1000)
                        } else if (oldPrice > newPrice) {
                            color = ContextCompat.getColor(activity?.applicationContext!!, R.color.md_red_500)
                        } else if (oldPrice < newPrice) {
                            color = ContextCompat.getColor(activity?.applicationContext!!, R.color.md_green_500)
                        }


                        activity?.runOnUiThread {
                            try {
                                mainView.tv_live_price.setTextColor(color)
                                mainView.tv_usd_value.setTextColor(color)

                                var coinPair = data.coinSelected + "/" + data.currencySelected + " "

                                var diff = (newPrice - oldPrice).toString()

                                diff.replace(",", ".")
                                diff = "%.6f".format(diff.toFloat())
                                //                                    println("$diff $oldPrice $newPrice")
                                mainView.tv_live_price.text = coinPair + newPrice.toString() +
                                        "(" + (diff) + ")"

                                if (newUSDPrice > 1) {
                                    newUSDPrice = "%.2f".format(newUSDPrice).toFloat()
                                } else {
                                    newUSDPrice = "%.4f".format(newUSDPrice).toFloat()
                                }
                                diff = (newUSDPrice - data.currentUSDValue).toString()
                                diff.replace(",", ".")
                                diff = "%.4f".format(diff.toFloat())
                                if (!data.currencySelected.contains("USD")) {
                                    tv_usd_value.text = data.coinSelected + "/USD $" + newUSDPrice.toString() +
                                            "(" + diff + ")"
                                } else {
                                    //Case for when we are already looking at BTC to USD, no need to do conversion
                                    mainView.tv_usd_value.text = mainView.tv_live_price.text
                                }

                                data.currentCoinRatio = newPrice
                                data.currentUSDValue = newUSDPrice
                            } catch (e: Exception) {
                            }

                        }
                    } catch (e: Exception) {
                    }
                }
            }, 0, 5000) //it executes this every 5000ms

            //Add list for Overlay Adapter
            val list = ArrayList<Overlay>()
            val allList = HashMap<Overlay.Kind, Overlay>()
            for (item in Overlay.Kind.values()) {
                //println("item " + item.name)
                var overlay = Overlay(activity?.applicationContext!!, item)
                if (overlay.kindData.visible) {
                    list.add(overlay)
                }
                allList[item] = overlay
            }
            //Init dropdown overlays

            mainView.indicators_recycler_view.adapter = OverlayAdapter(activity?.applicationContext!!, list, allList)
            mainView.indicators_recycler_view.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            mainView.indicators_recycler_view.setHasFixedSize(true)
            data.rvIndicatorsOverlays = mainView.indicators_recycler_view
            data.rvIndicatorsOverlays.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))

            //Set the initial style for the chart
            if (data.chartList.isEmpty()) {
                data.chartList.add(data.chartList.size, ChartStatusData(ChartStatusData.Status.LOADING, ChartStatusData.Type.MAIN_CHART, Overlay.Kind.None))
                for (overlay in OverlayAdapter.data.list) {
                    if (overlay.separateChart and overlay.selected) {
                        data.chartList.add(data.chartList.size, ChartStatusData(ChartStatusData.Status.LOADING, ChartStatusData.Type.SEPARATE_CHART, overlay.kind))
                    }
                }
            } else {
                updateChartStatus(ChartStatusData.Status.LOADING, ChartStatusData.Type.MAIN_CHART, Overlay.Kind.None)
                for (overlay in OverlayAdapter.data.list) {
                    if (overlay.separateChart and overlay.selected) {
                        updateChartStatus(ChartStatusData.Status.LOADING, ChartStatusData.Type.SEPARATE_CHART, overlay.kind)
                    }
                }
            }
        }


        //Internet not on
        if (!internetOn) {
            if (data.chartList.isEmpty()) {
                data.chartList.add(ChartStatusData(ChartStatusData.Status.INTERNET_OUT, ChartStatusData.Type.MAIN_CHART, Overlay.Kind.None))
            }
        }

        mainView.all_charts_recycler_view.adapter = ChartListAdapter(activity?.applicationContext!!, data.chartList)
        mainView.all_charts_recycler_view.layoutManager = LinearLayoutManager(activity)
        (mainView.all_charts_recycler_view.adapter as ChartListAdapter).notifyDataSetChanged()
        //                all_charts_recycler_view.addItemDecoration(RecyclerViewMargin(activity))
        mainView.all_charts_recycler_view.setOnClickListener {
            if (indicators_recycler_view.visibility == View.VISIBLE) {
                hideIndicatorsList()
            }
        }

        data.rvCharts = mainView.all_charts_recycler_view

        //On swipe down, refresh overlays
        if (internetOn) {
            mainView.swipe_to_refresh_market_cap.setOnRefreshListener {
                println("Updating Graph")
                if (!data.loading) {
                    data.endTA = true
                    data.runningTA.lock()
                    data.runningTA.unlock()
                    updateCurrentGraphFromWebData(data.saved_time_period, data.coinSelected, data.exchangeSelected, data.currencySelected, true)
                }
            }
        }

        mainView.b_drawer.setOnClickListener {
            if (mainView.indicators_recycler_view.visibility == View.VISIBLE) {
                hideIndicatorsList()
            } else {
                displayIndicatorList()
            }

        }

        mainView.b_drawer.isClickable = false

        //Updating overlays
        if (internetOn) {
            mainView.swipe_to_refresh_market_cap.isRefreshing = true

            data.rvIndicatorsOverlays.setBackgroundColor(ContextCompat.getColor(activity?.applicationContext!!, R.color.md_grey_700))
            data.rvIndicatorsOverlays.alpha = 0F
            data.rvIndicatorsOverlays.setHasFixedSize(true)

            data.rvIndicatorsOverlays.isDrawingCacheEnabled = true
            data.rvIndicatorsOverlays.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
            data.rvIndicatorsOverlays.layoutParams.height = 550

            //Update the initial graph
            data.loading = false
            println("Finished loading: ${data.loading}")

            if (!regressionTesting) {
                updateCurrentGraphFromWebData(data.saved_time_period, data.coinSelected, data.exchangeSelected, data.currencySelected, false)
            }


        }

        data.runningOrientationLoad = false
        data.isInitialLoadComplete = true

        if (regressionTesting && internetOn) {
            GlobalScope.launch {
                for (pair in strListCoins1) {

                    var selectedCoin = pair.substringBefore("/")
                    if (selectedCoin.contains("-")) {
                        selectedCoin = selectedCoin.substringBefore("-")
                    }
                    val selectedCurrency = pair.substringAfter("/")
                    data.coinSelected = selectedCoin
                    data.currencySelected = selectedCurrency

                    val coinIndex: Int = strListCoins1

                            .firstOrNull {
                                var coinSelect = it.substringBefore("/")
                                if (coinSelect.contains("-")) {
                                    coinSelect = coinSelect.substringBefore("-")
                                }
                                var currencySelect = it.substringAfter("/")
                                //                                println("$coinSelect == ${data.coinSelected}; $currencySelect == ${data.currencySelected}")
                                coinSelect.contentEquals(data.coinSelected) && currencySelect.contentEquals(data.currencySelected)
                            }
                            ?.let { strListCoins1.indexOf(it) }
                            ?: 0
                    activity?.runOnUiThread {
                        mainView.spinner_coin_type.setSelection(coinIndex)
                    }
                    updateExchangeDueToCoinUpdate(false)
                    executeGraphUpdate(data.saved_time_period, selectedCoin, data.exchangeSelected, selectedCurrency, true, true)

                    var loadingComplete = false
                    while (!loadingComplete) {
                        loadingComplete = ChartListAdapter.data.status == ChartStatusData.Status.LOADING_COMPLETE
                        sleep(20)
                    }

                }
            }
        }
        hideIndicatorsList()
    }

    private fun getNavigationBarHeight(context: Context, orientation: Int): Int {
        val resources = context.resources

        val id = resources.getIdentifier(
                if (orientation == Configuration.ORIENTATION_PORTRAIT) "navigation_bar_height" else "navigation_bar_height_landscape",
                "dimen", "android")
        return if (id > 0) {
            resources.getDimensionPixelSize(id)
        } else 0
    }

    private fun createScreenshotAndSendImage(it: View, resizeForVertical: Boolean) {
        val bundle = Bundle()
        bundle.putString("uuid", data.uuid)
        bundle.putString("CoinPair", data.coinSelected + "/" + data.currencySelected)
        data.mFirebaseAnalytics.logEvent("SavingScreenshot", bundle)
        var bitmap = screenShot(it.rootView)
//        println("Before Bitmap size: h ${bitmap.height} w ${bitmap.width}")

        if (resizeForVertical) {
            val aspectRatio: Double = bitmap.width.toDouble() / bitmap.height.toDouble()
            // 1080×1350
            val adjustedHeight = 1350
            val adjustedWidth = Math.round((adjustedHeight * aspectRatio)).toInt()
//            println("Aspect ratio: $aspectRatio width: $adjustedWidth")
            val scalledBitmap = Bitmap.createScaledBitmap(bitmap, adjustedWidth, adjustedHeight, false)
            bitmap = Bitmap.createBitmap(1080, 1350, Bitmap.Config.ARGB_8888)
            val diffOnWidth = 1080 - adjustedWidth
            val offsetToCenterImage = (diffOnWidth / 2).toFloat()
            val comboImage = Canvas(bitmap)
            comboImage.drawBitmap(scalledBitmap, offsetToCenterImage, 0F, null)
        }

//        println("After Bitmap size: h ${bitmap.height} w ${bitmap.width}")

        val current = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd__HH_mm_ss")
        val formatted = current.format(formatter)
        val file = saveBitmap(bitmap, "CryptoTA_$formatted.png")
//        println("File saved: " + file.absolutePath)
        //            val uri = Uri.fromFile(File(file.absolutePath))
        val apkURI = FileProvider.getUriForFile(activity?.applicationContext!!,
                activity?.applicationContext!!.applicationContext.packageName + ".provider", file)

        var shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_STREAM, apkURI)
        shareIntent.type = "image/*"
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(shareIntent, "share via"))
    }

    private fun saveBitmap(bitmap: Bitmap, fileName: String): File {
        val path: String = Environment.getExternalStorageDirectory().absolutePath
        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, fileName)
        try {
            var fOut = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
            fOut.flush()
            fOut.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file
    }

    private fun screenShot(view: View): Bitmap {
        val heigth = getNavigationBarHeight(activity?.applicationContext!!, activity?.applicationContext!!.resources.configuration.orientation)
        lateinit var bitmap: Bitmap
        if (activity?.applicationContext!!.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            bitmap = Bitmap.createBitmap(view.width, view.height - heigth, Bitmap.Config.ARGB_8888)
        } else {
            bitmap = Bitmap.createBitmap(view.width - heigth, view.height, Bitmap.Config.ARGB_8888)
        }
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun requestWritePermission() {
        // Permission has not been granted and must be requested.

        if (ActivityCompat.checkSelfPermission(activity?.applicationContext!!,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                val snackbar = Snackbar.make(layout,
                        "Requesting write permissions",
                        Snackbar.LENGTH_INDEFINITE)
                snackbar.setAction("Requesting permissions to write screenshot " +
                        "to device to be able to share screenshot") {
                    requestPermissions(
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            MY_PERMISSIONS_REQUEST_WRITE_FILE)
                }
            } else {
                // No explanation needed, we can request the permission.
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        MY_PERMISSIONS_REQUEST_WRITE_FILE)
            }
        } else {
            requestImageResize()
        }
    }

    private fun requestImageResize() {
        if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val alertDialogBuilder = AlertDialog.Builder(activity)
            alertDialogBuilder.setTitle("Resize Image")
            alertDialogBuilder.setMessage("Would you like to resize image for Instagram?")
            alertDialogBuilder.setPositiveButton("Yes") { dialogInterface, i ->
                createScreenshotAndSendImage(activity?.window?.decorView?.rootView!!, true)
            }
            alertDialogBuilder.setNegativeButton("No") { dialogInterface, i ->
                createScreenshotAndSendImage(activity?.window?.decorView?.rootView!!, false)
            }
            val dialog: AlertDialog = alertDialogBuilder.create()
            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(activity?.applicationContext!!, R.color.md_black_1000))
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(activity?.applicationContext!!, R.color.md_black_1000))
        } else {
            createScreenshotAndSendImage(activity?.window?.decorView?.rootView!!, false)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_WRITE_FILE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Snackbar.make(layout,
                            "Permission granted",
                            Snackbar.LENGTH_SHORT).show()

                    requestImageResize()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Snackbar.make(layout,
                            "Permission denied",
                            Snackbar.LENGTH_SHORT).show()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    fun updateChartStatus(status: ChartStatusData.Status, type: ChartStatusData.Type, kind: Overlay.Kind = Overlay.Kind.None) {
        for (chart in data.chartList) {

            if (type == ChartStatusData.Type.MAIN_CHART && chart.type == type){
                chart.status = status
                chart.kind = kind
            }
            else if (chart.type == type && chart.kind == kind) {
                chart.status = status
            }
        }
    }

    private fun updateCurrentPrices() {
        try {
            val newPrice = MainActivity.data.dataSource.getCurrentValue(data.coinSelected,
                    data.exchangeSelected,
                    data.currencySelected)
            var newUSDPrice = MainActivity.data.dataSource.getUSDValue(
                    data.coinSelected,
                    data.exchangeSelected)

            mainView.tv_live_price.setTextColor(ContextCompat.getColor(activity?.applicationContext!!, R.color.md_white_1000))
            mainView.tv_usd_value.setTextColor(ContextCompat.getColor(activity?.applicationContext!!, R.color.md_white_1000))

            mainView.tv_live_price.text = newPrice.toString()
            if (newUSDPrice > 1) {
                newUSDPrice = "%.2f".format(newUSDPrice).toFloat()
            }
            mainView.tv_usd_value.text = "$" + newUSDPrice.toString()
        } catch (e: Exception) {

        }
    }

    private fun displayIndicatorList() {

        mainView.b_drawer.animate()
                .rotation(90F)
                .alpha(0F)
                .duration = 200
        mainView.b_collapse_arrow.animate()
                .rotation(540F)
                .alpha(1F)
                .duration = 200


        data.rvIndicatorsOverlays.visibility = View.VISIBLE
        val x = activity?.windowManager?.defaultDisplay?.width!! - data.rvIndicatorsOverlays.width
        data.rvIndicatorsOverlays.animate()
                .alpha(1.0F)
                .translationX(x.toFloat())
                .setDuration(500)
                .setInterpolator(OvershootInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        data.rvIndicatorsOverlays.visibility = View.VISIBLE
                    }
                }
                )
    }

    private fun hideIndicatorsList() {

        mainView.b_drawer.animate()
                .rotation(0F)
                .alpha(1F)
                .duration = 200
        mainView.b_collapse_arrow.animate()
                .rotation(0F)
                .alpha(0F)
                .duration = 200


        data.displayWidth = activity?.windowManager?.defaultDisplay?.width!! * 2
        data.rvIndicatorsOverlays.animate()
                .alpha(0.0F)
                .translationX(data.displayWidth.toFloat())
                .setDuration(500)
                .setInterpolator(OvershootInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        data.rvIndicatorsOverlays.visibility = View.GONE
                    }
                }
                )
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (activity?.currentFocus != null)
            imm.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
    }

    private fun updateIndicatorTitle(runningTA: Boolean) {
        if (runningTA) {
            println("TA Running, making title Loading")
            hideIndicatorsList()
            mainView.b_drawer.setImageResource(R.drawable.menu_yellow)
            mainView.b_drawer.isClickable = false
        } else {
            println("TA not running, making  title Indicators")
            mainView.b_drawer.setImageResource(R.drawable.menu_white)
            mainView.b_drawer.isClickable = true
        }
    }

    private fun updateExchangeDueToCoinUpdate(updatePrefs: Boolean = true) {
        val exchanges: List<DataSource.Exchange> = DataSource.data.coins[data.coinSelected.toLowerCase()].let {
            it?.exchanges?.filter { it.paring.contains(data.currencySelected.toLowerCase()) }
        } ?: return
        if (exchanges.isEmpty())
            return
        var strListExchanges: ArrayList<String> = ArrayList()
        for (i in exchanges.iterator()) {
            strListExchanges.add(i.exchange.capitalize())
        }
        val strSetExchanges: HashSet<String> = HashSet(strListExchanges)
        strListExchanges = ArrayList(strSetExchanges)
        strListExchanges.sort()
        val exchangeAdapter = SimpleArrowDropdownAdapter(activity, R.layout.spinner_dropdown_main_view_with_arrow, strListExchanges)
        exchangeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_main_view_no_arrow)

        activity?.runOnUiThread {
            mainView.spinner_exchange_type.adapter = exchangeAdapter
        }
        //reset first selection on Exchange spinner so we dont update the graph 2 times
        spinnerExchangeFirstRun = true
        //Check if previous exchange is still in the list, if not select the first one
        if (strListExchanges.contains(data.exchangeSelected)) {
            activity?.runOnUiThread {
                mainView.spinner_exchange_type.setSelection(strListExchanges.indexOf(data.exchangeSelected))
            }
        } else {
            activity?.runOnUiThread {
                mainView.spinner_exchange_type.setSelection(0)
            }
            if (updatePrefs) {
                val editor = data.prefs!!.edit()
                editor.putString(EXCHANGE_SELETED, strListExchanges[0])
                editor.apply()
            }
            data.exchangeSelected = strListExchanges[0]
        }
    }

    private fun updateCurrentGraphFromWebData(position: Int, coin: String, exchange: String, currency: String, forceUpdate: Boolean) = runBlocking {
        println("Attempting  to update graph, Pos:$position, Coin:$coin, exchange:$exchange, Currency:$currency, forceUpdate:$forceUpdate")

        GlobalScope.launch{
            executeGraphUpdate(position, coin, exchange, currency, forceUpdate)
        }
    }

    private fun executeGraphUpdate(
            position: Int,
            coin: String,
            exchange: String,
            currency: String,
            forceUpdate: Boolean,
            runTA: Boolean = true
    ) = runBlocking{
        activity?.runOnUiThread {
            mainView.swipe_to_refresh_market_cap.isRefreshing = true
            updateCurrentPrices()
        }
        println("Task running")
        activity?.runOnUiThread {
            mainView.spinner_coin_type.isEnabled = false
            mainView.spinner_exchange_type.isEnabled = false
            mainView.spinner_time_period.isEnabled = false
        }
        if (data.all_ta[position].getCandlestickData(Overlay.Kind.CandleStick).size == 0 || forceUpdate) {
            updateChartStatus(ChartStatusData.Status.LOADING, ChartStatusData.Type.MAIN_CHART, Overlay.Kind.None)
            //                    data.chartList[0] = ChartStatusData(ChartStatusData.Status.LOADING,ChartStatusData.Type.MAIN_CHART)
            for (overlay in OverlayAdapter.data.list) {
                if (overlay.separateChart and overlay.selected) {
                    updateChartStatus(ChartStatusData.Status.LOADING, ChartStatusData.Type.SEPARATE_CHART, overlay.kind)
                }
            }
            activity?.runOnUiThread {
                if (mainView.all_charts_recycler_view.adapter != null)
                    mainView.all_charts_recycler_view.adapter!!.notifyDataSetChanged()

                //Reset legends
                for ((key, chart) in ChartListAdapter.data.charts) {
                    chart as CombinedChart
                    chart.xAxis.limitLines.removeAll(chart.xAxis.limitLines)
                    chart.legend.resetCustom()
                }
            }


            val interval = DataSource.Interval.values()[position]
            println("Getting data from web")
            val ticks = MainActivity.data.dataSource.getData(coin, exchange, currency, interval)
            Log.d("DEBUG", "Finished getting data")
            if (ticks.size < 10) {
                activity?.runOnUiThread {
                    updateChartStatus(ChartStatusData.Status.UPDATE_FAILED, ChartStatusData.Type.MAIN_CHART)
                    for (overlay in OverlayAdapter.data.list) {
                        if (overlay.separateChart and overlay.selected) {
                            updateChartStatus(ChartStatusData.Status.UPDATE_FAILED, ChartStatusData.Type.SEPARATE_CHART, overlay.kind)
                        }
                    }
                    if (mainView.all_charts_recycler_view.adapter != null)
                        mainView.all_charts_recycler_view.adapter!!.notifyDataSetChanged()

                    mainView.b_drawer.setImageResource(R.drawable.menu_red)
                    mainView.b_drawer.isClickable = false

                    mainView.swipe_to_refresh_market_cap.isRefreshing = false
                    mainView.spinner_coin_type.isEnabled = true
                    mainView.spinner_exchange_type.isEnabled = true
                    mainView.spinner_time_period.isEnabled = true
                }


                return@runBlocking
            } else {
                println("Doing TA $position")

                data.all_ta[position].clearAll()
                data.all_ta[position].updateCandlestickData(ticks, addTimeSeriesData = true)


                updateChartStatus(ChartStatusData.Status.UPDATE_CANDLESTICKS, ChartStatusData.Type.MAIN_CHART, Overlay.Kind.None)
                for (overlay in OverlayAdapter.data.list) {
                    if (overlay.separateChart and overlay.selected) {
                        updateChartStatus(ChartStatusData.Status.INITIAL_LOAD, ChartStatusData.Type.SEPARATE_CHART, overlay.kind)
                    }
                }
                if (all_charts_recycler_view.adapter != null)
                    activity?.runOnUiThread {
                        all_charts_recycler_view.adapter!!.notifyDataSetChanged()
                    }
                activity?.runOnUiThread {
                    //Reset legends
                    for ((key, chart) in ChartListAdapter.data.charts) {
                        chart as CombinedChart
                        chart.xAxis.limitLines.removeAll(chart.xAxis.limitLines)
                        chart.legend.resetCustom()
                    }
                }

                //Disabling because it looks redundant
                activity?.runOnUiThread { updateIndicatorTitle(runTA) }
                if (runTA) {
                    data.endTA = false
                    data.runningTA.lock()
                    data.all_ta[position] = TechnicalAnalysis(ticks,
                            TechnicalAnalysis.TAData(data.all_ta[position].getCandlestickData(Overlay.Kind.CandleStick),
                                    Overlay.Kind.CandleStick))
                    data.endTA = false
                    data.runningTA.unlock()

                    println("TA Finished")
                    activity?.runOnUiThread {
                        data.endTA = false
                        updateIndicatorTitle(false)
                    }
                }
            }
        }else {
            activity?.runOnUiThread {
                println("TA Done, time to update recycler")
                updateIndicatorTitle(false)
            }
        }

        GlobalScope.launch {
            data.endTA = false
            data.runningTA.lock()
            data.all_ta[position].updateNonSelectedItems()
            data.endTA = false
            data.runningTA.unlock()
        }

        println("{updatedGraph} Chart list size: " + data.chartList.size)
        updateChartStatus(ChartStatusData.Status.UPDATE_OVERLAYS, ChartStatusData.Type.MAIN_CHART)

        for (overlay in OverlayAdapter.data.list) {
            if (overlay.separateChart and overlay.selected) {
                updateChartStatus(ChartStatusData.Status.TOGGLE_CHART, ChartStatusData.Type.SEPARATE_CHART, overlay.kind)
            }
        }

        activity?.runOnUiThread {
            if (mainView.all_charts_recycler_view.adapter != null)
                mainView.all_charts_recycler_view.adapter!!.notifyDataSetChanged()

            mainView.spinner_coin_type.isEnabled = true
            mainView.spinner_exchange_type.isEnabled = true
            mainView.spinner_time_period.isEnabled = true
            mainView.swipe_to_refresh_market_cap.isRefreshing = false
        }

    }

    private fun isInternetOn(): Boolean {

        // get Connectivity Manager object to check connection
        val connec: ConnectivityManager =
                activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Check for network connections
        if (connec.activeNetworkInfo != null && (
                        connec.activeNetworkInfo.state == android.net.NetworkInfo.State.CONNECTED ||
                                connec.activeNetworkInfo.state == android.net.NetworkInfo.State.CONNECTING)) {
            return true

        } else if (connec.activeNetworkInfo != null &&
                connec.activeNetworkInfo.state == android.net.NetworkInfo.State.DISCONNECTED) {
            return false
        }
        return false
    }

}
