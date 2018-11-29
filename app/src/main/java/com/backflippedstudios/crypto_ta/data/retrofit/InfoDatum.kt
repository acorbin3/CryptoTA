package com.backflippedstudios.crypto_ta.data.retrofit

import java.io.Serializable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class InfoDatum : Serializable {

    @SerializedName("id")
    @Expose
    var id: Int? = null
    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("symbol")
    @Expose
    var symbol: String? = null
    @SerializedName("slug")
    @Expose
    var slug: String? = null
    @SerializedName("logo")
    @Expose
    var logo: String? = null
    @SerializedName("tags")
    @Expose
    var tags: List<String>? = null
    @SerializedName("urls")
    @Expose
    var urls: List<URLs>? = null

}