package io.mavsdk.androidclient

import io.mavsdk.androidclient.abstract_drone_layer.Drone

sealed class Event {
    data class GetDrone(val onDroneInfo: (Drone?) -> Unit) : Event()
}