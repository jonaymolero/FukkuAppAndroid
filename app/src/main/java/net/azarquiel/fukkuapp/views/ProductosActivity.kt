package net.azarquiel.fukkuapp.views

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.View
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

import kotlinx.android.synthetic.main.activity_productos.*
import kotlinx.android.synthetic.main.content_productos.*
import net.azarquiel.fukkuapp.adapter.CustomAdapterProductos
import net.azarquiel.fukkuapp.model.Producto
import net.azarquiel.fukkuapp.util.*
import net.azarquiel.fukkuapp.R
import net.azarquiel.fukkuapp.model.Favorito
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast

class ProductosActivity : AppCompatActivity(), SearchView.OnQueryTextListener{

    private lateinit var accion : String
    private lateinit var adapter : CustomAdapterProductos
    private lateinit var db: FirebaseFirestore
    private lateinit var arrayProductos:ArrayList<Producto>
    private lateinit var arrayFavoritos:ArrayList<Favorito>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productos)
        setSupportActionBar(toolbar)

        inicializate()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_productos, menu)
        // ************* <Filtro> ************
        val searchItem = menu.findItem(R.id.search)
        val searchView = searchItem.actionView as SearchView
        searchView.setQueryHint("Search...")
        searchView.setOnQueryTextListener(this)
        // ************* </Filtro> ************
        return true
    }

    override fun onQueryTextSubmit(p0: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(query: String?): Boolean {
        adapter.setProductos(arrayProductos.filter { p -> p.nombre.toLowerCase().contains(query!!.toLowerCase()) })
        return false
    }

    //metodo que inicializa las variables y funciones necesarias
    //dependiendo de la variable accion la activity valdra para listar tus productos o listar tus productos favoritos
    private fun inicializate(){
        db = FirebaseFirestore.getInstance()
        arrayProductos= ArrayList()
        accion=intent.getSerializableExtra("accion") as String

        if(accion == TUS_PRODUCTOS){
            crearAdapter(accion)
            title = resources.getString(R.string.productosNav)
            cargarProductos(FirestoreUtil.uidUser())
            fab.setOnClickListener {
                startActivity(intentFor<AddProductoActivity>())
            }
        }else if(accion == TUS_PRODUCTOS_FAVORITOS){
            crearAdapter(accion)
            title = resources.getString(R.string.productosFavNav)
            cargarIdProductosFavoritos(FirestoreUtil.uidUser())
            fab.hide()
        }
    }

    //se crea el adapter de productos
    private fun crearAdapter(accion:String){
        adapter= CustomAdapterProductos(this,R.layout.productosrow, accion)
        rvProductos.layoutManager= LinearLayoutManager(this)
        rvProductos.adapter=adapter
    }

    //metodo que carga todos los productos en tiempo real de la subcoleccion tus productos
    private fun cargarProductos(id:String){
        db.collection(COLECCION_USUARIOS).document(id).collection(SUBCOLECCION_PRODUCTOS)
            .orderBy(CAMPO_FECHA, Query.Direction.DESCENDING)
            .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e)
                    return@EventListener
                }
                arrayProductos.clear()
                for (document in value!!) {
                    arrayProductos.add(document.toObject(Producto::class.java))
                }
                adapter.setProductos(arrayProductos)
            })
    }

    //metodo que carga todos los id de productos que esten guardados en la subcoleccions tus productos favoritos
    private fun cargarIdProductosFavoritos(id:String){
        arrayFavoritos= ArrayList()
        db.collection(COLECCION_USUARIOS).document(id).collection(SUBCOLECCION_PRODUCTOS_FAVORITOS)
            .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e)
                    return@EventListener
                }
                arrayFavoritos.clear()
                for (document in value!!) {
                    arrayFavoritos.add(document.toObject(Favorito::class.java))
                }
                cargarProductoFavorito(arrayFavoritos)
            })
    }

    //metodo que recorre el array con los ids de los productos y saca el objeto producto
    private fun cargarProductoFavorito(array:ArrayList<Favorito>){
        arrayProductos.clear()
        for(favorito in array){
            db.collection(COLECCION_PRODUCTOS).document(favorito.id).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var document = task.result
                        if(document!!.exists()){
                            arrayProductos.add(document.toObject(Producto::class.java)!!)
                            adapter.setProductos(arrayProductos.sortedBy {it.fecha}.reversed())
                        }
                    }
                }
        }
    }

    //metodo que comprueba si el producto existe y si existe viaja a la activity del producto
    fun pinchaProducto(v: View){
        val producto = v.tag as Producto
        db.collection(COLECCION_PRODUCTOS).document(producto.id).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var document = task.result
                    if(document!!.exists()){
                        var intent= Intent(this, DetailProductActivity::class.java)
                        intent.putExtra("producto", "${document.data!!.getValue(CAMPO_IDPRODUCTO)}")
                        startActivity(intent)
                    }else{
                        toast("Es posible que el producto haya sido borrado")
                    }
                }
            }
    }
}
