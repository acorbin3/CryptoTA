package com.backflippedstudios.crypto_ta.data

import com.backflippedstudios.crypto_ta.data.retrofit.CryptoList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ta4j.core.Tick

/**
 * Suspend-function facade over [DataSource]. Every call runs on the IO
 * dispatcher, so callers can invoke these from any coroutine (including ones
 * started on the main thread) without blocking the UI or tripping
 * NetworkOnMainThreadException.
 */
object CryptoRepository {

    private val dataSource = DataSource()

    suspend fun getCandles(coin: String, exchange: String, currency: String,
                           interval: DataSource.Interval): ArrayList<Tick> =
            withContext(Dispatchers.IO) {
                dataSource.getData(coin, exchange, currency, interval)
            }

    suspend fun getCurrentValue(coin: String, exchange: String, currency: String): Float =
            withContext(Dispatchers.IO) {
                dataSource.getCurrentValue(coin, exchange, currency)
            }

    suspend fun getUSDValue(coin: String, exchange: String): Float =
            withContext(Dispatchers.IO) {
                dataSource.getUSDValue(coin, exchange)
            }

    /** Fetch the coin catalog + market-cap data from the network. */
    suspend fun refreshMarkets(): Boolean =
            withContext(Dispatchers.IO) {
                dataSource.initCoinsGecko()
            }

    /** Populate the catalog from the last cached response, if any. */
    suspend fun loadCachedMarkets(): Boolean =
            withContext(Dispatchers.IO) {
                dataSource.loadCoinsFromCache()
            }

    suspend fun getMarketCap(): CryptoList? =
            withContext(Dispatchers.IO) {
                dataSource.getMarketCapV2()
            }
}
