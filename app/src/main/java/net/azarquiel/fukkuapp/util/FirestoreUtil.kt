package net.azarquiel.fukkuapp.util

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import net.azarquiel.fukkuapp.model.*
import net.azarquiel.fukkuapp.views.CreateUserActivity


object FirestoreUtil {

    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document("Usuarios/${FirebaseAuth.getInstance().uid
            ?: throw  NullPointerException("UID es null")}")

    fun createUserFirestore(userFirestore: User){
        currentUserDocRef.set(userFirestore)
            .addOnSuccessListener { Log.d(CreateUserActivity.TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(CreateUserActivity.TAG, "Error writing document", e) }
    }

    private val chatChannelCollectionRef = firestoreInstance.collection("Canales")

    fun getOrCreateChatChannel( otherUserID: String,
                                productID: String,
                                onComplete: (channelID: String) -> Unit){
        currentUserDocRef.collection("Chats")
            .document("$productID-$otherUserID").get().addOnSuccessListener {
                if (it.exists()){
                    onComplete(it.toObject(Chat::class.java)!!.channelID)
                    return@addOnSuccessListener
                }

                val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid

                val newChannel = chatChannelCollectionRef.document()
                newChannel.set(ChatChannel(mutableListOf(currentUserID, otherUserID)))

                currentUserDocRef
                    .collection("Chats")
                    .document("$productID-$otherUserID")
                    .set(Chat(newChannel.id,productID,otherUserID))

                firestoreInstance.collection("Usuarios").document(otherUserID)
                    .collection("Chats")
                    .document("$productID-${uidUser()}")
                    .set(Chat(newChannel.id,productID, uidUser()))

                onComplete(newChannel.id)
            }
    }

    fun sendMessage(message: Message, channelID: String){
        chatChannelCollectionRef.document(channelID)
            .collection("Mensajes")
            .add(message)
    }

    //region FCM
    fun getFCMRegistrationTokens(onComplete: (tokens: MutableList<String>) -> Unit){
        currentUserDocRef.get().addOnSuccessListener {
            val user = it.toObject(User::class.java)!!
            onComplete(user.registrationTokens)
        }
    }

    fun setFCMRegistrationTokens(registrationTokens: MutableList<String>){
        currentUserDocRef.update("registrationTokens", registrationTokens)
    }
    //endregion FCM

    fun addToCategoriasFavoritas(categoria: Categoria){
        firestoreInstance.collection(COLECCION_USUARIOS).document(uidUser()).collection(SUBCOLECCION_CATEGORIAS_FAVORITOS)
            .document(categoria.id).set(categoria)
    }

    fun deleteToCategoriasFavoritas(categoria: Categoria){
        firestoreInstance.collection(COLECCION_USUARIOS).document(uidUser()).collection(SUBCOLECCION_CATEGORIAS_FAVORITOS)
            .document(categoria.id).delete()
    }

    fun addToProductosFavoritos(producto: Producto){
        var fav = Favorito(producto.id)
        firestoreInstance.collection(COLECCION_USUARIOS).document(uidUser()).collection(SUBCOLECCION_PRODUCTOS_FAVORITOS)
            .document(fav.id).set(fav)
    }

    fun deleteToProductosFavoritos(producto: Producto){
        firestoreInstance.collection(COLECCION_USUARIOS).document(uidUser()).collection(SUBCOLECCION_PRODUCTOS_FAVORITOS)
            .document(producto.id).delete()
    }

    fun updateProducto(idProducto:String, producto:Producto){
        firestoreInstance.collection(COLECCION_USUARIOS).document(uidUser()).collection(SUBCOLECCION_PRODUCTOS).document(idProducto).set(producto)
        firestoreInstance.collection(COLECCION_CATEGORIA).document(producto.categoriaId).collection(SUBCOLECCION_PRODUCTOS).document(idProducto).set(producto)
        firestoreInstance.collection(COLECCION_PRODUCTOS).document(idProducto).set(producto)
    }

    fun addProducto(producto: Producto){
        firestoreInstance.collection(COLECCION_PRODUCTOS).document(producto.id).set(producto)
        firestoreInstance.collection(COLECCION_CATEGORIA).document(producto.categoriaId).collection(
            SUBCOLECCION_PRODUCTOS).document(producto.id).set(producto)
        firestoreInstance.collection(COLECCION_USUARIOS).document(uidUser()).collection(SUBCOLECCION_PRODUCTOS).document(producto.id).set(producto)
    }

    fun deleteProducto(producto:Producto){
        firestoreInstance.collection(COLECCION_USUARIOS).document(uidUser()).collection(SUBCOLECCION_PRODUCTOS).document(producto.id).delete()
        firestoreInstance.collection(COLECCION_CATEGORIA).document(producto.categoriaId).collection(SUBCOLECCION_PRODUCTOS).document(producto.id).delete()
        firestoreInstance.collection(COLECCION_PRODUCTOS).document(producto.id).delete()
    }

    fun deleteChats(producto:Producto){
        firestoreInstance.collection(COLECCION_USUARIOS)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userID = document.id
                    checkUsersChats(producto, userID)
                }
            }
    }

    fun checkUsersChats(producto:Producto, userID: String){
        firestoreInstance.collection(COLECCION_USUARIOS).document(userID).collection("Chats")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val chat = document.toObject(Chat::class.java)

                    if (chat.productID == producto.id) {
                        deleteDocument(document.id, userID,"Usuarios")
                        deleteDocument(chat.channelID, userID,"Canales")
                    }
                }
            }
    }

    fun deleteDocument(documentID: String, userID: String, collectionName: String){
        if (collectionName == "Canales"){
            firestoreInstance.collection(collectionName).document(documentID)
                .delete()
                .addOnSuccessListener { Log.d("PRUEBAS", "DocumentSnapshot successfully deleted!") }
        } else {
            firestoreInstance.collection(collectionName).document(userID).collection("Chats").document(documentID)
                .delete()
                .addOnSuccessListener { Log.d("PRUEBAS", "DocumentSnapshot successfully deleted!") }
        }
    }

    fun uidUser():String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun nameUser():String{
        return FirebaseAuth.getInstance().currentUser!!.displayName!!
    }
}