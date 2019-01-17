package com.backflippedstudios.crypto_ta.data.retrofit

import java.io.Serializable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CryptoInfoList : Serializable {

    @SerializedName("status")
    @Expose
    var status: Status? = null
    @SerializedName("data")
    @Expose
    var data: List<InfoDatum>? = null

}