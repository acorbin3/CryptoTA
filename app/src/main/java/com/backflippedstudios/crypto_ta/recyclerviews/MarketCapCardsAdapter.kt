package com.backflippedstudios.crypto_ta.recyclerviews

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.backflippedstudios.crypto_ta.R
import com.backflippedstudios.crypto_ta.customchartmods.ChartStyle
import com.backflippedstudios.crypto_ta.data.DataSource
import com.backflippedstudios.crypto_ta.data.retrofit.Datum
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.ArrayList

class MarketCapCardsAdapter(var context: Context, val mCardList: List<Datum>?): RecyclerView.Adapter<MarketCapCardsAdapter.CardsViewHolder>(){

    object data{
        var coinLineData: HashMap<String, ArrayList<ILineDataSet>> = HashMap()
    }
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CardsViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.market_cap_item,p0, false)
        return CardsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mCardList?.size ?: 0
    }

    override fun onBindViewHolder(p0: CardsViewHolder, p1: Int) {
        val firstItem = mCardList?.get(p1)
        p0.tv_coinName.text = firstItem?.name
        val numFormat = NumberFormat.getNumberInstance()
        p0.tv_marketcap.text = "$" + numFormat.format(firstItem?.quote?.usd?.marketCap).toString()

        val defaulFormat = NumberFormat.getCurrencyInstance()
        if(firstItem?.quote?.usd?.price!! > 1){
            defaulFormat.maximumFractionDigits = 2
        }else{
            defaulFormat.maximumFractionDigits = 5
        }
        p0.tv_price.text = defaulFormat.format(firstItem?.quote?.usd?.price!!).toString()

        p0.tv_supply.text = numFormat.format(firstItem?.totalSupply).toString() + " " + firstItem?.symbol
        p0.tv_volume.text = "$" + numFormat.format(firstItem?.quote?.usd?.volume24h).toString()+"(24hV)"

        Picasso.get()
                .load("https://chasing-coins.com/api/v1/std/logo/"+firstItem?.symbol)
                .placeholder(android.R.drawable.ic_menu_help)
                .error(android.R.drawable.ic_menu_help)
                .resize(100,100)
                .into(p0.iv_coin_icon)

        //Update chart with data
        p0.chart_24.clear()
        p0.chart_7d.clear()
        var symbol = firstItem?.symbol?.toLowerCase()
        if(symbol == "miota"){
            symbol = "iot"
        }
        val coin24h = symbol + "_24h"
        val coin7d = symbol +"_7d"
        var percentChange24h = 0.0
        var percentChange7d = 0.0
        DataSource.data.lockCoinData.lock()
        if(coin24h in DataSource.data.coinData) {
            if(coin24h in data.coinLineData){
                ChartStyle(context).updateMarketCapGraph(data.coinLineData[coin24h]!!,p0.chart_24)
            }
            else if (DataSource.data.coinData[coin24h]?.size!! > 0) {
                ChartStyle(context).updateMarketCapGraph(DataSource.data.coinData[coin24h]!!,p0.chart_24)
            }
            // Update 24h % Change
            percentChange24h = DataSource.data.coinData[coin24h]?.first()?.y?.let {
                DataSource.data.coinData[coin24h]?.last()?.y?.minus(it) }?.toFloat()?.toDouble()!!/ DataSource.data.coinData[coin24h]?.first()?.y!!
        }
        else{
            p0.chart_24.setNoDataText("Loading data")
        }

        if(coin7d in DataSource.data.coinData) {
            if(coin7d in data.coinLineData){
                ChartStyle(context).updateMarketCapGraph(data.coinLineData[coin7d]!!,p0.chart_7d)
            }
            else if (DataSource.data.coinData[coin7d]?.size!! > 0) {
                data.coinLineData[coin7d] = ChartStyle(context).updateMarketCapGraph(DataSource.data.coinData[coin7d]!!,p0.chart_7d)
            }
            //Update 7d % Change
            percentChange7d = DataSource.data.coinData[coin7d]?.first()?.y?.let {
                DataSource.data.coinData[coin7d]?.last()?.y?.minus(it) }?.toFloat()?.toDouble()!!/ DataSource.data.coinData[coin7d]?.first()?.y!!
        }
        else{
            p0.chart_7d.setNoDataText("Loading data")
        }
        DataSource.data.lockCoinData.unlock()
        p0.chart_24.invalidate()
        p0.chart_7d.invalidate()

        val percentFormat = NumberFormat.getPercentInstance()
        percentFormat.minimumFractionDigits = 5
        if(percentChange24h == 0.0){
            percentChange24h = firstItem?.quote?.usd?.percentChange24h!!
            p0.tv_percentChange.text = firstItem?.quote?.usd?.percentChange24h.toString()+"%(24h)"
        }else{
            p0.tv_percentChange.text = percentFormat.format(percentChange24h).toString()+"(24h)"
        }
        if(percentChange7d == 0.0){
            percentChange7d = firstItem?.quote?.usd?.percentChange7d!!
            p0.tv_percentChange_7d.text = "${firstItem?.quote?.usd?.percentChange7d}%(7d)"
        }else{
            p0.tv_percentChange_7d.text = percentFormat.format(percentChange7d).toString()+"(7d)"
        }




        if(percentChange24h!! > 0){
            p0.tv_percentChange.setTextColor(ContextCompat.getColor(context, R.color.md_green_500))
        }else{
            p0.tv_percentChange.setTextColor(ContextCompat.getColor(context, R.color.md_red_500))
        }
        if(percentChange7d!! > 0){
            p0.tv_percentChange_7d.setTextColor(ContextCompat.getColor(context, R.color.md_green_500))
        }else{
            p0.tv_percentChange_7d.setTextColor(ContextCompat.getColor(context, R.color.md_red_500))
        }
    }

    inner class CardsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        internal var tv_price: TextView = itemView.findViewById(R.id.tv_last_price)
        internal var tv_percentChange: TextView = itemView.findViewById(R.id.tv_percent_change_24h)
        internal var tv_percentChange_7d: TextView = itemView.findViewById(R.id.tv_percent_change_7d)
        internal var tv_supply: TextView = itemView.findViewById(R.id.tv_supply)
        internal var tv_coinName: TextView = itemView.findViewById(R.id.tv_coin_name)
        internal var tv_marketcap: TextView = itemView.findViewById(R.id.tv_marketcap)
        internal var tv_volume: TextView = itemView.findViewById(R.id.tv_volume)
        internal var iv_coin_icon: ImageView = itemView.findViewById(R.id.iv_coin_icon)
        internal var chart_24: CombinedChart = itemView.findViewById(R.id.market_cap_combined_chart_24h)
        internal var chart_7d: CombinedChart = itemView.findViewById(R.id.market_cap_combined_chart_7d)
    }

}