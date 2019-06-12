package net.azarquiel.fukkuapp.views

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login.*
import net.azarquiel.fukkuapp.R
import net.azarquiel.fukkuapp.service.MyFirebaseInstanceIdService
import org.jetbrains.anko.*

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "EmailPassword"
    }

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        btnLogin.setOnClickListener{
            val email = fieldEmail.text.toString()
            val password = fieldPassword.text.toString()
            signIn(email,password)
        }

        btnCreateAccount.setOnClickListener{
            val intent = Intent(this, CreateUserActivity::class.java)
            startActivity(intent)
        }

        btnResetPass.setOnClickListener {
            alert {
                customView {
                    title = "Resetear contraseña"
                    verticalLayout {
                        val etEmail = editText {
                            hint = "Introduce tu email"
                            padding = dip(20)
                        }
                        positiveButton("Enviar") {
                            if (TextUtils.isEmpty(etEmail.text)){
                                longToast("Debes introducir un email")
                            } else {
                                val emailAddress = etEmail.text.toString()

                                auth.sendPasswordResetEmail(emailAddress)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.d(TAG, "Email sent.")
                                        }
                                    }

                                longToast("Enviado email de cambio de contraseña")
                            }
                        }
                        negativeButton("Cancelar") {
                        }
                    }
                }
            }.show()
        }

    }

    private fun signIn(email: String, password: String) {
        Log.d(TAG, "signIn:$email")
        if (!validateForm()) {
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")

                    val registrationToken = FirebaseInstanceId.getInstance().token
                    MyFirebaseInstanceIdService.addTokentoFireStore(registrationToken)

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = fieldEmail.text.toString()
        if (TextUtils.isEmpty(email)) {
            fieldEmail.error = "Required."
            valid = false
        } else {
            fieldEmail.error = null
        }

        val password = fieldPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            fieldPassword.error = "Required."
            valid = false
        } else {
            fieldPassword.error = null
        }

        return valid
    }
}
