package net.azarquiel.fukkuapp.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.robertlevonyan.components.picker.ItemModel
import com.robertlevonyan.components.picker.PickerDialog
import com.robertlevonyan.components.picker.set
import kotlinx.android.synthetic.main.activity_detail_product.*
import kotlinx.android.synthetic.main.content_detail_product.*
import net.azarquiel.fukkuapp.AppConstants
import net.azarquiel.fukkuapp.model.Producto
import net.azarquiel.fukkuapp.R
import net.azarquiel.fukkuapp.util.*
import net.azarquiel.fukkuapp.util.Util
import org.jetbrains.anko.*

class DetailProductActivity : AppCompatActivity() {

    private lateinit var producto : Producto
    private lateinit var db : FirebaseFirestore
    private var isFavorito : Boolean=false
    private var editable = false
    private lateinit var pickerDialog: PickerDialog
    private var uriImagen: Uri?=null
    private lateinit var riversRef: StorageReference
    private var imagenRuta: String?=null
    private lateinit var idProducto:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_product)
        setSupportActionBar(toolbar)

        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        inicializate()
    }

    //metodo donde se incializan las variables necesarias y se llama a los metodos necesarios para comenzar el proceso
    private fun inicializate(){
        db = FirebaseFirestore.getInstance()
        idProducto=intent.getSerializableExtra("producto") as String
        title = resources.getString(R.string.titleDetail)
        getProducto()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_detail_product, menu)
        //checkFavorite(menu)
        checkUser(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_update_product -> editable()
            R.id.action_delete_product -> deleteProducto()
            R.id.action_favorito_product -> addDeleteFavoritos(item)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    //coger el producto en real data time para que se actualice en tiempo real
    private fun getProducto() {
        db.collection(COLECCION_PRODUCTOS).document(idProducto).
            addSnapshotListener(EventListener<DocumentSnapshot> { snapshot, e ->
                if (e != null) {
                    Log.w("PRODUCTO", "Listen failed.", e)
                    return@EventListener
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d("PRODUCTO", "Current data: ${snapshot.data}")
                    producto = snapshot.toObject(Producto::class.java)!!
                    showProduct()
                } else {
                    Log.d("PRODUCTO", "Current data: null")
                }
            })
    }

    //pinta los datos del producto
    private fun showProduct(){
        activarEditText(false)
        mostrarImagen()
        etNombreDetail.set(producto.nombre)
        etPrecioDetail.set(producto.precio)
        etDescripcionDetail.set(producto.descripcion)
        tvSubidoPor.text = "Subido pot ${producto.nombreUsuario} el ${producto.fecha}"
        tvCategoriaDetail.set(producto.nombreCategoria)
        tvCategoriaDetail.isEnabled = false
    }

    //muestra la imagen con la url guardada en el interior del producto
    private fun mostrarImagen(){
        if(producto.imagen != ""){
            Glide.with(this).load(producto.imagen).into(ivProductDetail)
        }else{
            ivProductDetail.setImageResource(R.drawable.notfound)
        }
    }

    //Comrpobacion si el usuario es el propietario del producto
    //Si no lo es se ocultan los botones de actualizar y eliminar, se mete el boton del chat y se pasa a comprobar si esta en favoritos o no
    //Si es el dueño se esconde el boton de añadir a favoritos y el boton del chat
    private fun checkUser(menu: Menu){
        if(producto.usuarioId != FirebaseAuth.getInstance().currentUser!!.uid){
            menu.findItem(R.id.action_delete_product).isVisible = false
            menu.findItem(R.id.action_update_product).isVisible = false
            checkFavorite(menu)
            fab.setOnClickListener {
                FirestoreUtil.getOrCreateChatChannel(producto.usuarioId, producto.id){ channelID ->
                    //toast("Haber que me saca: ${channelID}")
                    val intent = Intent(this, ChatActivity::class.java)
                    intent.putExtra(AppConstants.CHANNEL_ID, channelID)
                    intent.putExtra(AppConstants.OTHER_USER_ID, producto.usuarioId)
                    intent.putExtra(AppConstants.PRODUCT_ID, producto.id)
                    startActivity(intent)
                }
            }

        }else{
            menu.findItem(R.id.action_favorito_product).isVisible = false
            fab.hide()
        }
    }

    //metodo que comprueba si el producto esta en favoritos para poder cambiar el boolean y el boton del menu
    private fun checkFavorite(menu: Menu) {
        db.collection(COLECCION_USUARIOS).document(FirestoreUtil.uidUser()).collection(SUBCOLECCION_PRODUCTOS_FAVORITOS)
            .document(producto.id).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var document = task.result
                    if(document!!.exists()){
                        isFavorito = true
                        menu.findItem(R.id.action_favorito_product).title = resources.getString(R.string.deleteFavortios)
                    }else{
                        isFavorito = false
                        menu.findItem(R.id.action_favorito_product).title = resources.getString(R.string.addFavortios)
                    }
                }
            }
    }

    //metodo que segun una variable boolean va a añadir o eliminar el producto de favoritos cuando el usuario le de al boton del menu
    private fun addDeleteFavoritos(item: MenuItem): Boolean{
        if(!isFavorito){
            FirestoreUtil.addToProductosFavoritos(producto)
            item.title = resources.getString(R.string.deleteFavortios)
        }else{
            FirestoreUtil.deleteToProductosFavoritos(producto)
            item.title = resources.getString(R.string.addFavortios)
        }
        isFavorito = !isFavorito
        return true
    }

    //metodo que llama a funciones que eliminan el producto de firestore
    private fun deleteProducto(){
        alert {
            customView {
                title = "¿Desea eliminar el producto ${producto.nombre}?"
                verticalLayout {
                    positiveButton("Confirmar") {
                        FirestoreUtil.deleteProducto(producto)
                        FirestoreUtil.deleteChats(producto)
                        finish()
                    }
                    negativeButton("Cancelar") {
                    }
                }
            }
        }.show()
    }

    //activa o descativa los edit text cuando el ususario pulsa en actualizar
    private fun editable(){
        if(!editable){
            //enable edit text
            activarEditText(true)
        }else{
            if (!validateForm()) {
                return
            }
            activarEditText(false)
            checkUpdate()
        }
        editable =!editable
    }

    //activa o desactiva los edit text dependiendo de un boolean
    private fun activarEditText(accion:Boolean){
        etNombreDetail.isEnabled = accion
        etPrecioDetail.isEnabled = accion
        etDescripcionDetail.isEnabled = accion
        ivProductDetail.setOnClickListener {
            if (accion){
                picker()
            }
        }
    }

    //compruba si hay imagen seleccionada o no para subir el producto directamente o antes subir la imagen
    private fun checkUpdate(){
        if(uriImagen !=null){
            subirImagen()
        }else{
            updateProduct()
        }
    }

    //metodo que actualiza el producto
    private fun updateProduct(){
        producto.nombre = "${etNombreDetail.text}"
        producto.descripcion = "${etDescripcionDetail.text}"
        producto.precio = "${etPrecioDetail.text}"
        if(imagenRuta !=null){
            producto.imagen = imagenRuta!!
        }
        FirestoreUtil.updateProducto(idProducto, producto)
        uriImagen = null
        imagenRuta = null
    }

    //picker donde seleccionas una imagen y te saca su URI
    private fun picker(){
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
                    uriImagen=uri
                    ivProductDetail.setImageURI(uriImagen)
                }
                ItemModel.ITEM_GALLERY -> {
                    uriImagen=uri
                    ivProductDetail.setImageURI(uriImagen)
                }
            }
        }
        pickerDialog.show(supportFragmentManager, "")
    }

    //metodo que sube la imagen al storage
    private fun subirImagen(){
        Util.inicia(this)
        var storageRef = FirebaseStorage.getInstance().reference
        riversRef = storageRef.child("images").child(uriImagen!!.lastPathSegment)
        var uploadTask = riversRef.putFile(uriImagen!!)
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener {
            toast("Fallo al subir la imagen")
        }.addOnSuccessListener {
            sacarUrlImagen(riversRef.path)
        }
    }

    //metodo que saca la url de la imagen para poder añadirsela al producto
    private fun sacarUrlImagen(path: String) {
        var storageRef = FirebaseStorage.getInstance().reference
        storageRef.child(path).downloadUrl.addOnSuccessListener {
            imagenRuta = it.toString()
            Util.finaliza()
            updateProduct()
        }
    }

    //metodo que compruba los campos que estan vacios para sacar un mensaje de error
    private fun validateForm(): Boolean {
        var valid = true

        if (TextUtils.isEmpty(etNombreDetail.text)) {
            etNombreDetail.error = "Required."
            valid = false
        } else {
            etNombreDetail.error = null
        }

        if (TextUtils.isEmpty(etPrecioDetail.text)) {
            etPrecioDetail.error = "Required."
            valid = false
        } else {
            etPrecioDetail.error = null
        }

        if (TextUtils.isEmpty(etDescripcionDetail.text)) {
            etDescripcionDetail.error = "Required."
            valid = false
        } else {
            etDescripcionDetail.error = null
        }

        return valid
    }
}
