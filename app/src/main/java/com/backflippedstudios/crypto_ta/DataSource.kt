package com.backflippedstudios.crypto_ta

import android.util.JsonReader
import android.util.JsonToken
import org.ta4j.core.BaseTick
import org.ta4j.core.Decimal
import org.ta4j.core.Tick
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import java.util.*
import javax.net.ssl.HttpsURLConnection
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by C0rbin on 11/15/2017.
 */
class DataSource {

    enum class Interval(val seconds: Long){
        _1MIN(60),
        _3MIN(60*3),
        _5MIN(60*5),
        _15MIN(60*15),
        _30MIN(60*30),
        _1HOUR(60*60),
        _2HOUR(60*60*2),
        _4HOUR(60*60*4),
        _6HOUR(60*60*6),
        _12HOUR(60*60*12),
        _1DAY(60*60*24),
        _3DAY(60*60*24*3),
        _1WEEK(60*60*24*7)
    }
    data class Exchange(val exchange: String, val paring: String, val active: Boolean, val url: String)
    data class Asset(val id: Int, val symboal: String, val name: String, val FiatLegalTender: Boolean, val url: String){
        var exchanges: ArrayList<Exchange> = ArrayList()
    }
    object data{
        var coins: HashMap<String,Asset> = HashMap()
    }

    fun getData(coin: String, exchange: String, currency: String, interval: Interval) : ArrayList<Tick> {
        val ticks = ArrayList<Tick>()
        try {

            var granularity = interval.seconds
            println("About to connect to gdax api")
            var startCal = Calendar.getInstance();
            startCal.set(2016, 10, 10)
            var endCal = Calendar.getInstance();
            endCal.set(2016, 9, 10)

            //println("Start: " + start.fromCalendar(startCal))
            //println("End: " + end.fromCalendar(endCal))
            //val url = URL("https://api.gdax.com/products/ETH-USD/candles?granularity="+granularity)
            var exchangeData = data.coins[coin.toLowerCase()]?.exchanges?.filter({
                it.paring.contains(currency.toLowerCase())&&
                it.exchange.toLowerCase() == exchange.toLowerCase()
            })

            if(exchangeData?.size == 0){
                return ticks
            }

            println("Connecting to " + exchangeData?.get(0)?.url + "/ohlc?periods="+granularity)
            var url = URL(exchangeData?.get(0)?.url + "/ohlc?periods="+granularity)
            val connection = url.openConnection() as HttpsURLConnection
            connection.requestMethod = "GET"
            //connection.setRequestProperty("Accept", "application/json");
            if (connection.responseCode == 200) {
                // Success
                // Further processing here
                println("Response: " + connection.responseCode)
            } else {
                // Error handling code goes here
            }


            val responseBody = connection.inputStream
            val responseBodyReader = InputStreamReader(responseBody, "UTF-8")
            //println(responseBodyReader)
            val jsonReader = JsonReader(responseBodyReader)
            jsonReader.beginObject()//Start results object
            jsonReader.nextName()
            jsonReader.beginObject()//Start period object
            jsonReader.nextName()
            val peak = jsonReader.peek()
            if(peak == JsonToken.NULL){
                println("Bad coin")
                return ticks
            }
            jsonReader.beginArray() // Start processing the JSON object
            var lastTick: Tick? = null
            while (jsonReader.hasNext()) { // Loop through all keys
                jsonReader.beginArray()
                while (jsonReader.hasNext()) { // Loop through all keys
                    val time = jsonReader.nextInt()

//                    val dtf = DateTimeFormat.forPattern("yyyy-MMMM-dd hh:mm:ssa")
//                    val date = Date(time * 1000L)
//                    val dateTime = DateTime(date)
                    //println(date.toString())
                    //println(dtf.print(dateTime))


                    var open = jsonReader.nextDouble() // Fetch the next key
                    val high = jsonReader.nextDouble() // Fetch the next key
                    val low = jsonReader.nextDouble() // Fetch the next key
                    val close = jsonReader.nextDouble() // Fetch the next key
                    val volume = jsonReader.nextDouble() // Fetch the next key
                    val peak = jsonReader.peek()
                    if(peak == JsonToken.NUMBER){
                        val unknownFloat = jsonReader.nextDouble()
                    }

                    var i = Instant.ofEpochSecond(time.toLong())
                    val z = i.atZone(ZoneId.of("America/New_York"))
                    //clean open and close betwwen last 2 ticks so we don't have gaps
                    if(lastTick != null && !lastTick.closePrice.isEqual(Decimal.valueOf(open))) {
                        open = lastTick.closePrice.toDouble()
                    }
                    var currentTick = BaseTick(z,
                            Decimal.valueOf(open),
                            Decimal.valueOf(high),
                            Decimal.valueOf(low),
                            Decimal.valueOf(close),
                            Decimal.valueOf(volume))
                    //Filter out bad data if we get a zero value
                    if(currentTick.closePrice.isEqual(Decimal.valueOf(0)) or currentTick.minPrice.isEqual(Decimal.valueOf(0))){
                        if(lastTick != null)
                            ticks.add(lastTick)
                    }
                    else{

                        ticks.add(currentTick)
                        lastTick = currentTick
                    }
//                    println(" Time:" + time + " Low:" + low +
//                            " High:" + high + " Open:" + open + " Close:" + close + " Volume:" + volume)
                }
                jsonReader.endArray()
            }
            jsonReader.endArray()
            jsonReader.close()
            connection.disconnect()
            println("Finished parsing and found: " + ticks.size)
        }
        catch (e : MalformedURLException) {
            e.printStackTrace();
        } catch (e: IOException) {
            e.printStackTrace();
        }
        catch (e: NumberFormatException){
            e.printStackTrace();
            ticks.clear()
            return ticks
        }

        return ticks
    }

