package io.mavsdk.androidclient.drone_impl.mavlink

import android.util.Log
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.MavsdkEventQueue
import io.mavsdk.System
import io.mavsdk.androidclient.abstract_drone_layer.Drone
import io.mavsdk.androidclient.abstract_drone_layer.Mission
import io.mavsdk.mavsdkserver.MavsdkServer
import io.mavsdk.telemetry.Telemetry
import io.reactivex.disposables.Disposable

class MavLinkDrone : Drone {
    private val disposables: MutableList<Disposable> = ArrayList()
    private var drone: System? = null

    fun start(
        mavsdkServer: MavsdkServer,
        ipAddress: String,
        onComplete: (drone: Drone) -> Unit,
        onLocationUpdate: (location: LatLng) -> Unit
    ) {
        MavsdkEventQueue.executor().execute {
            Log.d(TAG, "startMavsdkServer")
            val mavsdkServerPort: Int = mavsdkServer.run()
            drone = System(ipAddress, mavsdkServerPort)
            disposables.add(
                drone!!.telemetry.flightMode.distinctUntilChanged()
                    .subscribe { flightMode: Telemetry.FlightMode ->
                        Log.d(TAG, "flight mode: $flightMode")
                    })
            disposables.add(
                drone!!.telemetry.armed.distinctUntilChanged()
                    .subscribe { armed: Boolean ->
                        Log.d(TAG, "armed: $armed")
                    })
            disposables.add(drone!!.telemetry.position.subscribe { position: Telemetry.Position ->
                val latLng = LatLng(position.latitudeDeg, position.longitudeDeg)
                // viewModel.currentPositionLiveData.postValue(latLng)
                onLocationUpdate(latLng)
            })
            onComplete(this)
        }
    }

    fun stop(
        mavsdkServer: MavsdkServer,
        onComplete: () -> Unit
    ) {
        MavsdkEventQueue.executor().execute {
            Log.d(TAG, "destroyMavsdkServer")
            for (disposable in disposables) {
                disposable.dispose()
            }
            disposables.clear()
            drone!!.dispose()
            drone = null
            mavsdkServer.stop()
            mavsdkServer.destroy()
            onComplete()
        }
    }

    override fun getName() = "MavLinkDrone"

    override fun run(mission: Mission, onFinished: () -> Unit) {
        drone?.let { drone ->
            val disposable: Disposable? = when (mission) {
                Mission.Arm -> {
                    drone.action.arm().andThen(drone.action.takeoff()).subscribe(
                        {
                            onFinished()
                        },
                        {
                            Log.e(TAG, "mission($mission) error", it)
                        }
                    )
                }
                is Mission.FlyToLocation -> {
                    drone.action.gotoLocation(
                        mission.latLng.latitude,
                        mission.latLng.longitude,
                        mission.absoluteAltitude,
                        mission.yawDeg
                    )?.subscribe(
                        {
                            onFinished()
                        },
                        {
                            Log.e(TAG, "mission($mission) error", it)
                        }
                    )
                }
                Mission.InterruptMissions -> {
                    for (dispose in disposables) {
                        dispose.dispose()
                    }
                    disposables.clear()
                    onFinished()
                    null
                }
                Mission.Kill -> {
                    drone.action.kill().subscribe(
                        {
                            onFinished()
                        },
                        {
                            Log.e(TAG, "mission($mission) error", it)
                        }
                    )
                }
                Mission.Land -> {
                    drone.action.land().subscribe(
                        {
                            onFinished()
                        },
                        {
                            Log.e(TAG, "mission($mission) error", it)
                        }
                    )
                }
                Mission.StartVideo -> {
                    drone.camera.startVideoStreaming().subscribe(
                        {
                            onFinished()
                        },
                        {
                            Log.e(TAG, "mission($mission) error", it)
                        }
                    )
                }
                Mission.ReturnToLaunchPosition -> {
                    drone.action.returnToLaunch()?.subscribe(
                        {
                            onFinished()
                        },
                        {
                            Log.e(TAG, "mission($mission) error", it)
                        }
                    )
                }
                Mission.TakeOff -> {
                    drone.action.arm().andThen(drone.action.takeoff())?.subscribe(
                        {
                            onFinished()
                        },
                        {
                            Log.e(TAG, "mission($mission) error", it)
                        }
                    )
                }

                is Mission.MissionPlan -> {
                    startMissionPlan(
                        _drone = drone,
                        latLngs = mission.latLngs,
                        height = mission.height,
                        speed = mission.speed,
                        onFinished = onFinished
                    )
                }
            }

            disposable?.let {
                disposables.add(disposable)
            }
        }
    }

    private fun startMissionPlan(
        _drone: System,
        latLngs: List<LatLng>,
        height: Float,
        speed: Float,
        onFinished: () -> Unit
    ): Disposable? {
        if (latLngs.isNotEmpty()) {
            val missionItems: MutableList<io.mavsdk.mission.Mission.MissionItem> = ArrayList()
            for (latLng in latLngs) {
                val missionItem = io.mavsdk.mission.Mission.MissionItem(
                    /* latitudeDeg = */ latLng.latitude,
                    /* longitudeDeg = */ latLng.longitude,
                    /* relativeAltitudeM = */ height,
                    /* speedMS = */ speed,
                    /* isFlyThrough = */ true,
                    /* gimbalPitchDeg = */ Float.NaN,
                    /* gimbalYawDeg = */ Float.NaN,
                    /* cameraAction = */ io.mavsdk.mission.Mission.MissionItem.CameraAction.NONE,
                    /* loiterTimeS = */ Float.NaN,
                    /* cameraPhotoIntervalS = */ 1.0,
                    /* acceptanceRadiusM = */ Float.NaN,
                    /* yawDeg = */ Float.NaN,
                    /* cameraPhotoDistanceM = */ Float.NaN
                )
                missionItems.add(missionItem)
            }
            val missionPlan = io.mavsdk.mission.Mission.MissionPlan(missionItems)
            Log.d(TAG, "Uploading and starting mission...")
            return _drone.mission
                .setReturnToLaunchAfterMission(true)
                .andThen(
                    _drone.mission.uploadMission(missionPlan)
                        .doOnComplete {
                            Log.d(TAG, "Upload succeeded")
                        }
                        .doOnError { throwable: Throwable? ->
                            Log.e(TAG, "Failed to upload the mission", throwable)
                        })
                .andThen(
                    _drone.action.arm().onErrorComplete()
                )
                .andThen(
                    _drone.mission.startMission()
                        .doOnComplete {
                            Log.d(TAG, "Mission started")
                        }
                        .doOnError { throwable: Throwable? ->
                            Log.d(TAG, "Failed to start the mission")
                        })
                .subscribe(
                    {
                        onFinished()
                    },
                    {
                        Log.e(TAG, "mission plan error", it)
                    }
                )
        }
        return null
    }
}