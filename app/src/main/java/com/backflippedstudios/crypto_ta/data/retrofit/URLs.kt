package com.backflippedstudios.crypto_ta.data.retrofit

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class URLs : Serializable{
    @SerializedName("website")
    @Expose
    var website: List<String>? = null
    @SerializedName("explorer")
    @Expose
    var explorer: List<String>? = null
    @SerializedName("source_code")
    @Expose
    var source_code: List<String>? = null
    @SerializedName("message_board")
    @Expose
    var message_board: List<String>? = null
    @SerializedName("chat")
    @Expose
    var chat: List<String>? = null
    @SerializedName("announcement")
    @Expose
    var announcement: List<String>? = null
    @SerializedName("reddit")
    @Expose
    var reddit: List<String>? = null
    @SerializedName("twitter")
    @Expose
    var twitter: List<String>? = null

}