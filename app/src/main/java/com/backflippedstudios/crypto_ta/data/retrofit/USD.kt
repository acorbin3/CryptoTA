package com.backflippedstudios.crypto_ta.data.retrofit

import java.io.Serializable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class USD : Serializable {

    @SerializedName("price")
    @Expose
    var price: Double? = null
    @SerializedName("volume_24h")
    @Expose
    var volume24h: Double? = null
    @SerializedName("percent_change_1h")
    @Expose
    var percentChange1h: Double? = null
    @SerializedName("percent_change_24h")
    @Expose
    var percentChange24h: Double? = null
    @SerializedName("percent_change_7d")
    @Expose
    var percentChange7d: Double? = null
    @SerializedName("market_cap")
    @Expose
    var marketCap: Double? = null
    @SerializedName("last_updated")
    @Expose
    var lastUpdated: String? = null

    companion object {
        private const val serialVersionUID = -4300650181662130498L
    }

}