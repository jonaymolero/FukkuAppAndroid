package net.azarquiel.fukkuapp.util

import android.Manifest
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import net.azarquiel.fukkuapp.model.Categoria
import org.jetbrains.anko.indeterminateProgressDialog
import java.text.SimpleDateFormat
import java.util.*

object Util {

    private lateinit var p: ProgressDialog

    fun inicia(activity: AppCompatActivity){
        p=activity.indeterminateProgressDialog("Uploading product")
        p.show()
    }

    fun finaliza(){
        p.hide()
    }

    fun activatePermissionCheck(activity: AppCompatActivity){
        var permissionCheck= ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)

        if(permissionCheck == PackageManager.PERMISSION_DENIED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)){

            }else{
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
            }
        }
    }

    fun sacarCategoriaConNombre(nombreCategoria:String, arrayCategorias:ArrayList<Categoria>, arrayNombres:ArrayList<String>): Categoria{
        var categoria = Categoria()
        categoria.nombre =  arrayCategorias.get(arrayNombres.indexOf(nombreCategoria)).nombre
        categoria.id = arrayCategorias.get(arrayNombres.indexOf(nombreCategoria)).id
        categoria.icono = arrayCategorias.get(arrayNombres.indexOf(nombreCategoria)).icono
        return categoria
    }

    fun formatearFecha(formato:String, fecha:Date):String{
        val formatter = SimpleDateFormat(formato)
        return formatter.format(fecha)
    }
}