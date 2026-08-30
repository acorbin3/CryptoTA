package com.backflippedstudios.crypto_ta.data.retrofit

import java.io.Serializable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CryptoList : Serializable {

    @SerializedName("status")
    @Expose
    var status: Status? = null
    @SerializedName("data")
    @Expose
    var data: List<Datum>? = null

    companion object {
        private const val serialVersionUID = -4369048252305703014L
    }

}