    fun getCurrentValue(coin: String, exchange: String, currency: String) : Float{
        var endPrice: Float = 0.0F

        var exchangeData = data.coins[coin.toLowerCase()]?.exchanges?.filter({
            it.paring.contains(currency.toLowerCase())&&
                    it.exchange.toLowerCase() == exchange.toLowerCase()
        })
        if(exchangeData?.size ?: 0 == 0){
            return 0.0F
        }
        val urlStr = "${exchangeData?.get(0)?.url}/price"

        var url = URL(urlStr)
//        println("Getting current price: $urlStr. Coin $coin currency $currency exchange $exchange")
        val connection = url.openConnection() as HttpsURLConnection
        connection.requestMethod = "GET"
        if (connection.responseCode == 200) {
            // Success
            // Further processing here
//            println("Response: " + connection.responseCode)
            val responseBody = connection.inputStream
            val responseBodyReader = InputStreamReader(responseBody, "UTF-8")
            //println(responseBodyReader)
            val jsonReader = JsonReader(responseBodyReader)
            //Result:
            //{"result":{"price":0.0162},"allowance":{"cost":1872372,"remaining":7942076584}}
            jsonReader.beginObject()//Start results object
            jsonReader.nextName()
            jsonReader.beginObject()//Start price object
            jsonReader.nextName()
            endPrice = jsonReader.nextDouble().toFloat()

//            println("Found price: $endPrice")
            jsonReader.close()
            connection.disconnect()
        } else {
            // Error handling code goes here
        }




        return endPrice
    }

