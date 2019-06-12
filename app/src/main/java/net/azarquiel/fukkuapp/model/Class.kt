package net.azarquiel.fukkuapp.model

import java.io.Serializable

data class Categoria(
    var id : String = "",
    var nombre : String = "",
    var icono : String = ""
):Serializable

data class Producto(
    val id : String = "",
    var nombre : String = "",
    val nombreUsuario : String = "",
    var descripcion : String = "",
    var precio : String = "",
    val fecha : String = "",
    val latitud : String = "",
    val longitud : String = "",
    val categoriaId : String = "",
    var nombreCategoria : String = "",
    val usuarioId : String = "",
    var imagen : String = ""
):Serializable

data class Favorito(
    val id : String = ""
):Serializable