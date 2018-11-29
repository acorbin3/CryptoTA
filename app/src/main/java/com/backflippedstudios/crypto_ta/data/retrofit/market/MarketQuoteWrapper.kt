package com.backflippedstudios.crypto_ta.data.retrofit.market

import java.io.Serializable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MarketQuoteWrapper : Serializable {

    @SerializedName("timestamp")
    @Expose
    var timestamp: String? = null
    @SerializedName("quote")
    @Expose
    var quote: MarketQuote? = null
}