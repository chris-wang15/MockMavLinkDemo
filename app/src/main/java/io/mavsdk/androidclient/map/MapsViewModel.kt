package io.mavsdk.androidclient.map

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.androidclient.abstract_drone_layer.Drone
import io.mavsdk.androidclient.abstract_drone_layer.Mission

class MapsViewModel : ViewModel() {
    private val _currentMissionPlanLiveData: MutableLiveData<MutableList<LatLng>> = MutableLiveData(
        ArrayList()
    )
    val currentMissionPlanLiveData: LiveData<List<LatLng>> = _currentMissionPlanLiveData.map {
        val list: List<LatLng> = it
        list
    }

    override fun onCleared() {
        super.onCleared()
    }

    /**
     * Executes the current mission.
     */
    @SuppressLint("CheckResult")
    fun startMission(drone: Drone) {
        val plan = _currentMissionPlanLiveData.value ?: return
        val latLngs: List<LatLng> = ArrayList(plan)
        val missionPlan = Mission.MissionPlan(
            latLngs = latLngs,
            height = MISSION_HEIGHT,
            speed = MISSION_SPEED,
        )
        drone.run(missionPlan) {
            Log.d(TAG, "mission plan finished")
            _currentMissionPlanLiveData.postValue(ArrayList())
        }
    }

    /**
     * Adds a waypoint to the current mission.
     *
     * @param latLng waypoint to add
     */
    fun addWaypoint(latLng: LatLng) {
        val currentMissionItems = _currentMissionPlanLiveData.value!!
        currentMissionItems.add(latLng)
        _currentMissionPlanLiveData.postValue(currentMissionItems)
    }

    companion object {
        private const val TAG = "MapsViewModel"
        private const val MISSION_HEIGHT = 5.0f
        private const val MISSION_SPEED = 1.0f
    }
}