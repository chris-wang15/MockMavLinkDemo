package io.mavsdk.androidclient

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.androidclient.abstract_drone_layer.Drone
import io.mavsdk.androidclient.abstract_drone_layer.Mission

class MapsViewModel : ViewModel() {
    val currentPositionLiveData: MutableLiveData<LatLng> = MutableLiveData(
        LatLng(45.71701, 126.64256)
    )
    val currentMissionPlanLiveData: MutableLiveData<MutableList<LatLng>> = MutableLiveData(
        ArrayList()
    )

    override fun onCleared() {
        super.onCleared()
    }

    /**
     * Executes the current mission.
     */
    @SuppressLint("CheckResult")
    fun startMission(drone: Drone) {
        val plan = currentMissionPlanLiveData.value ?: return
        val latLngs: List<LatLng> = ArrayList(plan)
        val missionPlan = Mission.MissionPlan(
            latLngs = latLngs,
            height = MISSION_HEIGHT,
            speed = MISSION_SPEED,
        )
        drone.run(missionPlan) { Log.d(TAG, "mission plan finished") }
    }

    /**
     * Adds a waypoint to the current mission.
     *
     * @param latLng waypoint to add
     */
    fun addWaypoint(latLng: LatLng) {
        val currentMissionItems = currentMissionPlanLiveData.value!!
        currentMissionItems.add(latLng)
        currentMissionPlanLiveData.postValue(currentMissionItems)
    }

    companion object {
        private const val TAG = "MapsViewModel"
        private const val MISSION_HEIGHT = 5.0f
        private const val MISSION_SPEED = 1.0f
    }
}