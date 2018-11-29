package com.backflippedstudios.crypto_ta.data.retrofit.market

import java.io.Serializable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MarketQuote : Serializable {

    @SerializedName("USD")
    @Expose
    var usd: MarketUSD? = null

}