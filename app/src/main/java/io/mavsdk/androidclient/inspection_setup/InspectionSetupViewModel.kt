package io.mavsdk.androidclient.inspection_setup

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.androidclient.abstract_drone_layer.Drone
import io.mavsdk.androidclient.abstract_drone_layer.DroneServer
import io.mavsdk.androidclient.mavlink.MavLinkServer
import io.mavsdk.androidclient.mock_drone.MockDroneServer

class InspectionSetupViewModel : ViewModel() {
    companion object {
        private const val TAG = "InspectionSetupViewModel"
    }

    private val mavLinkServer by lazy { MavLinkServer() }
    private val mockServer by lazy { MockDroneServer() }

    private val _server: MutableLiveData<DroneServer> = MutableLiveData(mockServer)
    val server: LiveData<DroneServer> = _server
    private var _isServerRunning: MutableLiveData<Boolean> = MutableLiveData(false)
    private val _droneState: MutableLiveData<Drone?> = MutableLiveData(null)
    val droneState: LiveData<Drone?> = _droneState
    private val _detectedDevice: MutableLiveData<String> = MutableLiveData("No device detected")
    val detectedDevice: LiveData<String> = _detectedDevice
    private val _deviceLocation: MutableLiveData<LatLng> = MutableLiveData(
        LatLng(45.71701, 126.64256)
    )
    val deviceLocation: LiveData<LatLng> = _deviceLocation
    private val _visibleState: MutableLiveData<Boolean> = MutableLiveData(true)
    val visibleState: LiveData<Boolean> = _visibleState

    fun startServer() {
        if (_isServerRunning.value != true) {
            _detectedDevice.value = "detecting"
            _server.value!!.startServer(
                {// may not be ui thread
                    Log.d(TAG, "drone detected")
                    _isServerRunning.postValue(true)
                    _droneState.postValue(it)
                    _detectedDevice.postValue(it.getName())
                },
                {
                    Log.d(TAG, "drone location update")
                    _deviceLocation.postValue(it)
                }
            )
        }
    }

    fun stopServer() {
        if (_isServerRunning.value == true) {
            _detectedDevice.value = "server is shutting down"
            _server.value!!.stopServer {
                _droneState.postValue(null)
                _isServerRunning.postValue(false)
                _detectedDevice.postValue("No device detected")
            }
        }
    }

    fun getAndroidDeviceInfo(): String {
        val list = Build.SUPPORTED_ABIS.asList()
        return "Android ${Build.VERSION.SDK_INT}; Hardware $list"
    }

    fun selectServerMode(context: Context, mock: Boolean) {
        if (_isServerRunning.value == true) {
            Toast.makeText(
                context, "Stop previous server before select new", Toast.LENGTH_SHORT
            ).show()
            return
        }
        _server.value = if (mock) mockServer else mavLinkServer
    }

    fun showInspectionScreen() {
        _visibleState.value = true
    }

    fun hidInspectionScreen() {
        _visibleState.value = false
    }
}