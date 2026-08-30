package com.backflippedstudios.crypto_ta

import android.content.Context
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics

// No-ops when Firebase isn't configured (no google-services.json), so the app
// runs with or without it.
object Analytics {
    private var firebase: FirebaseAnalytics? = null

    fun init(context: Context) {
        if (FirebaseApp.getApps(context).isNotEmpty()) {
            firebase = FirebaseAnalytics.getInstance(context)
        }
    }

    fun logEvent(name: String, bundle: Bundle) {
        firebase?.logEvent(name, bundle)
    }

    fun subscribeToTopic(topic: String) {
        if (firebase != null) {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic(topic)
        }
    }

    fun unsubscribeFromTopic(topic: String) {
        if (firebase != null) {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
        }
    }
}
