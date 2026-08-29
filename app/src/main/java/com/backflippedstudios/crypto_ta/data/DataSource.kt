package com.backflippedstudios.crypto_ta.data

import com.backflippedstudios.crypto_ta.data.retrofit.CryptoList
import com.backflippedstudios.crypto_ta.data.retrofit.Datum
import com.backflippedstudios.crypto_ta.data.retrofit.Quote
import com.backflippedstudios.crypto_ta.data.retrofit.USD
import com.github.mikephil.charting.data.Entry
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import org.ta4j.core.BaseTick
import org.ta4j.core.Decimal
import org.ta4j.core.Tick
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by C0rbin on 11/15/2017.
 * Rewritten 2026 to use the CoinGecko API (cryptowat.ch was shut down in 2023).
 */
class DataSource {

    // CoinGecko free-tier OHLC granularity is driven by the "days" window:
    // 1 day -> 30 min candles, 2-30 days -> 4 h candles, 31+ days -> 4 day candles.
    enum class Interval(val seconds: Long, val geckoDays: Int) {
        _1MIN(60, 1),
        _3MIN(60 * 3, 1),
        _5MIN(60 * 5, 1),
        _15MIN(60 * 15, 1),
        _30MIN(60 * 30, 1),
        _1HOUR(60 * 60, 7),
        _2HOUR(60 * 60 * 2, 14),
        _4HOUR(60 * 60 * 4, 30),
        _6HOUR(60 * 60 * 6, 30),
        _12HOUR(60 * 60 * 12, 30),
        _1DAY(60 * 60 * 24, 180),
        _3DAY(60 * 60 * 24 * 3, 365),
        _1WEEK(60 * 60 * 24 * 7, 365)
    }

    data class Exchange(
            @SerializedName("exchange") val exchange: String,
            @SerializedName("paring") val paring: String,
            @SerializedName("active") val active: Boolean,
            @SerializedName("url") val url: String
    )

    data class Asset(
            @SerializedName("id") val id: Int,
            @SerializedName("symboal") val symbol: String,
            @SerializedName("name") val name: String,
            @SerializedName("legal_tender") val FiatLegalTender: Boolean,
            @SerializedName("url") val url: String, // CoinGecko coin id, e.g. "ethereum"
            @SerializedName("exchanges") var exchanges: ArrayList<Exchange> = ArrayList()
    )

    object data {
        var coins: HashMap<String, Asset> = HashMap()
        var coinData: HashMap<String, ArrayList<Entry>> = HashMap()
        var lockCoinData: Lock = ReentrantLock()
        var marketCapList: CryptoList? = null
    }

    data class CoinData(
            val coinPair: String,
            var avgPercentChange: Double,
            val marketList: ArrayList<MarketData>
    )

    data class MarketData(
            val exchange: String,
            val coinPair: String,
            val lastPrice: Double,
            val percentChange: Double
    )

    // ---- CoinGecko response models ----

    private class GeckoSparkline {
        @SerializedName("price")
        var price: List<Double>? = null
    }

    private class GeckoMarket {
        @SerializedName("id")
        var id: String? = null
        @SerializedName("symbol")
        var symbol: String? = null
        @SerializedName("name")
        var name: String? = null
        @SerializedName("image")
        var image: String? = null
        @SerializedName("current_price")
        var currentPrice: Double? = null
        @SerializedName("market_cap")
        var marketCap: Double? = null
        @SerializedName("market_cap_rank")
        var marketCapRank: Int? = null
        @SerializedName("total_volume")
        var totalVolume: Double? = null
        @SerializedName("total_supply")
        var totalSupply: Double? = null
        @SerializedName("circulating_supply")
        var circulatingSupply: Double? = null
        @SerializedName("price_change_percentage_24h_in_currency")
        var percentChange24h: Double? = null
        @SerializedName("price_change_percentage_7d_in_currency")
        var percentChange7d: Double? = null
        @SerializedName("sparkline_in_7d")
        var sparkline: GeckoSparkline? = null
    }

