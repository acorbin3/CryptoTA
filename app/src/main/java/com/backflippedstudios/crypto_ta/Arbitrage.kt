package com.backflippedstudios.crypto_ta

import org.ta4j.core.Decimal
import org.ta4j.core.Tick

/**
 * Created by C0rbin on 12/9/2017.
 */
class Arbitrage {

    //TODO: get 2 exchange data for close prices
    //TODO: get percent change between the 2 exchanges

    object data{
        var exchange1Data: ArrayList<Tick> = ArrayList<Tick>()
        var exchange2Data: ArrayList<Tick> = ArrayList<Tick>()
        var diffData: ArrayList<Double> = ArrayList()
    }


    fun getData(){
        data.exchange1Data = DataSource().getData("ETH","bitfinex","usd",DataSource.Interval._1MIN)
        data.exchange2Data = DataSource().getData("ETH","gdax","usd",DataSource.Interval._1MIN)


        for(i in 0 until data.exchange1Data.size){
            data.diffData.add(Math.abs(data.exchange1Data[i].closePrice.minus(data.exchange1Data[i].closePrice ).toDouble()))
        }
    }
}