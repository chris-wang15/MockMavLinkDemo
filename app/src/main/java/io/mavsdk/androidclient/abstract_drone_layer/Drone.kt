package io.mavsdk.androidclient.abstract_drone_layer

import com.mapbox.mapboxsdk.geometry.LatLng

interface Drone {
    fun getName(): String
    fun run(mission: Mission, onFinished: () -> Unit)
}

sealed class Mission {
    object TakeOff : Mission()
    object Land : Mission()
    object Arm : Mission()
    object Kill : Mission()
    object ReturnToLaunchPosition : Mission()
    object StartVideo : Mission()
    object InterruptMissions : Mission()
    data class FlyToLocation(
        val latLng: LatLng, val absoluteAltitude: Float, val yawDeg: Float
    ) : Mission()

    data class MissionPlan(
        val latLngs: List<LatLng>,
        val height: Float,
        val speed: Float,
    ) : Mission()
}