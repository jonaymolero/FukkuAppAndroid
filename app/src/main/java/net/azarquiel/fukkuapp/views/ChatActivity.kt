package net.azarquiel.fukkuapp.views

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.content_profile.*
import net.azarquiel.fukkuapp.AppConstants
import net.azarquiel.fukkuapp.R
import net.azarquiel.fukkuapp.model.Message
import net.azarquiel.fukkuapp.model.ProductoPruebas
import net.azarquiel.fukkuapp.adapter.MessagesAdapter
import net.azarquiel.fukkuapp.model.Producto
import net.azarquiel.fukkuapp.util.CAMPO_IDPRODUCTO
import net.azarquiel.fukkuapp.util.FirestoreUtil
import java.util.*
import kotlin.collections.ArrayList

class ChatActivity : AppCompatActivity() {

    private lateinit var product: Producto
    private lateinit var productID: String
    private lateinit var database: FirebaseFirestore
    private lateinit var adapter: MessagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        database = FirebaseFirestore.getInstance()
        val channelID = intent.getStringExtra(AppConstants.CHANNEL_ID)
        val otherUserID = intent.getStringExtra(AppConstants.OTHER_USER_ID)
        productID = intent.getStringExtra(AppConstants.PRODUCT_ID)

        getProduct()

        adapter = MessagesAdapter(this, R.layout.row_message)
        rvMessages.layoutManager = LinearLayoutManager(this)
        rvMessages.adapter = adapter

        database.collection("Canales/$channelID/Mensajes")
            .orderBy("time")
            .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e)
                    return@EventListener
                }

                val mensajes = ArrayList<Message>()
                for (doc in value!!) {
                    mensajes.add(doc.toObject(Message::class.java))
                }

                adapter.setMessages(mensajes)
                rvMessages.scrollToPosition(adapter.itemCount-1)
            })



        btnSendMessage.setOnClickListener {

            val mensaje = Message(etMessage.text.toString(),
                Calendar.getInstance().time,
                FirebaseAuth.getInstance().currentUser!!.uid,
                otherUserID,
                FirebaseAuth.getInstance().currentUser!!.displayName!!,
                channelID,
                productID)
            etMessage.text.clear()

            FirestoreUtil.sendMessage(mensaje, channelID)
        }

        cvProductChat.setOnClickListener {
            val intent= Intent(this, DetailProductActivity::class.java)
            intent.putExtra("producto", product.id)
            startActivity(intent)
        }

    }

    private fun getProduct() {
        val docRef = database.document("Productos/$productID")
        docRef.addSnapshotListener(EventListener<DocumentSnapshot> { snapshot, e ->
            if (e != null) {
                Log.w("PROFILE", "Listen failed.", e)
                return@EventListener
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d("PROFILE", "Current data: ${snapshot.data}")
                product = snapshot.toObject(Producto::class.java)!!
                showProduct()

            } else {
                Log.d("PROFILE", "Current data: null")
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun showProduct() {
        tvChatProduct.text = product.nombreUsuario + " - " + product.nombre

        if(product.imagen != ""){
            Glide.with(this).load(product.imagen).into(ivChatProduct)
        }else{
            ivChatProduct.setImageResource(R.drawable.notfound)
        }
    }

}
