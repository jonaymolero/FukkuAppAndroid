package net.azarquiel.fukkuapp.views

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.robertlevonyan.components.picker.ItemModel
import com.robertlevonyan.components.picker.PickerDialog
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.content_profile.*
import net.azarquiel.fukkuapp.R
import net.azarquiel.fukkuapp.model.User
import org.jetbrains.anko.*
import java.util.*


class ProfileActivity : AppCompatActivity() {

    private lateinit var pickerDialog: PickerDialog
    private lateinit var user: FirebaseUser
    private lateinit var docRef: DocumentReference
    private lateinit var db: FirebaseFirestore
    private lateinit var userFirestore: User
    private var editable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(toolbar)

        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        itProfileBirthday.inputType = InputType.TYPE_NULL
        itProfileGender.inputType = InputType.TYPE_NULL

        user = FirebaseAuth.getInstance().currentUser!!
        itProfileName.isEnabled = editable

        getUser()
        editData()

        btnProfileChangePass.setOnClickListener { changePass() }
        itProfileBirthday.setOnClickListener{ setBirthDate() }
        itProfileGender.setOnClickListener { setGender() }
    }

    private fun setGender() {
        lateinit var dialog: AlertDialog
        // Initialize an array of genders
        val array = arrayOf("Hombre","Mujer")

        // Initialize a new instance of alert dialog builder object
        val builder = AlertDialog.Builder(this)

        // Set a title for alert dialog
        builder.setTitle("Sexo")

        // Set the single choice items for alert dialog with initial selection
        builder.setSingleChoiceItems(array,-1) { _, which->
            // Get the dialog selected item
            val gender = array[which]

            itProfileGender.setText(gender)

            // Dismiss the dialog
            dialog.dismiss()
        }

        // Initialize the AlertDialog using builder object
        dialog = builder.create()
        // Finally, display the alert dialog
        dialog.show()
    }

    private fun setBirthDate() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { datePicker, mYear, mMonth, mDay ->
                itProfileBirthday.setText("$mDay/$mMonth/$mYear")
            }, year, month, day
        )

        datePickerDialog.show()
    }

    private fun changePass() {
        alert {
            customView {
                title = "Cambiar contraseña"
                verticalLayout {
                    val etPass = editText {
                        hint = "Introduzca la nueva contraseña"
                        padding = dip(20)
                    }
                    val etPassConfirm = editText {
                        hint = "Confirme la nueva contraseña"
                        padding = dip(20)
                    }
                    positiveButton("Enviar") {
                        if (TextUtils.isEmpty(etPass.text) || TextUtils.isEmpty(etPassConfirm.text)){
                            longToast("Debes rellenar todos los campos")
                        } else {
                            if (etPass.text.toString() == etPassConfirm.text.toString()){
                                val newPassword = etPass.text.toString()

                                user?.updatePassword(newPassword)
                                    ?.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.d("Password", "User password updated.")
                                            longToast("Contraseña cambiada")
                                        } else {
                                            longToast("La contraseña debe tener mínimo 6 caracteres")
                                        }
                                    }
                            } else {
                                longToast("Las contraseñas no coinciden")
                            }
                        }
                    }
                    negativeButton("Cancelar") {
                    }
                }
            }
        }.show()
    }

    private fun updateUser() {
        if (validateForm()){
            user?.updateEmail(itProfileEmail.text.toString())
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Email", "User email address updated.")
                    }
                }

            docRef.update(mapOf(
                "name" to itProfileName.text.toString(),
                "surnames" to itProfileSurnames.text.toString(),
                "gender" to itProfileGender.text.toString(),
                "birthday" to itProfileBirthday.text.toString(),
                "email" to itProfileEmail.text.toString()
            ))


            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(itProfileName.text.toString())
                .build()

            user.updateProfile(profileUpdates)
            toast("Datos actualizados")
        } else {
            itProfileEmail.setText(userFirestore.email)
            toast("Debes introducir una dirección de correo")
        }

        editData()
    }

    private fun editData() {
        itProfileSurnames.isEnabled = editable
        itProfileBirthday.isEnabled = editable
        itProfileGender.isEnabled = editable
        itProfileEmail.isEnabled = editable
        if (!editable) {
            btnProfileChangePass.visibility = INVISIBLE
            ivProfile.setOnClickListener(null)
        } else {
            btnProfileChangePass.visibility = VISIBLE
            ivProfile.setOnClickListener { picker() }
        }
    }

    private fun picker() {
        val itemModelc = ItemModel(ItemModel.ITEM_CAMERA)
        val itemModelg = ItemModel(ItemModel.ITEM_GALLERY)
        pickerDialog = PickerDialog.Builder(this)
            .setListType(PickerDialog.TYPE_GRID)
            .setItems(arrayListOf(itemModelg, itemModelc))
            .setDialogStyle(PickerDialog.DIALOG_MATERIAL)
            .create()

        pickerDialog.setPickerCloseListener { type, uri ->
            when (type) {
                ItemModel.ITEM_CAMERA -> {
                    uploadImage(uri)
                }
                ItemModel.ITEM_GALLERY -> {
                    uploadImage(uri)
                }
            }
        }

        pickerDialog.show(supportFragmentManager, "")
    }

    private fun uploadImage(uriImage: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val riversRef = storageRef.child("profile_photos/${FirebaseAuth.getInstance().currentUser!!.uid}").child(uriImage.lastPathSegment!!)
        val uploadTask = riversRef.putFile(uriImage)
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener {
            toast("Fallo al subir la imagen")
        }.addOnSuccessListener {
            getUrlImage(riversRef.path)
        }
    }

    private fun getUrlImage(path: String) {
        var storageRef = FirebaseStorage.getInstance().reference
        storageRef.child(path).downloadUrl.addOnSuccessListener {
            docRef.update("profilePhoto", it.toString())

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(it.toString()))
                .build()

            user.updateProfile(profileUpdates)
        }
    }

    private fun getUser() {
        db = FirebaseFirestore.getInstance()
        docRef = db.collection("Usuarios").document(FirebaseAuth.getInstance().currentUser!!.uid)
        docRef.addSnapshotListener(EventListener<DocumentSnapshot> { snapshot, e ->
            if (e != null) {
                Log.w("PROFILE", "Listen failed.", e)
                return@EventListener
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d("PROFILE", "Current data: ${snapshot.data}")
                userFirestore = snapshot.toObject(User::class.java)!!
                pintar()

            } else {
                Log.d("PROFILE", "Current data: null")
            }
        })
    }

    private fun changeAction(item: MenuItem): Boolean{
        if (!editable){
            item.setIcon(R.drawable.ic_action_save)
        } else {
            item.setIcon(R.drawable.ic_action_edit)
        }
        editable = !editable

        if (editable){
            editData()
        } else {
            updateUser()
        }

        return true
    }

    @SuppressLint("SetTextI18n")
    private fun pintar() {

        tvProfileName.text = userFirestore.name + " " + userFirestore.surnames.substring(0,1) + "."

        itProfileName.setText(userFirestore.name)
        itProfileSurnames.setText(userFirestore.surnames)
        itProfileGender.setText(userFirestore.gender)
        itProfileBirthday.setText(userFirestore.birthday)
        itProfileEmail.setText(userFirestore.email)

        if(userFirestore.profilePhoto != ""){
            Glide.with(this).load(userFirestore.profilePhoto).into(ivProfile)
        }else{
            ivProfile.setImageResource(R.drawable.ic_fukku_logo)
        }

        /*val ivAvatar = nav_view.getHeaderView(0).imageUser
        if (user.photoUrl.toString() != "null") Glide.with(this).load(user.photoUrl).into(ivAvatar)
        val tvNick = nav_view.getHeaderView(0).nick
        tvNick.text = user.displayName*/
    }

    private fun validateForm(): Boolean {
        return !TextUtils.isEmpty(itProfileEmail.text)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            //R.id.action_flag -> true
            R.id.action_flag -> changeAction(item)
            else -> super.onOptionsItemSelected(item)
        }
    }
}
