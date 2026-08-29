package com.backflippedstudios.crypto_ta

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.backflippedstudios.crypto_ta.data.DataSource
import com.backflippedstudios.crypto_ta.frags.DetailedAnalysisFrag
import com.backflippedstudios.crypto_ta.frags.MarketCapFrag
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var tab1Frag: DetailedAnalysisFrag
    lateinit var tab2Frag: MarketCapFrag

    object data {
        val dataSource = DataSource()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("Loading Main activity")
        setContentView(R.layout.activity_swipable_tabs)
        AndroidThreeTen.init(this)
        Analytics.init(applicationContext)
        DataSource.appContext = applicationContext

        // Background check for saved price alerts (~15 min, the WorkManager minimum)
        androidx.work.WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "price_alerts",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                androidx.work.PeriodicWorkRequestBuilder<com.backflippedstudios.crypto_ta.data.PriceAlertWorker>(
                        15, java.util.concurrent.TimeUnit.MINUTES).build())
        // Also check immediately on launch so alerts never feel stale
        if (com.backflippedstudios.crypto_ta.data.PriceAlertStore.load(applicationContext).isNotEmpty()) {
            androidx.work.WorkManager.getInstance(applicationContext).enqueue(
                    androidx.work.OneTimeWorkRequestBuilder<com.backflippedstudios.crypto_ta.data.PriceAlertWorker>()
                            .build())
        }

        //Init viewPager
        val adapter = ViewPagerAdapter(supportFragmentManager)

        tab1Frag = DetailedAnalysisFrag()
        tab2Frag = MarketCapFrag()

        adapter.addFragment(tab1Frag, tab1Frag.title)
        adapter.addFragment(tab2Frag, tab2Frag.title)
        //To ensure that the first page stays in memory
        val viewPager: CustViewPager = this.viewpager
        viewPager.offscreenPageLimit = 5
        viewPager.adapter = adapter

        tablayout.setupWithViewPager(viewPager)
        if (DataSource.data.coins.isEmpty()) {
            loadCoins()
        }
    }

    private fun loadCoins() {
        GlobalScope.launch(Dispatchers.IO) {
            // Show the last-known data instantly while fresh data loads
            val cached = data.dataSource.loadCoinsFromCache()
            if (cached) {
                println("Loaded coin list from disk cache")
                runOnUiThread { refreshTabs() }
            }

            // Retry with backoff so a rate-limited cold start recovers on its own
            var loaded = false
            for (attempt in 1..10) {
                loaded = data.dataSource.initCoinsGecko()
                if (loaded) break
                println("Failed to load coin list from CoinGecko (attempt $attempt), retrying...")
                kotlinx.coroutines.delay(15_000L * attempt.coerceAtMost(4))
            }
            if (!loaded) {
                println("Giving up loading the coin list from CoinGecko")
                return@launch
            }
            // Skip the second full UI rebuild if the cache already drove one —
            // the fresh data landed in the same shared structures
            if (!cached) {
                runOnUiThread { refreshTabs() }
            } else {
                runOnUiThread { tab2Frag.processGraphs() }
            }
            println("Finished initial loading")
        }
    }

    private fun refreshTabs() {
        if (tab1Frag.mainView != null) {
            tab1Frag.processInit(applicationContext, true)
        }
        tab2Frag.processGraphs()
    }
}
