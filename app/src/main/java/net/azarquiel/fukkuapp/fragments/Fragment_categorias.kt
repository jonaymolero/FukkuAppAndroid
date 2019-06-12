package net.azarquiel.fukkuapp.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_fragment_categorias.*
import net.azarquiel.fukkuapp.adapter.CustomAdapterCategorias
import net.azarquiel.fukkuapp.model.Categoria
import net.azarquiel.fukkuapp.R
import net.azarquiel.fukkuapp.util.COLECCION_CATEGORIA
import java.util.ArrayList

class Fragment_categorias : Fragment() {

    private lateinit var arrayCategorias : ArrayList<Categoria>
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter:CustomAdapterCategorias

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        val rootView = inflater.inflate(R.layout.activity_fragment_categorias, container, false)
        return rootView
    }

    //metodo donde se inicializan todas las variables y funciones necesarias
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        crearAdapter()
        cargarCategorias()
    }

    //se crea el adapter de categorias
    private fun crearAdapter(){
        adapter= CustomAdapterCategorias(activity!!.applicationContext, R.layout.categoriasrow, null)
        rvAllCategorias.layoutManager= LinearLayoutManager(activity!!.applicationContext)
        rvAllCategorias.adapter=adapter
    }

    //se cargan todas las categorias y se añaden al array
    private fun cargarCategorias(){
        arrayCategorias= ArrayList()
        db.collection(COLECCION_CATEGORIA)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        arrayCategorias.add(document.toObject(Categoria::class.java))
                    }
                    adapter.setCategorias(arrayCategorias)
                }
            }
    }
}
