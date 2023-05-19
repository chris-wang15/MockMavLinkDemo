package io.mavsdk.androidclient.mock_drone

import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.androidclient.abstract_drone_layer.Drone
import io.mavsdk.androidclient.abstract_drone_layer.DroneServer

class MockDroneServer: DroneServer {
    private var drone: MockDrone? = null

    override fun getName() = "MockDroneServer"

    override fun startServer(
        onComplete: (drone: Drone) -> Unit,
        onLocationUpdate: (location: LatLng) -> Unit
    ) {
        drone = MockDrone().apply {
            start(
                onComplete = onComplete,
                onLocationUpdate = onLocationUpdate
            )
        }
    }

    override fun stopServer(onComplete: () -> Unit) {
        drone?.stop(onComplete = onComplete)
    }
}

internal const val TAG = "MavLinkSDKImpl"