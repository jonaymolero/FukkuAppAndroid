@file:Suppress("DEPRECATION")

package net.azarquiel.fukkuapp.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import net.azarquiel.fukkuapp.util.FirestoreUtil

class MyFirebaseInstanceIdService : FirebaseInstanceIdService() {
    override fun onTokenRefresh() {
        val newResgistrationToken = FirebaseInstanceId.getInstance().token

        if (FirebaseAuth.getInstance().currentUser != null)
            addTokentoFireStore(newResgistrationToken)
    }

    companion object {
        fun addTokentoFireStore(newResgistrationToken: String?){
            if (newResgistrationToken == null) throw NullPointerException("FCM is null")

            FirestoreUtil.getFCMRegistrationTokens { tokens ->
                if (tokens.contains(newResgistrationToken))
                    return@getFCMRegistrationTokens

                tokens.add(newResgistrationToken)
                FirestoreUtil.setFCMRegistrationTokens(tokens)
            }
        }
    }
}