package com.backflippedstudios.crypto_ta

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.content.Context
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFCM: FirebaseMessagingService() {
    override fun onMessageReceived(p0: RemoteMessage?) {
        val TAG = "JSA-FCM"
        super.onMessageReceived(p0)
        println("Message received!")
        //Check to see if notifications are on, if so go head and create notification
        if (p0?.notification != null) {
            Log.e(TAG, "Title: " + p0.notification?.title)
            Log.e(TAG, "Body: " + p0.notification?.body)
            sendNotification(p0.notification?.body, p0.data["link"], p0.data["openPlayStore"]?.toBoolean() )
        }

        if (p0?.data!!.isNotEmpty()) {
            Log.e(TAG, "Data: " + p0.data)
        }
    }
    private fun sendNotification(body: String?, url: String?, openPlayStore: Boolean?) {
        lateinit var intent: Intent

        //If set, and the activity being launched is already running in the current task,
        //then instead of launching a new instance of that activity, all of the other activities
        // on top of it will be closed and this Intent will be delivered to the (now on top)
        // old activity as a new Intent.
        var packageInfo = this.packageManager.getPackageInfo(packageName,0)
        println("packageInfo.versionName:${packageInfo.versionName}")
        if(openPlayStore!!){
            println("Open Play Store")
            intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.putExtra("openPlayStore",true)
        }
        else{
            intent = Intent(this,MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("Notification",body)
        }





        var pendingIntent = PendingIntent.getActivity(this,0,intent,0/*Flag indicating that this PendingIntent can be used only once.*/)
        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val channelID = "openPlayStoreActivity"
        var notificationBuilder = NotificationCompat.Builder(this@MyFCM,channelID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Push Notification FCM")
                .setContentText(body)
                .setStyle(null)
                .setAutoCancel(false)
                .setSound(notificationSound)
                .setChannelId(channelID)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        var notificationManager: NotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(101,notificationBuilder.build())
    }
}