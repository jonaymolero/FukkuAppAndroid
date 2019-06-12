package net.azarquiel.fukkuapp.views

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent = if (FirebaseAuth.getInstance().currentUser == null)
            Intent(applicationContext, LoginActivity::class.java)
        else
            Intent(applicationContext, MainActivity::class.java)

        startActivity(intent)
        finish()
    }
}
