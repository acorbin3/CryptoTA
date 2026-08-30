package com.backflippedstudios.crypto_ta.frags

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.backflippedstudios.crypto_ta.*
import com.backflippedstudios.crypto_ta.data.retrofit.CryptoList
import com.backflippedstudios.crypto_ta.recyclerviews.MarketCapCardsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MarketCapFrag : Fragment() {
    val title = "Market Cap"
    private lateinit var adapter: MarketCapCardsAdapter
    private var marketData: CryptoList? = null
    private var mainView: View? = null

    // Tapping a card jumps to that coin's chart on the Detail Analysis tab
    private val onCoinClick: (String) -> Unit = { symbol ->
        (activity as? MainActivity)?.let { act ->
            act.viewpager.currentItem = 0
            act.tab1Frag.selectCoinPair(symbol, "USD")
        }
    }

    // Long-pressing a card sets a one-shot price alert
    private val onCoinLongPress: (com.backflippedstudios.crypto_ta.data.retrofit.Datum) -> Unit = { datum ->
        showPriceAlertDialog(datum)
    }

    private fun showPriceAlertDialog(datum: com.backflippedstudios.crypto_ta.data.retrofit.Datum) {
        val ctx = activity ?: return
        val symbol = datum.symbol ?: return
        val geckoId = datum.slug ?: return

        if (android.os.Build.VERSION.SDK_INT >= 33 &&
                androidx.core.content.ContextCompat.checkSelfPermission(ctx,
                        android.Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 77)
        }

        val input = android.widget.EditText(ctx)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.setText(datum.quote?.usd?.price?.toString() ?: "")

        android.app.AlertDialog.Builder(ctx, android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle("Price alert for $symbol")
                .setMessage("Notify me when the USD price goes...")
                .setView(input)
                .setPositiveButton("Above") { _, _ -> saveAlert(symbol, geckoId, true, input.text.toString()) }
                .setNegativeButton("Below") { _, _ -> saveAlert(symbol, geckoId, false, input.text.toString()) }
                .setNeutralButton("Cancel", null)
                .show()
    }

    private fun saveAlert(symbol: String, geckoId: String, above: Boolean, thresholdText: String) {
        val ctx = activity?.applicationContext ?: return
        val threshold = thresholdText.toDoubleOrNull()
        if (threshold == null || threshold <= 0) {
            android.widget.Toast.makeText(ctx, "Invalid price", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        com.backflippedstudios.crypto_ta.data.PriceAlertStore.add(ctx,
                com.backflippedstudios.crypto_ta.data.PriceAlert(symbol, geckoId, above, threshold))
        val direction = if (above) "above" else "below"
        android.widget.Toast.makeText(ctx,
                "Alert set: $symbol $direction $$threshold", android.widget.Toast.LENGTH_LONG).show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainView = inflater.inflate(R.layout.market_overview_main_layout, container, false)

        // Edge-to-edge: let the last card scroll clear of the gesture-nav bar
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(mainView!!) { _, windowInsets ->
            val bars = windowInsets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            mainView?.rv_market_overview?.apply {
                clipToPadding = false
                setPadding(0, 0, 0, bars.bottom)
            }
            windowInsets
        }

        marketData = com.backflippedstudios.crypto_ta.data.DataSource.data.marketCapList

        adapter = MarketCapCardsAdapter(activity?.applicationContext!!, marketData?.data, onCoinClick, onCoinLongPress)
        mainView?.rv_market_overview?.layoutManager = LinearLayoutManager(activity?.applicationContext, LinearLayoutManager.VERTICAL, false)
        mainView?.rv_market_overview?.adapter = adapter
        mainView?.swipe_to_refresh_market_cap?.setOnRefreshListener {
            GlobalScope.launch(Dispatchers.IO) {
                val refreshed = MainActivity.data.dataSource.getMarketCapV2()
                activity?.runOnUiThread {
                    marketData = refreshed
                    adapter = MarketCapCardsAdapter(activity?.applicationContext!!, marketData?.data, onCoinClick, onCoinLongPress)
                    mainView?.rv_market_overview?.adapter = adapter
                    mainView?.swipe_to_refresh_market_cap?.isRefreshing = false
                }
            }
        }

        return mainView
    }

    // Sparkline chart data is loaded together with the market list in
    // DataSource.initCoinsGecko; this just refreshes the cards once it's there.
    fun processGraphs() {
        marketData = com.backflippedstudios.crypto_ta.data.DataSource.data.marketCapList
        activity?.runOnUiThread {
            adapter = MarketCapCardsAdapter(activity?.applicationContext!!, marketData?.data, onCoinClick, onCoinLongPress)
            mainView?.rv_market_overview?.adapter = adapter
        }
    }
}
