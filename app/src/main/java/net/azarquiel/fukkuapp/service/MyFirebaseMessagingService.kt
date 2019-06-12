package net.azarquiel.fukkuapp.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.notification != null)
            //TODO: Show notificstion
            Log.d("Hola",remoteMessage.data.toString())
    }
}