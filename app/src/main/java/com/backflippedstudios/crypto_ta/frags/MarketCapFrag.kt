package com.backflippedstudios.crypto_ta.frags

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.backflippedstudios.crypto_ta.MainActivity
import com.backflippedstudios.crypto_ta.R
import com.backflippedstudios.crypto_ta.data.DataSource
import com.backflippedstudios.crypto_ta.data.retrofit.CryptoList
import com.backflippedstudios.crypto_ta.recyclerviews.MarketCapCardsAdapter
import kotlinx.android.synthetic.main.market_overview_main_layout.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Response

class MarketCapFrag :Fragment(){
    val title = "Market Cap"
    private lateinit var adapter : MarketCapCardsAdapter
    private var marketData: Response<CryptoList>? = null
    private var mainView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainView = inflater.inflate(R.layout.market_overview_main_layout, container, false)

        // create list of Market data items.
        if(marketData == null) {
            try {
                marketData = MainActivity.data.dataSource.getMarketCapV2()
            }catch(e:Exception){

            }
        }
        //TODO - support when response fails
        // create adapter
        adapter = MarketCapCardsAdapter(activity?.applicationContext!!,marketData?.body()?.data)
        //set adapter to RecyclerView
        mainView?.rv_market_overview?.layoutManager = LinearLayoutManager(activity?.applicationContext,LinearLayoutManager.VERTICAL, false)
        mainView?.rv_market_overview?.adapter = adapter
        mainView?.swipe_to_refresh_market_cap?.setOnRefreshListener {
            adapter = MarketCapCardsAdapter(activity?.applicationContext!!, MainActivity.data.dataSource.getMarketCapV2()?.body()?.data)

            mainView?.rv_market_overview?.adapter = adapter
            mainView?.rv_market_overview?.adapter!!.notifyDataSetChanged()
            mainView?.swipe_to_refresh_market_cap?.isRefreshing = false
        }
        if(DataSource.data.coinData.isEmpty()) {
            processGraphs()
        }

        return mainView
    }

    fun processGraphs() = runBlocking {
        GlobalScope.launch {
            println("DataSource.data.coins size: ${DataSource.data.coins.size}")
            // Create thread to run this on
            // Find an exchange for coin
            if(DataSource.data.coins.size == 0)
                return@launch


            marketData?.body()?.data?.forEachIndexed { index, item ->
                var symbol = item.symbol
                if(symbol == "MIOTA"){
                    symbol = "IOT"
                }
                println("looking at $symbol")
                if (symbol in DataSource.data.coins) {
                    println(" " + DataSource.data.coins[symbol]?.url)
                } else if (symbol?.toLowerCase() in DataSource.data.coins) {
                    //Find an exchange with USD/USDT
                    var foundExchangeWithUSD = false

                    val exchangeList = arrayOf(
                            "coinbase-pro",
                            "binance",
                            "kraken",
                            "bitfinex",
                            "bittrex",
                            "poloniex",
                            "hitbtc",
                            "cexio",
                            "bitbay"
                    )
                    for(exchange in exchangeList) {
                        val url = getExchangeURL(symbol!!, exchange)
                        if (url.isNotEmpty()) {
                            println("$url/ohlc")
                            foundExchangeWithUSD = true
                            //Get data from DataSource
                            //notify that data change for specific item
                            MainActivity.data.dataSource.processOHLC(symbol?.toLowerCase().toString(), url)
                            activity?.runOnUiThread {
                                mainView?.rv_market_overview?.adapter!!.notifyItemChanged(index)
                            }
                            break
                        }
                    }


                    if(!foundExchangeWithUSD){
                        println(" Didnt find. using BTC")
                        for(exchange in DataSource.data.coins[item.symbol?.toLowerCase()]?.exchanges!!){
                            if(exchange.paring.contains("btc")){
                                println(" " +exchange.url+"/ohlc")
                                //Get data from DataSource
                                //notifiy that data change for specific item
                                GlobalScope.launch {
                                    MainActivity.data.dataSource.processOHLC(item.symbol?.toLowerCase().toString(), exchange.url)
                                    activity?.runOnUiThread {
                                        mainView?.rv_market_overview?.adapter!!.notifyItemChanged(index)
                                    }
                                }
                                break
                            }
                        }
                    }
//                    println(" Lower " + DataSource.data.coins[item.symbol?.toLowerCase()]?.exchanges?.get(0)?.url)
                }
            }
        }
    }

    private fun getExchangeURL(symbol: String, exchange: String): String {

        var goodUSDExchanges =
                DataSource.data.coins[symbol?.toLowerCase()]?.exchanges?.filter {
                    it.paring.contains("usd")
                            && it.paring.contains(symbol?.toLowerCase().toString())
                            && (it.exchange.contains(exchange))
                }

        var url = ""
        if (goodUSDExchanges?.isNotEmpty()!!) {
            url = goodUSDExchanges[0].url
        }
        return url
    }
}