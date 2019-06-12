package net.azarquiel.fukkuapp.views

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_create_user.*
import net.azarquiel.fukkuapp.R
import net.azarquiel.fukkuapp.model.User
import net.azarquiel.fukkuapp.service.MyFirebaseInstanceIdService
import net.azarquiel.fukkuapp.util.FirestoreUtil

class CreateUserActivity : AppCompatActivity() {

    companion object {
        const val TAG="Gonzalo"
    }

    private lateinit var surnames: String
    private lateinit var user: FirebaseUser
    private lateinit var password: String
    private lateinit var email: String
    private lateinit var name: String
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        auth = FirebaseAuth.getInstance()


        btnEntrar.setOnClickListener{
            name = etName.text.toString()
            surnames = etSurnames.text.toString()
            email = etEmail.text.toString()
            password = etPassword.text.toString()

            createAccount(email, password)
        }
    }

    private fun createAccount(email: String, password: String) {
        Log.d(TAG, "createAccount:$email")
        if (!validateForm()) {
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    createUserFirestore()

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createUserFirestore() {
        user = auth.currentUser!!
        val userFirestore = User(name, surnames, email, user.uid, mutableListOf(),"","","")
        FirestoreUtil.createUserFirestore(userFirestore)

        val registrationToken = FirebaseInstanceId.getInstance().token
        MyFirebaseInstanceIdService.addTokentoFireStore(registrationToken)

        addDisplayName()
    }

    private fun addDisplayName() {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User profile updated.")

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
    }

    private fun validateForm(): Boolean {
        var valid = true

        if (TextUtils.isEmpty(name)) {
            etName.error = "Required."
            valid = false
        } else {
            etName.error = null
        }

        if (TextUtils.isEmpty(surnames)) {
            etSurnames.error = "Required."
            valid = false
        } else {
            etName.error = null
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.error = "Required."
            valid = false
        } else {
            etEmail.error = null
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.error = "Required."
            valid = false
        } else {
            etPassword.error = null
        }

        return valid
    }
}