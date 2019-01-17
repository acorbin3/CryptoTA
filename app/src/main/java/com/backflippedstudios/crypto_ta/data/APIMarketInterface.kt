package com.backflippedstudios.crypto_ta.data

import com.backflippedstudios.crypto_ta.data.retrofit.CryptoInfoList
import com.backflippedstudios.crypto_ta.data.retrofit.CryptoList
import com.backflippedstudios.crypto_ta.data.retrofit.market.CryptoMarketList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

internal interface APIMarketInterface {

    //https://pro.coinmarketcap.com/api/v1#section/Quick-Start-Guide
    @Headers("X-CMC_PRO_API_KEY: c2690900-7d25-4e55-88aa-ef7fdaa7030b" )
    @GET("/v1/cryptocurrency/quotes/historical")
    fun getMarketInfo(@Query("symbol") page: String): Call<CryptoMarketList>

}