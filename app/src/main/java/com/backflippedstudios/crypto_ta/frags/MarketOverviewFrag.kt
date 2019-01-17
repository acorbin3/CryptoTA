package com.backflippedstudios.crypto_ta.frags

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.backflippedstudios.crypto_ta.MainActivity
import com.backflippedstudios.crypto_ta.R
import com.backflippedstudios.crypto_ta.data.DataSource
import com.backflippedstudios.crypto_ta.recyclerviews.MarketSummaryCardsAdapter
import kotlinx.android.synthetic.main.market_overview_main_layout.view.*

class MarketOverviewFrag :Fragment(){
    val title = "Market Overview"
    private lateinit var adapter: MarketSummaryCardsAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var mainView: View = inflater.inflate(R.layout.market_overview_main_layout, container, false)

        // create list of Market data items.
        val marketData = MainActivity.data.dataSource.getMarketSummary()

        // create adapter
        adapter = MarketSummaryCardsAdapter(activity?.applicationContext!!, marketData)
        //set adapter to RecyclerView
        mainView.rv_market_overview.layoutManager = GridLayoutManager(activity?.applicationContext,2,1, false)
        mainView.rv_market_overview.adapter = adapter
        mainView.swipe_to_refresh_market_cap.setOnRefreshListener {
            val marketData = MainActivity.data.dataSource.getMarketSummary()
            adapter = MarketSummaryCardsAdapter(activity?.applicationContext!!, marketData)
            mainView.rv_market_overview.adapter = adapter
            mainView.rv_market_overview.adapter!!.notifyDataSetChanged()
            mainView.swipe_to_refresh_market_cap.isRefreshing = false
        }
        return mainView
    }
}