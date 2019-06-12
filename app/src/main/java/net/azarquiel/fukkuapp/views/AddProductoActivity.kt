package net.azarquiel.fukkuapp.views

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.View
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.robertlevonyan.components.picker.ItemModel
import com.robertlevonyan.components.picker.PickerDialog
import kotlinx.android.synthetic.main.activity_add_producto.*
import net.azarquiel.fukkuapp.model.Categoria
import net.azarquiel.fukkuapp.model.Producto
import net.azarquiel.fukkuapp.R
import net.azarquiel.fukkuapp.util.*
import org.jetbrains.anko.toast
import java.util.*

@Suppress("DEPRECATION")
class AddProductoActivity : AppCompatActivity(){

    private lateinit var db: FirebaseFirestore
    private lateinit var arrayStringCategorias:ArrayList<String>
    private lateinit var arrayCategorias:ArrayList<Categoria>
    private var categoriaElegida:String?=null
    private lateinit var pickerDialog: PickerDialog
    private lateinit var imageRef: StorageReference
    private lateinit var storageRef: StorageReference
    private var imagenRuta: String?=null
    private var locationManager : LocationManager? = null
    private var uriImagen: Uri?=null
    private var latitude: Double?=null
    private var longitude: Double?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_producto)
        inicializate()
    }

    //Se inicializan todas las variables necesarias y se inician los metodos
    private fun inicializate(){
        db = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        // Create persistent LocationManager reference
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?

        var permissionCheck=ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        if(permissionCheck == PackageManager.PERMISSION_DENIED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){

            }else{
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
            }
        }

        cargarCategorias()
        iniciarUbicacion()
        ivAddProduct.setOnClickListener { picker() }
        btnSubirProducto.setOnClickListener { validaciones() }
    }

    //Cargar todas las categorias para introducir al spinner
    private fun cargarCategorias(){
        arrayStringCategorias= ArrayList()
        arrayCategorias= ArrayList()
        db.collection(COLECCION_CATEGORIA)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        arrayStringCategorias.add("${document.data.getValue(CAMPO_NOMBRE)}")
                        arrayCategorias.add(document.toObject(Categoria::class.java))
                    }
                    cargarSpinner()
                }
            }
    }

    //cargar spinner con el array de nombres de categorias y metodo que se ejecuta cuando seleccionas un item
    private fun cargarSpinner(){
        spinnerCategorias.adapter=ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayStringCategorias)
        spinnerCategorias.onItemSelectedListener= object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                categoriaElegida=arrayStringCategorias.get(position)
            }
        }
    }

    //iniciar la variable locationManager con el listener
    //Si no has aceptado los permisos de ubicacion te vuelve a saltar el mensaje
    private fun iniciarUbicacion(){
        try {
            // Request location updates
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1L, 0f, locationListener)

        } catch(ex: SecurityException) {
            var permissionCheck=ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            if(permissionCheck == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
            }
        }
    }

    //listener de la ubicacion que coge la longitud y latitud cuando la ubicacion cambia
    //Y pone a null las variables cuando se desactiva la ubicacion
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            latitude = location.latitude
            longitude = location.longitude
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {
            latitude = null
            longitude = null
        }
    }

    //metodo de comprobacion antes de subir el producto
    private fun validaciones(){
        if (!validateForm()) {
            return
        }

        if(latitude !=null && longitude != null) {
            if (uriImagen != null) {
                subirImagen()
            }else{
                addProduct()
            }
        }else{
            toast("Tienes que activar la ubicación para poder añadir un producto")
        }
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
                    ivAddProduct.setImageURI(uriImagen)
                }
                ItemModel.ITEM_GALLERY -> {
                    uriImagen=uri
                    ivAddProduct.setImageURI(uriImagen)
                }
            }
        }
        pickerDialog.show(supportFragmentManager, "")
    }

    //metodo que sube la imagen al storage
    private fun subirImagen(){
        Util.inicia(this)
        imageRef = storageRef.child("images").child(uriImagen!!.lastPathSegment)
        var uploadTask = imageRef.putFile(uriImagen!!)
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener {
            toast("Fallo al subir la imagen")
            Util.finaliza()
        }.addOnSuccessListener {
            sacarUrlImagen(imageRef.path)
        }
    }

    //metodo que saca la url de la imagen para poder añadirsela al producto
    private fun sacarUrlImagen(path: String) {
        storageRef.child(path).downloadUrl.addOnSuccessListener {
            imagenRuta = it.toString()
            Util.finaliza()
            addProduct()
        }
    }

    //metodo que carga el producto con los datos y lo sube a firestore
    private fun addProduct(){
        Util.inicia(this)
        val idDocumento = FirebaseFirestore.getInstance().collection("Productos").document()
        var categoria = Util.sacarCategoriaConNombre(categoriaElegida!!,arrayCategorias,arrayStringCategorias)
        var producto = Producto(
            idDocumento.id,
            "${etNombreProducto.text}",
            FirestoreUtil.nameUser(),
            "${etDescripcionProducto.text}",
            "${etPrecioProducto.text}",
            Util.formatearFecha("dd-MM-yyyy HH:mm",Date()),
            "${latitude}",
            "${longitude}",
            categoria.id,
            categoria.nombre,
            FirestoreUtil.uidUser(),
            if(imagenRuta == null) "" else imagenRuta!!
        )

        FirestoreUtil.addProducto(producto)
        finish()
        Util.finaliza()
    }

    //metodo que compruba los campos que estan vacios para sacar un mensaje de error
    private fun validateForm(): Boolean {
        var valid = true

        if (TextUtils.isEmpty(etNombreProducto.text)) {
            etNombreProducto.error = "Required."
            valid = false
        } else {
            etNombreProducto.error = null
        }

        if (TextUtils.isEmpty(etDescripcionProducto.text)) {
            etDescripcionProducto.error = "Required."
            valid = false
        } else {
            etDescripcionProducto.error = null
        }

        if (TextUtils.isEmpty(etPrecioProducto.text)) {
            etPrecioProducto.error = "Required."
            valid = false
        } else {
            etPrecioProducto.error = null
        }

        return valid
    }
}
