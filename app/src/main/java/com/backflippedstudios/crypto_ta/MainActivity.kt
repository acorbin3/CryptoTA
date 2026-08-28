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
            runOnUiThread {
                if (tab1Frag.mainView != null) {
                    tab1Frag.processInit(applicationContext, true)
                }
                tab2Frag.processGraphs()
                println("Finished initial loading")
            }
        }
    }
}