    fun getUSDValue(coin: String, exchange: String): Float{
        //Assumption that exchange has a coin to BTC value option at first,
        // if not then we use binance as a backup
        //if not then we might need to do an exhaustive search for coinBTC value
//        print("getting USD value")

        //Look for an exchange that has a coin to BTC value
        val coinBTCValue: Float = getCurrentValue(coin,exchange,"btc")
        //Look for the latest BTC to USD Value ratio
        val btcusdValue: Float = getCurrentValue("btc","gdax","usd")
        //Convert coinBTC value to coinUSD value

        val usdValue: Float = coinBTCValue * btcusdValue

        return usdValue
    }
    fun initExchangesForCoin(coinSymbol: String){
        //Check to see if we need to get exchanges only if coin doesnt have exchange list already
        if(data.coins[coinSymbol]?.exchanges?.count()!! > 0) return

        var url = URL("https://api.cryptowat.ch/assets/"+coinSymbol)
        val connection = url.openConnection() as HttpsURLConnection
        connection.requestMethod = "GET"
        if (connection.responseCode == 200) {
            // Success
            // Further processing here
            println("Response: " + connection.responseCode)
            val responseBody = connection.inputStream
            val responseBodyReader = InputStreamReader(responseBody, "UTF-8")
            //println(responseBodyReader)
            val jsonReader = JsonReader(responseBodyReader)
            jsonReader.beginObject()//Start results object
            jsonReader.nextName() //result
            jsonReader.beginObject()//Start results object
            jsonReader.nextName()
            jsonReader.nextInt() //id item
            jsonReader.nextName()
            jsonReader.nextString() //symboal
            jsonReader.nextName()
            jsonReader.nextString() // name
            jsonReader.nextName()
            jsonReader.nextBoolean() // fiat
            jsonReader.nextName() // Markets
            jsonReader.beginObject()//Start Markets object
            jsonReader.nextName() // Base item
            jsonReader.beginArray() // Start processing the base object
            while (jsonReader.hasNext()) { // Loop through all keys
                jsonReader.beginObject()
                jsonReader.nextName()
                var id = jsonReader.nextInt()
                jsonReader.nextName()
                val exchange = jsonReader.nextString()
                jsonReader.nextName()
                val pair = jsonReader.nextString()
                jsonReader.nextName()
                val active = jsonReader.nextBoolean()
                jsonReader.nextName()
                val exchangeurl = jsonReader.nextString()
                data.coins[coinSymbol].let { it?.exchanges?.add(Exchange(exchange,pair,active,exchangeurl)) }
                jsonReader.endObject()
//                println("id $id exchange: $exchange pair: $pair Active: $active exchangeURL: $exchangeurl")
            }
            jsonReader.close()
            data.coins.get(coinSymbol).let {println(it?.exchanges?.toString())}
            } else {
            // Error handling code goes here
        }

        connection.disconnect()
    }
    fun initCoins(){
        var url = URL("https://api.cryptowat.ch/assets")
        val connection = url.openConnection() as HttpsURLConnection
        connection.requestMethod = "GET"
        if (connection.responseCode == 200) {
            // Success
            // Further processing here
            println("Response: " + connection.responseCode)
            val responseBody = connection.inputStream
            val responseBodyReader = InputStreamReader(responseBody, "UTF-8")
            //println(responseBodyReader)
            val jsonReader = JsonReader(responseBodyReader)
            jsonReader.beginObject()//Start results object
            jsonReader.nextName()
            jsonReader.beginArray() // Start processing the JSON Array
            while (jsonReader.hasNext()) { // Loop through all keys
                jsonReader.beginObject()//Start object
                jsonReader.nextName()
                val id = jsonReader.nextInt()
                jsonReader.nextName()
                val symbol = jsonReader.nextString()
                jsonReader.nextName()
                val name = jsonReader.nextString()
                jsonReader.nextName()
                val fiatLegalTender = jsonReader.nextBoolean()
                jsonReader.nextName()
                val coinURL = jsonReader.nextString()
                data.coins[symbol] = Asset(id,symbol,name,fiatLegalTender,coinURL)

                println("id $id symbol $symbol name: $name Fiat: $fiatLegalTender URL: $coinURL")
                jsonReader.endObject()
            }
            jsonReader.close()
        } else {
            // Error handling code goes here
        }
        connection.disconnect()
    }
}