    companion object {
        private const val API_ROOT = "https://api.coingecko.com/api/v3"
        private val gson = Gson()

        // Which provider produced the candles currently on screen
        @Volatile
        var lastCandleSource: String = "CoinGecko"

        // Set once by MainActivity; used for the on-disk response cache so the
        // app can open instantly with stale data while fresh data loads.
        @Volatile
        var appContext: android.content.Context? = null

        // Simple caches so spinner changes and the live-price timer don't hammer
        // CoinGecko's free-tier rate limit (~10-30 calls/min).
        private val tickCache = HashMap<String, Pair<Long, ArrayList<Tick>>>()
        private val priceCache = HashMap<String, Pair<Long, HashMap<String, Float>>>()
        private const val TICK_CACHE_MS = 60_000L
        private const val PRICE_CACHE_MS = 30_000L
    }

    private fun httpGetJson(urlStr: String): String? {
        for (attempt in 0..1) {
            try {
                val connection = URL(urlStr).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15_000
                connection.readTimeout = 15_000
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("User-Agent", "CryptoTA-Android")
                val code = connection.responseCode
                if (code == 200) {
                    val body = InputStreamReader(connection.inputStream, "UTF-8").use { it.readText() }
                    connection.disconnect()
                    return body
                }
                println("CoinGecko request failed ($code): $urlStr")
                connection.disconnect()
                // Rate limited: back off once, then give up so the UI isn't stalled
                if (code == 429 && attempt == 0) {
                    Thread.sleep(4_000)
                    continue
                }
                return null
            } catch (e: Exception) {
                println("CoinGecko request error for $urlStr: ${e.message}")
                return null
            }
        }
        return null
    }

    private fun cacheFile(key: String): java.io.File? =
            appContext?.let { java.io.File(it.cacheDir, "api_$key.json") }

    private fun writeCache(key: String, body: String) {
        try {
            cacheFile(key)?.writeText(body)
        } catch (e: Exception) {
        }
    }

    private fun readCache(key: String): String? {
        return try {
            cacheFile(key)?.takeIf { it.exists() }?.readText()
        } catch (e: Exception) {
            null
        }
    }

    // Network first; successful bodies are cached to disk and replayed when the
    // network (or the rate limit) fails.
    private fun httpGetJsonCached(urlStr: String, cacheKey: String): String? {
        val body = httpGetJson(urlStr)
        if (body != null) {
            writeCache(cacheKey, body)
            return body
        }
        val cached = readCache(cacheKey)
        if (cached != null) println("Using cached response for $cacheKey")
        return cached
    }

    private fun geckoIdFor(coin: String): String? = data.coins[coin.toLowerCase()]?.url

    /**
     * Loads the top coins from CoinGecko, populating the coin catalog, the
     * market-cap tab data, and the 24h/7d sparkline mini-charts in one request.
     */
    fun initCoinsGecko(): Boolean {
        val body = httpGetJson("$API_ROOT/coins/markets?vs_currency=usd&order=market_cap_desc" +
                "&per_page=250&page=1&sparkline=true&price_change_percentage=24h,7d") ?: return false
        val ok = parseMarkets(body)
        if (ok) writeCache("markets", body)
        return ok
    }

    // Instant cold open: replay the last successful market response from disk
    fun loadCoinsFromCache(): Boolean = readCache("markets")?.let { parseMarkets(it) } ?: false

