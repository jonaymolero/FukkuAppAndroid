package net.azarquiel.fukkuapp.views

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_open_chats.*
import net.azarquiel.fukkuapp.AppConstants
import net.azarquiel.fukkuapp.R
import net.azarquiel.fukkuapp.model.Chat
import net.azarquiel.fukkuapp.model.ChatChannel
import net.azarquiel.fukkuapp.util.FirestoreUtil
import net.azarquiel.fukkuapp.adapter.ChatsAdapter

class OpenChatsActivity : AppCompatActivity() {

    private lateinit var otherUserID: String
    private lateinit var database: FirebaseFirestore
    private lateinit var adapter: ChatsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_chats)

        adapter = ChatsAdapter(this, R.layout.row_chat)
        rvOpenChats.layoutManager = LinearLayoutManager(this)
        rvOpenChats.adapter = adapter

        database = FirebaseFirestore.getInstance()
        database.collection("Usuarios/${FirebaseAuth.getInstance().currentUser!!.uid}/Chats")
            .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e)
                    return@EventListener
                }

                val chats = ArrayList<Chat>()
                for (doc in value!!) {
                    chats.add(doc.toObject(Chat::class.java))
                }

                adapter.setChats(chats)
            })
    }

    fun onClickChat(view: View){
        val channel = view.tag as Chat
        val docRef = database.collection("Canales").document(channel.channelID)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            val users = documentSnapshot.toObject(ChatChannel::class.java)
            users?.usersID?.forEach{
                if (it != FirebaseAuth.getInstance().currentUser!!.uid){
                    otherUserID = it
                }
            }

            FirestoreUtil.getOrCreateChatChannel(otherUserID, channel.productID){ channelID ->
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra(AppConstants.CHANNEL_ID, channelID)
                intent.putExtra(AppConstants.OTHER_USER_ID,otherUserID)
                intent.putExtra(AppConstants.PRODUCT_ID,channel.productID)
                startActivity(intent)
            }
        }
    }

}
