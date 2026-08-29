package com.backflippedstudios.crypto_ta.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.backflippedstudios.crypto_ta.MainActivity
import com.backflippedstudios.crypto_ta.R
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class PriceAlert(
        val symbol: String,
        val geckoId: String,
        val above: Boolean,
        val threshold: Double
)

object PriceAlertStore {
    private const val PREFS = "com.backflippedstudios.saved.prefs"
    private const val KEY = "price_alerts"
    private val gson = Gson()

    fun load(context: Context): MutableList<PriceAlert> {
        val json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY, null) ?: return ArrayList()
        return try {
            gson.fromJson(json, object : TypeToken<MutableList<PriceAlert>>() {}.type)
        } catch (e: Exception) {
            ArrayList()
        }
    }

    fun save(context: Context, alerts: List<PriceAlert>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY, gson.toJson(alerts)).apply()
    }

    fun add(context: Context, alert: PriceAlert) {
        val alerts = load(context)
        alerts.add(alert)
        save(context, alerts)
    }
}

/**
 * Periodic background check (WorkManager, ~15 min) of all saved price alerts.
 * Triggered alerts fire a notification and are removed (one-shot alerts).
 */
class PriceAlertWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val alerts = PriceAlertStore.load(applicationContext)
        if (alerts.isEmpty()) return Result.success()

        val ids = alerts.map { it.geckoId }.distinct().joinToString(",")
        val prices = fetchPrices(ids) ?: return Result.retry()

        val remaining = ArrayList<PriceAlert>()
        alerts.forEachIndexed { index, alert ->
            val price = prices[alert.geckoId]
            if (price == null) {
                remaining.add(alert)
                return@forEachIndexed
            }
            val triggered = if (alert.above) price >= alert.threshold else price <= alert.threshold
            if (triggered) {
                notifyAlert(alert, price, index)
            } else {
                remaining.add(alert)
            }
        }
        PriceAlertStore.save(applicationContext, remaining)
        return Result.success()
    }

    private fun fetchPrices(ids: String): Map<String, Double>? {
        return try {
            val url = URL("https://api.coingecko.com/api/v3/simple/price?ids=$ids&vs_currencies=usd")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("User-Agent", "CryptoTA-Android")
            if (connection.responseCode != 200) {
                connection.disconnect()
                return null
            }
            val body = InputStreamReader(connection.inputStream, "UTF-8").use { it.readText() }
            connection.disconnect()
            val obj = JsonParser.parseString(body).asJsonObject
            val result = HashMap<String, Double>()
            for ((key, value) in obj.entrySet()) {
                result[key] = value.asJsonObject.get("usd")?.asDouble ?: continue
            }
            result
        } catch (e: Exception) {
            null
        }
    }

    private fun notifyAlert(alert: PriceAlert, price: Double, notificationId: Int) {
        val manager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "price_alerts"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                    NotificationChannel(channelId, "Price alerts", NotificationManager.IMPORTANCE_HIGH))
        }
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val direction = if (alert.above) "above" else "below"
        val notification = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("${alert.symbol} is $direction $${alert.threshold}")
                .setContentText("${alert.symbol} is now $$price")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        manager.notify(2000 + notificationId, notification)
    }
}
