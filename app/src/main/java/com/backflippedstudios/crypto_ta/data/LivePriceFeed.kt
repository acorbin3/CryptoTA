package com.backflippedstudios.crypto_ta.data

import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

/**
 * Streams live trade prices from the Coinbase Exchange public WebSocket feed.
 * If the product doesn't exist on Coinbase (or the socket drops), nothing
 * streams and the existing polling timer remains the price source.
 */
object LivePriceFeed {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
                .pingInterval(20, TimeUnit.SECONDS)
                .build()
    }

    private var webSocket: WebSocket? = null

    @Volatile
    var currentProduct: String? = null
        private set

    // True once a ticker message for the current product has arrived
    @Volatile
    var isStreaming: Boolean = false
        private set

    @Volatile
    var onPrice: ((product: String, price: Float) -> Unit)? = null

    /** Subscribe to e.g. "ETH-USD". Replaces any previous subscription. */
    @Synchronized
    fun subscribe(product: String) {
        if (product == currentProduct && webSocket != null) return
        stop()
        currentProduct = product

        val request = Request.Builder()
                .url("wss://ws-feed.exchange.coinbase.com")
                .build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                ws.send("""{"type":"subscribe","product_ids":["$product"],"channels":["ticker"]}""")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    val obj = JsonParser.parseString(text).asJsonObject
                    val type = obj.get("type")?.asString
                    if (type == "ticker" && obj.get("product_id")?.asString == currentProduct) {
                        val price = obj.get("price")?.asFloat ?: return
                        isStreaming = true
                        onPrice?.invoke(product, price)
                    } else if (type == "error") {
                        println("Coinbase feed error for $product: ${obj.get("reason")?.asString}")
                        isStreaming = false
                    }
                } catch (e: Exception) {
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                println("Coinbase feed failure: ${t.message}")
                isStreaming = false
            }
        })
    }

    @Synchronized
    fun stop() {
        isStreaming = false
        webSocket?.close(1000, null)
        webSocket = null
        currentProduct = null
    }
}
