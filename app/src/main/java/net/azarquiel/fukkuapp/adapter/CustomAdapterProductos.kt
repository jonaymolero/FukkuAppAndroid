package net.azarquiel.fukkuapp.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.productosrow.view.*
import net.azarquiel.fukkuapp.model.Producto
import net.azarquiel.fukkuapp.R
import net.azarquiel.fukkuapp.util.FirestoreUtil
import net.azarquiel.fukkuapp.util.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.customView
import org.jetbrains.anko.verticalLayout

class CustomAdapterProductos(val context: Context,
                     val layout: Int,
                     val accion:String?
                    ) : RecyclerView.Adapter<CustomAdapterProductos.ViewHolder>() {

    private var dataList: List<Producto> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val viewlayout = layoutInflater.inflate(layout, parent, false)
        return ViewHolder(viewlayout, context, accion)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    internal fun setProductos(productos: List<Producto>) {
        this.dataList = productos
        notifyDataSetChanged()
    }

    class ViewHolder(viewlayout: View, val context: Context, val accion:String?) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Producto){
            mostrarImagen(itemView, dataItem.imagen)
            itemView.tvNombreProducto.text=dataItem.nombre
            if (dataItem.nombreCategoria != "") {
                itemView.tvCategoriaProducto.text=dataItem.nombreCategoria
                itemView.tvCategoriaProducto.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.0F)
            } else {
                itemView.tvCategoriaProducto.setTextSize(TypedValue.COMPLEX_UNIT_SP, 0.0F)
            }
            itemView.tvFechaProducto.text=dataItem.fecha
            itemView.tvPrecioProducto.text= dataItem.precio + "€"
            itemView.tag=dataItem
            itemView.setOnLongClickListener{ eliminar(dataItem) }
        }

        private fun mostrarImagen(itemView:View,imagen:String){
            if(imagen != ""){
                Glide.with(context).load(imagen).into(itemView.ivImagenProducto)
            }else{
                itemView.ivImagenProducto.setImageResource(R.drawable.notfound)
            }
        }

        private fun eliminar(dataItem: Producto):Boolean{
            if(accion != null){
                if(accion == TUS_PRODUCTOS){
                    context.alert {
                        customView {
                            title = "¿Desea eliminar el producto ${dataItem.nombre}?"
                            verticalLayout {
                                positiveButton("Confirmar") {
                                    FirestoreUtil.deleteProducto(dataItem)
                                }
                                negativeButton("Cancelar") {
                                }
                            }
                        }
                    }.show()
                }else if(accion == TUS_PRODUCTOS_FAVORITOS){
                    context.alert {
                        customView {
                            title = "¿Desea eliminar de favoritos el producto ${dataItem.nombre}?"
                            verticalLayout {
                                positiveButton("Confirmar") {
                                    FirestoreUtil.deleteToProductosFavoritos(dataItem)
                                }
                                negativeButton("Cancelar") {
                                }
                            }
                        }
                    }.show()
                }
            }
            return true
        }
    }
}