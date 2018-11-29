package com.backflippedstudios.crypto_ta.recyclerviews

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.backflippedstudios.crypto_ta.R
import com.backflippedstudios.crypto_ta.data.DataSource
import java.text.NumberFormat

class MarketSummaryCardsAdapter(val context: Context, private val mCardList: List<DataSource.MarketData>): RecyclerView.Adapter<MarketSummaryCardsAdapter.CardsViewHolder>(){
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CardsViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.market_item,p0, false)
        return CardsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mCardList.size
    }

    override fun onBindViewHolder(p0: CardsViewHolder, p1: Int) {
        val firstItem = mCardList[p1]
        p0.tv_coinPair.text = firstItem.coinPair
        p0.tv_exchange.text = firstItem.exchange
        val defaulFormat = NumberFormat.getPercentInstance()
        defaulFormat.minimumFractionDigits = 2
        p0.tv_percentChange.text = defaulFormat.format(firstItem.percentChange)
        if(firstItem.percentChange > 0){
            p0.tv_percentChange.setTextColor(ContextCompat.getColor(context, R.color.md_green_500))
        }else{
            p0.tv_percentChange.setTextColor(ContextCompat.getColor(context, R.color.md_red_500))
        }
        val numFormat = NumberFormat.getNumberInstance()
        if(firstItem.lastPrice > 1){
            numFormat.maximumFractionDigits = 2
        }else{
            numFormat.maximumFractionDigits = 6
        }
        p0.tv_price.text = numFormat.format(firstItem.lastPrice).toString()

    }

    inner class CardsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        internal var tv_price: TextView = itemView.findViewById(R.id.tv_last_price)
        internal var tv_percentChange: TextView = itemView.findViewById(R.id.tv_percent_change)
        internal var tv_exchange: TextView = itemView.findViewById(R.id.tv_exchange)
        internal var tv_coinPair: TextView = itemView.findViewById(R.id.tv_coinpair)
    }

}