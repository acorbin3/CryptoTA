package com.backflippedstudios.crypto_ta.data.retrofit

import java.io.Serializable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Quote : Serializable {

    @SerializedName("USD")
    @Expose
    var usd: USD? = null

    companion object {
        private const val serialVersionUID = -5780538494495942860L
    }

}