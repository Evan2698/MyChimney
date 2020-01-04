package com.example.mychimney

import android.app.Application

class CustomApp :  Application() {
    companion object {
        lateinit var instance: CustomApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }


    var selectedItemId : Int = -1


}