package com.backflippedstudios.crypto_ta.data.retrofit.market

import java.io.Serializable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class QuoteDatum : Serializable {

    @SerializedName("id")
    @Expose
    var id: Int? = null
    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("symbol")
    @Expose
    var symbol: String? = null
    @SerializedName("quotes")
    @Expose
    var quotes: List<MarketQuoteWrapper>? = null

}