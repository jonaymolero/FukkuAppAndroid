package net.azarquiel.fukkuapp.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.robertlevonyan.components.picker.Str
import kotlinx.android.synthetic.main.row_chat.view.*
import net.azarquiel.fukkuapp.model.Chat
import net.azarquiel.fukkuapp.model.Producto
import net.azarquiel.fukkuapp.model.User
import net.azarquiel.fukkuapp.util.COLECCION_PRODUCTOS
import net.azarquiel.fukkuapp.util.COLECCION_USUARIOS
import net.azarquiel.fukkuapp.util.FirestoreUtil

/**
 * Created by gongaloagain on 25/05/19.
 */
class ChatsAdapter(val context: Context,
                    val layout: Int
                    ) : RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {

    private var dataList: List<Chat> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val viewlayout = layoutInflater.inflate(layout, parent, false)
        return ViewHolder(viewlayout, context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    internal fun setChats(chats: List<Chat>) {
        this.dataList = chats
        notifyDataSetChanged()
    }


    class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Chat){
            itemView.tag = dataItem
            getProduct(dataItem)
        }

        private fun getProduct(dataItem: Chat) {
            val docRef = FirebaseFirestore.getInstance().collection(COLECCION_PRODUCTOS).document(dataItem.productID)
            docRef.get().addOnSuccessListener { documentSnapshot ->
                val producto = documentSnapshot.toObject(Producto::class.java)
                getUser(producto!!.nombre, dataItem)
            }
        }

        private fun getUser(productName: String, dataItem: Chat) {
            val docRef = FirebaseFirestore.getInstance().collection(COLECCION_USUARIOS).document(dataItem.otherUserID)
            docRef.get().addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                showInfo(productName, user!!.name)
            }
        }

        private fun showInfo(productName: String, userName: String) {
            Log.d("NOMBRE PRODUCTO", "Saca esto: $productName")
            Log.d("NOMBRE USUARIO", "Saca esto: $userName")
            itemView.tvChat.text = "$userName - $productName"
        }

    }
}