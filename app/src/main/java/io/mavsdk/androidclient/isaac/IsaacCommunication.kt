package io.mavsdk.androidclient.isaac

import android.util.Log

// Fake ISAAC
object IsaacCommunication {
    private const val TAG = "IsaacCommunication"
    private var connected = false
    fun connect() {
        if (connected) {
            return
        }
        connected = true
        Log.i(TAG, "ISAAC connected")
    }

    fun disconnect() {
        if (!connected) {
            return
        }
        connected = false
        Log.i(TAG, "ISAAC disconnected")
    }
}