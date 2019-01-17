package com.backflippedstudios.crypto_ta.data.retrofit.market

import java.io.Serializable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MarketUSD : Serializable {

    @SerializedName("price")
    @Expose
    var price: Double? = null
    @SerializedName("volume_24h")
    @Expose
    var volume24h: Double? = null
    @SerializedName("market_cap")
    @Expose
    var marketCap: Double? = null
    @SerializedName("last_updated")
    @Expose
    var lastUpdated: String? = null

}