package net.azarquiel.fukkuapp.model

data class User (val name: String = "",
                 val surnames: String = "",
                 val email: String = "",
                 val uid: String = "",
                 val registrationTokens: MutableList<String> = mutableListOf(),
                 val profilePhoto: String = "",
                 val gender: String = "",
                 val birthday: String = "")