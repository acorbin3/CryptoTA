package com.backflippedstudios.crypto_ta.frags

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.backflippedstudios.crypto_ta.*
import com.backflippedstudios.crypto_ta.recyclerviews.MarketSummaryCardsAdapter

class MarketOverviewFrag : Fragment() {
    val title = "Market Overview"
    private lateinit var adapter: MarketSummaryCardsAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mainView: View = inflater.inflate(R.layout.market_overview_main_layout, container, false)

        // create list of Market data items.
        val marketData = MainActivity.data.dataSource.getMarketSummary()

        // create adapter
        adapter = MarketSummaryCardsAdapter(activity?.applicationContext!!, marketData)
        //set adapter to RecyclerView
        mainView.rv_market_overview.layoutManager = GridLayoutManager(activity?.applicationContext, 2, 1, false)
        mainView.rv_market_overview.adapter = adapter
        mainView.swipe_to_refresh_market_cap.setOnRefreshListener {
            val refreshed = MainActivity.data.dataSource.getMarketSummary()
            adapter = MarketSummaryCardsAdapter(activity?.applicationContext!!, refreshed)
            mainView.rv_market_overview.adapter = adapter
            mainView.rv_market_overview.adapter!!.notifyDataSetChanged()
            mainView.swipe_to_refresh_market_cap.isRefreshing = false
        }
        return mainView
    }
}
