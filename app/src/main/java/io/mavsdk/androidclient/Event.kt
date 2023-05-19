package io.mavsdk.androidclient

sealed class Event {
    object StopDrone : Event()
}