package com.backflippedstudios.crypto_ta

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFCM : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val TAG = "JSA-FCM"
        super.onMessageReceived(message)
        println("Message received!")
        //Check to see if notifications are on, if so go head and create notification
        if (message.notification != null) {
            Log.e(TAG, "Title: " + message.notification?.title)
            Log.e(TAG, "Body: " + message.notification?.body)
            sendNotification(message.notification?.body, message.data["link"], message.data["openPlayStore"]?.toBoolean())
        }

        if (message.data.isNotEmpty()) {
            Log.e(TAG, "Data: " + message.data)
        }
    }

    private fun sendNotification(body: String?, url: String?, openPlayStore: Boolean?) {
        val intent = Intent(this, MainActivity::class.java)
        if (openPlayStore == true) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.putExtra("openPlayStore", true)
        } else {
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("Notification", body)
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val channelID = "openPlayStoreActivity"
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                    NotificationChannel(channelID, "CryptoTA", NotificationManager.IMPORTANCE_DEFAULT))
        }

        val notificationBuilder = NotificationCompat.Builder(this@MyFCM, channelID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Push Notification FCM")
                .setContentText(body)
                .setStyle(null)
                .setAutoCancel(false)
                .setSound(notificationSound)
                .setChannelId(channelID)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(101, notificationBuilder.build())
    }
}
