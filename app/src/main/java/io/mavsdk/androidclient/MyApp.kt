package io.mavsdk.androidclient

import android.app.Application

class MyApp : Application() {
    companion object {
        lateinit var App: Application
    }

    override fun onCreate() {
        super.onCreate()
        App = this
    }
}