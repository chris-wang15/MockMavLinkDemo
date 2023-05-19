package io.mavsdk.androidclient.mock_drone

import android.util.Log
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.androidclient.abstract_drone_layer.Drone
import io.mavsdk.androidclient.abstract_drone_layer.Mission
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class MockDrone : Drone {
    private val disposables: MutableList<Disposable> = ArrayList()
    private var locationCallback: ((LatLng) -> Unit)? = null

    fun start(
        onComplete: (drone: Drone) -> Unit,
        onLocationUpdate: (location: LatLng) -> Unit
    ) {
        Log.d(TAG, "Start Mock Server")
        locationCallback = onLocationUpdate
        val disposable = Completable.create {
            // mock start server
            Thread.sleep(100)
            it.onComplete()
        }.subscribeOn(Schedulers.io())
            .subscribe {
                Log.d(TAG, "server started ${Thread.currentThread().name}")
                onComplete(this)
                val latLng = LatLng(52.377956, 4.897070)
                onLocationUpdate(latLng)
            }
        disposables.add(disposable)
    }

    fun stop(
        onComplete: () -> Unit
    ) {
        Log.d(TAG, "Destroy Mock Server")
        val disposable = Completable.create {
            // mock stop server
            Thread.sleep(100)
            it.onComplete()
        }.subscribeOn(Schedulers.io())
            .subscribe {
                Log.d(TAG, "stop complete ${Thread.currentThread().name}")
                onComplete()
            }
        disposables.forEach { dp -> dp.dispose() }
        locationCallback = null
    }

    override fun getName() = "MockDrone"

    override fun run(mission: Mission, onFinished: () -> Unit) {
        val disposable: Disposable? = when (mission) {

            is Mission.MissionPlan -> {
                startMissionPlan(
                    latLngs = mission.latLngs,
                    height = mission.height,
                    speed = mission.speed,
                    onFinished = onFinished
                )
            }

            else -> {
                onFinished()
                null
            }
        }

        disposable?.let {
            disposables.add(disposable)
        }
    }

    private fun startMissionPlan(
        latLngs: List<LatLng>,
        height: Float,
        speed: Float,
        onFinished: () -> Unit
    ): Disposable? {
        if (latLngs.isNotEmpty()) {
            val disposable = Observable.fromIterable(latLngs)
                .subscribeOn(Schedulers.io())
                .subscribe(
                    {
                        Thread.sleep(100)
                        Log.d(TAG, "go on ${Thread.currentThread().name}")
                        Log.d(TAG, "go to ${it.latitude}; ${it.longitude}")
                        locationCallback?.invoke(it)
                    },
                    {
                        Log.e(TAG, "startMissionPlan error", it)
                    }
                ) {
                    onFinished()
                }
            disposables.add(disposable)
        }
        return null
    }
}