    private fun parseMarkets(body: String): Boolean {
        val markets: Array<GeckoMarket> = try {
            gson.fromJson(body, Array<GeckoMarket>::class.java)
        } catch (e: Exception) {
            println("Failed to parse CoinGecko markets: ${e.message}")
            return false
        }

        val cryptoList = CryptoList()
        val datumList = ArrayList<Datum>()

        data.lockCoinData.lock()
        try {
            for (market in markets) {
                val geckoId = market.id ?: continue
                val symbol = market.symbol?.toLowerCase() ?: continue

                val exchanges = arrayListOf(
                        Exchange("CoinGecko", symbol + "usd", true, geckoId),
                        Exchange("CoinGecko", symbol + "btc", true, geckoId),
                        Exchange("CoinGecko", symbol + "eth", true, geckoId)
                )
                data.coins[symbol] = Asset(
                        market.marketCapRank ?: 0,
                        symbol,
                        market.name ?: symbol,
                        false,
                        geckoId,
                        exchanges)

                // Market-cap tab entry, mapped onto the existing model
                val datum = Datum()
                datum.id = market.marketCapRank
                datum.name = market.name
                datum.symbol = market.symbol?.toUpperCase()
                datum.slug = geckoId
                datum.imageUrl = market.image
                datum.totalSupply = market.totalSupply ?: market.circulatingSupply
                val usd = USD()
                usd.price = market.currentPrice
                usd.volume24h = market.totalVolume
                usd.marketCap = market.marketCap
                usd.percentChange24h = market.percentChange24h
                usd.percentChange7d = market.percentChange7d
                val quote = Quote()
                quote.usd = usd
                datum.quote = quote
                datumList.add(datum)

                // Sparkline (7d of hourly prices) feeds the mini charts
                val prices = market.sparkline?.price
                if (prices != null && prices.isNotEmpty()) {
                    val entries7d = ArrayList<Entry>()
                    prices.forEachIndexed { i, p -> entries7d.add(Entry(i.toFloat(), p.toFloat())) }
                    data.coinData[symbol + "_7d"] = entries7d

                    val last24 = prices.takeLast(24)
                    val entries24 = ArrayList<Entry>()
                    last24.forEachIndexed { i, p -> entries24.add(Entry(i.toFloat(), p.toFloat())) }
                    data.coinData[symbol + "_24h"] = entries24
                }
            }
        } finally {
            data.lockCoinData.unlock()
        }

        cryptoList.data = datumList
        data.marketCapList = cryptoList
        println("Loaded ${datumList.size} coins from CoinGecko")
        return datumList.isNotEmpty()
    }

    fun getData(coin: String, exchange: String, currency: String, interval: Interval): ArrayList<Tick> {
        val geckoId = geckoIdFor(coin) ?: return ArrayList()
        return getTicks(geckoId, currency.toLowerCase(), interval.geckoDays)
    }

    private fun getTicks(geckoId: String, vsCurrency: String, days: Int): ArrayList<Tick> {
        val cacheKey = "$geckoId/$vsCurrency/$days"
        synchronized(tickCache) {
            tickCache[cacheKey]?.let { (time, ticks) ->
                if (System.currentTimeMillis() - time < TICK_CACHE_MS) {
                    return ArrayList(ticks)
                }
            }
        }

        val ticks = ArrayList<Tick>()
        val body = httpGetJsonCached("$API_ROOT/coins/$geckoId/ohlc?vs_currency=$vsCurrency&days=$days",
                "ohlc_${geckoId}_${vsCurrency}_$days")
                ?: return ticks
        val candles: Array<DoubleArray> = try {
            gson.fromJson(body, Array<DoubleArray>::class.java)
        } catch (e: Exception) {
            println("Failed to parse OHLC: ${e.message}")
            return ticks
        }

        // CoinGecko OHLC carries no volume; use the rolling 24h volume series from
        // market_chart so volume-based indicators still have a real signal.
        val volumes = getVolumeSeries(geckoId, vsCurrency, days)

        var lastTick: Tick? = null
        for (candle in candles) {
            if (candle.size < 5) continue
            val timeMs = candle[0].toLong()
            var open = candle[1]
            val high = candle[2]
            val low = candle[3]
            val close = candle[4]
            val volume = nearestVolume(volumes, timeMs)

            val z = Instant.ofEpochMilli(timeMs).atZone(ZoneId.systemDefault())
            // Stitch open to the previous close so the chart has no gaps
            if (lastTick != null && !lastTick.closePrice.isEqual(Decimal.valueOf(open))) {
                open = lastTick.closePrice.toDouble()
            }
            val currentTick = BaseTick(z,
                    Decimal.valueOf(open),
                    Decimal.valueOf(high),
                    Decimal.valueOf(low),
                    Decimal.valueOf(close),
                    Decimal.valueOf(volume))
            // Filter out bad data if we get a zero value
            if (currentTick.closePrice.isEqual(Decimal.valueOf(0)) or currentTick.minPrice.isEqual(Decimal.valueOf(0))) {
                if (lastTick != null)
                    ticks.add(lastTick)
            } else {
                ticks.add(currentTick)
                lastTick = currentTick
            }
        }
        println("Parsed ${ticks.size} candles for $geckoId/$vsCurrency days=$days")

        if (ticks.isNotEmpty()) {
            synchronized(tickCache) {
                tickCache[cacheKey] = Pair(System.currentTimeMillis(), ArrayList(ticks))
            }
        }
        return ticks
    }

