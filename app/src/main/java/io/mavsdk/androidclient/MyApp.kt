package io.mavsdk.androidclient

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco

class MyApp : Application() {
    companion object {
        lateinit var App: Application
    }

    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(this);
        App = this
    }
}