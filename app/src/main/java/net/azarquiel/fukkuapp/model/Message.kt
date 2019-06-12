package net.azarquiel.fukkuapp.model

import java.util.*

data class Message (val text: String = "",
                    val time: Date = Date(0),
                    val senderID: String = "",
                    val recipientID: String = "",
                    val senderName: String = "",
                    val channelID: String = "",
                    val productID: String = "")