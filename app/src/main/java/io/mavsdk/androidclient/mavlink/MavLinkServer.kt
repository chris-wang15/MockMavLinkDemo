package io.mavsdk.androidclient.mavlink

import android.util.Log
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.MavsdkEventQueue
import io.mavsdk.System
import io.mavsdk.androidclient.abstract_drone_layer.Drone
import io.mavsdk.androidclient.abstract_drone_layer.DroneServer
import io.mavsdk.mavsdkserver.MavsdkServer
import io.mavsdk.telemetry.Telemetry
import io.reactivex.disposables.Disposable

class MavLinkServer: DroneServer {
    companion object {
        private const val BACKEND_IP_ADDRESS = "127.0.0.1"
    }
    private val mavsdkServer = MavsdkServer()
    private var drone: MavLinkDrone? = null
    private var ipAddress: String = BACKEND_IP_ADDRESS

    override fun startServer(
        onComplete: (drone: Drone) -> Unit,
        onLocationDetermined: (location: LatLng) -> Unit
    ) {
        drone = MavLinkDrone().apply {
            start(
                mavsdkServer = mavsdkServer,
                ipAddress = ipAddress,
                onComplete = onComplete,
                onLocationDetermined = onLocationDetermined
            )
        }
    }

    override fun stopServer(onComplete: () -> Unit) {
        drone?.stop(mavsdkServer = mavsdkServer, onComplete = onComplete)
    }
}

internal const val TAG = "MavLinkSDKImpl"