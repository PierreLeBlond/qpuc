package com.example.qpuc.utils

import com.google.firebase.database.FirebaseDatabase

object FirebaseUtils {
    fun getInstance(): FirebaseDatabase {
        return FirebaseDatabase.getInstance("https://qpuc-a2e9d-default-rtdb.europe-west1.firebasedatabase.app/")
    }

    fun generateGameCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }
}
