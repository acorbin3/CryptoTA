package com.backflippedstudios.crypto_ta.data.retrofit.market

import com.backflippedstudios.crypto_ta.data.retrofit.Status
import java.io.Serializable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CryptoMarketList : Serializable {

    @SerializedName("status")
    @Expose
    var status: Status? = null
    @SerializedName("data")
    @Expose
    var data: List<QuoteDatum>? = null

}