package io.mavsdk.androidclient.abstract_drone_layer

import com.mapbox.mapboxsdk.geometry.LatLng

interface DroneServer {
    fun getName(): String
    fun startServer(onComplete: (drone: Drone) -> Unit, onLocationUpdate: (location: LatLng) -> Unit)
    fun stopServer(onComplete: () -> Unit)
}