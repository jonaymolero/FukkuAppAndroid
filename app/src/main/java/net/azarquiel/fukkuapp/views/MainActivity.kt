package net.azarquiel.fukkuapp.views

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import net.azarquiel.fukkuapp.model.Categoria
import net.azarquiel.fukkuapp.model.Producto
import net.azarquiel.fukkuapp.model.ViewPagerAdapter
import net.azarquiel.fukkuapp.R
import net.azarquiel.fukkuapp.fragments.Fragment_categorias
import net.azarquiel.fukkuapp.fragments.Fragment_productos_por_categoria_fav
import net.azarquiel.fukkuapp.fragments.Fragment_productos_por_cercania
import net.azarquiel.fukkuapp.util.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var ivAvatar: CircleImageView
    private lateinit var tvNick: TextView
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            startActivity(intentFor<AddProductoActivity>())
        }

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        val linearProfile = nav_view.getHeaderView(0).LinearProfileMainPage
        linearProfile.setOnClickListener {
            startActivity(intentFor<ProfileActivity>())
        }
        ivAvatar = nav_view.getHeaderView(0).imageUser
        tvNick = nav_view.getHeaderView(0).nick

        showUserInformation()

        nav_view.setNavigationItemSelectedListener(this)
        db = FirebaseFirestore.getInstance()
        setupViewPager(viewPager)
        tabs.setupWithViewPager(viewPager)
    }

    private fun showUserInformation() {
        val userAuth = FirebaseAuth.getInstance().currentUser
        tvNick.text = userAuth!!.displayName

        if(userAuth.photoUrl.toString() != "null"){
            Glide.with(this).load(userAuth.photoUrl).into(ivAvatar)
        }else{
            ivAvatar.setImageResource(R.drawable.user)
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_productos -> {
                productos(TUS_PRODUCTOS)
            }
            R.id.nav_productosFav -> {
                productos(TUS_PRODUCTOS_FAVORITOS)
            }
            R.id.nav_categoriasFav -> {
                startActivity(intentFor<CategoriaActivity>())
            }
            R.id.nav_chat -> {
                startActivity(intentFor<OpenChatsActivity>())
            }
            R.id.nav_cerrarSesion -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(intentFor<LoginActivity>().newTask().clearTask())
            }
            R.id.nav_exit -> {
                finish()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(Fragment_categorias(), "Categorías")
        adapter.addFragment(Fragment_productos_por_cercania(), "Productos por cercania")
        adapter.addFragment(Fragment_productos_por_categoria_fav(), "Productos por categoria de interes")
        viewPager.adapter = adapter
    }

    private fun productos(accion : String){
        var intent= Intent(this, ProductosActivity::class.java)
        intent.putExtra("accion", accion)
        startActivity(intent)
    }

    fun pulsarCategoria(v: View){
        val categoria=v.tag as Categoria
        var intent= Intent(this, Productos_de_un_categoria::class.java)
        intent.putExtra("categoria", categoria)
        startActivity(intent)
    }

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
