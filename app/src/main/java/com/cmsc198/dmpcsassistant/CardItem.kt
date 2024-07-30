package com.cmsc198.dmpcsassistant

import android.util.Log
import java.time.LocalDateTime

data class CardItem(
    val id: String,
    var lastName: String,
    var firstName: String,
    var middleName: String,
    var suffix: String,
    var location: String
) {

    fun formatFullName(): String {
        return "${lastName.uppercase()}, $firstName ${middleName.firstOrNull()?.toString()?.plus(".") ?: ""}${if (suffix.isNotBlank()) " $suffix" else ""}"
    }
}

