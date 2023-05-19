package io.mavsdk.androidclient.mavlink

import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.androidclient.abstract_drone_layer.Drone
import io.mavsdk.androidclient.abstract_drone_layer.DroneServer
import io.mavsdk.mavsdkserver.MavsdkServer

class MavLinkServer: DroneServer {
    companion object {
        private const val BACKEND_IP_ADDRESS = "127.0.0.1"
    }
    private val mavsdkServer = MavsdkServer()
    private var drone: MavLinkDrone? = null
    private var ipAddress: String = BACKEND_IP_ADDRESS
    override fun getName() = "MavLinkServer"

    override fun startServer(
        onComplete: (drone: Drone) -> Unit,
        onLocationUpdate: (location: LatLng) -> Unit
    ) {
        drone = MavLinkDrone().apply {
            start(
                mavsdkServer = mavsdkServer,
                ipAddress = ipAddress,
                onComplete = onComplete,
                onLocationUpdate = onLocationUpdate
            )
        }
    }

    override fun stopServer(onComplete: () -> Unit) {
        drone?.stop(mavsdkServer = mavsdkServer, onComplete = onComplete)
    }
}

internal const val TAG = "MavLinkSDKImpl"