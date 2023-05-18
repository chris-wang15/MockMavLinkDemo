package io.mavsdk.androidclient.abstract_drone_layer

import com.mapbox.mapboxsdk.geometry.LatLng

interface DroneServer {
    fun startServer(onComplete: (drone: Drone) -> Unit, onLocationDetermined: (location: LatLng) -> Unit)
    fun stopServer(onComplete: () -> Unit)
}