    private fun getVolumeSeries(geckoId: String, vsCurrency: String, days: Int): List<DoubleArray> {
        val body = httpGetJsonCached("$API_ROOT/coins/$geckoId/market_chart?vs_currency=$vsCurrency&days=$days",
                "vol_${geckoId}_${vsCurrency}_$days")
                ?: return emptyList()
        return try {
            val obj: JsonObject = JsonParser.parseString(body).asJsonObject
            gson.fromJson(obj.get("total_volumes"), Array<DoubleArray>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun nearestVolume(volumes: List<DoubleArray>, timeMs: Long): Double {
        if (volumes.isEmpty()) return 0.0
        var best = volumes[0]
        var bestDiff = Long.MAX_VALUE
        for (v in volumes) {
            if (v.size < 2) continue
            val diff = Math.abs(v[0].toLong() - timeMs)
            if (diff < bestDiff) {
                bestDiff = diff
                best = v
            }
        }
        return if (best.size >= 2) best[1] else 0.0
    }

    fun getCurrentValue(coin: String, exchange: String, currency: String): Float {
        val geckoId = geckoIdFor(coin) ?: return 0.0F
        val cur = currency.toLowerCase()

        synchronized(priceCache) {
            priceCache[geckoId]?.let { (time, prices) ->
                if (System.currentTimeMillis() - time < PRICE_CACHE_MS) {
                    return prices[cur] ?: 0.0F
                }
            }
        }

        val body = httpGetJson("$API_ROOT/simple/price?ids=$geckoId&vs_currencies=usd,btc,eth")
        if (body == null) {
            // Cache the miss so a failing endpoint isn't hammered every poll
            synchronized(priceCache) {
                priceCache[geckoId] = Pair(System.currentTimeMillis(), HashMap())
            }
            return 0.0F
        }
        return try {
            val obj = JsonParser.parseString(body).asJsonObject.getAsJsonObject(geckoId)
            val prices = HashMap<String, Float>()
            for ((key, value) in obj.entrySet()) {
                prices[key] = value.asFloat
            }
            synchronized(priceCache) {
                priceCache[geckoId] = Pair(System.currentTimeMillis(), prices)
            }
            prices[cur] ?: 0.0F
        } catch (e: Exception) {
            0.0F
        }
    }

    fun getUSDValue(coin: String, exchange: String): Float {
        return getCurrentValue(coin, exchange, "usd")
    }

    fun getMarketCapV2(): CryptoList? {
        if (data.marketCapList == null) {
            initCoinsGecko()
        }
        return data.marketCapList
    }

    fun getMarketSummary(): List<MarketData> {
        val allCoinData = ArrayList<MarketData>()
        data.marketCapList?.data?.forEach { datum ->
            allCoinData.add(MarketData(
                    "CoinGecko",
                    (datum.symbol ?: "") + "/USD",
                    datum.quote?.usd?.price ?: 0.0,
                    datum.quote?.usd?.percentChange24h ?: 0.0))
        }
        return allCoinData.sortedByDescending { it.percentChange }
    }

    // Exchange lists are populated up front by initCoinsGecko; kept for API compatibility.
    fun initExchangesForCoin(coinSymbol: String) = Unit
}
