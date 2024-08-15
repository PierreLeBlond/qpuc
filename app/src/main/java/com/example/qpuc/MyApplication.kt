package com.example.qpuc

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialiser Firebase
        FirebaseApp.initializeApp(this)
        Log.d("MyApplication", "Firebase initialized")
    }
}
