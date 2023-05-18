package io.mavsdk.androidclient

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.mapboxsdk.utils.ColorUtils
import io.mavsdk.androidclient.abstract_drone_layer.Drone
import io.mavsdk.androidclient.abstract_drone_layer.DroneServer
import io.mavsdk.androidclient.manul.VirtualControlFragment
import io.mavsdk.androidclient.mavlink.MavLinkServer

@SuppressLint("SetTextI18n")
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private const val TAG = "MapsActivity"
    }

    private val viewModel : MapsViewModel by viewModels()
    private var isMavsdkServerRunning = false
    private lateinit var runStopServerButton: Button
    private var circleManager: CircleManager? = null
    private var symbolManager: SymbolManager? = null

    private lateinit var mapView: MapView
    private var map: MapboxMap? = null

    private val server: DroneServer = MavLinkServer()
    private var drone: Drone? = null

    private var currentPositionMarker: Symbol? = null
    private val waypoints: MutableList<Circle> = ArrayList()
    private val currentPositionObserver =
        Observer { newLatLng: LatLng ->
            updateVehiclePosition(
                newLatLng
            )
        }
    private val currentMissionPlanObserver =
        Observer { latLngList: List<LatLng> ->
            updateMarkers(
                latLngList
            )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, getString(R.string.access_token))

        setContentView(R.layout.activity_maps)
        runStopServerButton = findViewById(R.id.buttonRunDestroyMavsdkServer)
        runStopServerButton.setOnClickListener {
            val fragment = VirtualControlFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.container, fragment, null)
                .addToBackStack("VirtualControlFragment")
                .commit()
            if (drone == null) {
                Toast.makeText(this, "No Drone Detected", Toast.LENGTH_SHORT).show()
            }
            fragment.setDrone(drone)

//            if (isMavsdkServerRunning) {
//                server.stopServer {
//                    runOnUiThread {
//                        drone = null
//                        isMavsdkServerRunning = false
//                        symbolManager?.delete(currentPositionMarker)
//                        currentPositionMarker = null
//                        runStopServerButton.text = "Run Server"
//                    }
//                }
//            } else {
//                server.startServer(
//                    { _drone ->
//                        runOnUiThread {
//                            drone = _drone
//                            isMavsdkServerRunning = true
//                            runStopServerButton.text = "End Server"
//                        }
//                    },
//                    { latLng ->
//                        viewModel.currentPositionLiveData.postValue(latLng)
//                    }
//                )
//            }
        }
        mapView = findViewById<MapView>(R.id.mapView).apply {
            onCreate(savedInstanceState)
            getMapAsync(this@MapsActivity)
        }
    }

    // Update [currentPositionMarker] position with a new [position].
    private fun updateVehiclePosition(newLatLng: LatLng?) {
        if (newLatLng == null || map == null || symbolManager == null) {
            // Not ready
            return
        }

        // Add a vehicle marker and move the camera
        if (currentPositionMarker == null) {
            val symbolOptions = SymbolOptions()
            symbolOptions.withLatLng(newLatLng)
            symbolOptions.withIconImage("marker-icon-id")
            currentPositionMarker = symbolManager!!.create(symbolOptions)
            map!!.moveCamera(CameraUpdateFactory.tiltTo(0.0))
            map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 14.0))
        } else {
            currentPositionMarker?.setLatLng(newLatLng)
            symbolManager?.update(currentPositionMarker)
        }
    }

    // Update the [map] with the current mission plan waypoints.
    private fun updateMarkers(latLngList: List<LatLng>) {
        circleManager?.let {
            it.delete(waypoints)
            waypoints.clear()
            for (latLng in latLngList) {
                val circleOptions = CircleOptions()
                    .withLatLng(latLng)
                    .withCircleColor(ColorUtils.colorToRgbaString(Color.BLUE))
                    .withCircleStrokeColor(ColorUtils.colorToRgbaString(Color.BLACK))
                    .withCircleStrokeWidth(1.0f)
                    .withCircleRadius(12f)
                    .withDraggable(false)
                it.create(circleOptions)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        viewModel.currentPositionLiveData.observe(this, currentPositionObserver)
        viewModel.currentMissionPlanLiveData.observe(this, currentMissionPlanObserver)
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        viewModel.currentPositionLiveData.removeObserver(currentPositionObserver)
        viewModel.currentMissionPlanLiveData.removeObserver(currentMissionPlanObserver)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }


    override fun onMapReady(mapboxMap: MapboxMap) {
        Log.d("Drone", "onMapReady")
        mapboxMap.uiSettings.isRotateGesturesEnabled = false
        mapboxMap.uiSettings.isTiltGesturesEnabled = false
        mapboxMap.addOnMapLongClickListener { point: LatLng ->
            viewModel.addWaypoint(point)
            true
        }

        mapboxMap.setStyle(Style.LIGHT) { style: Style ->
            // Add the marker image to map
            style.addImage(
                "marker-icon-id",
                BitmapFactory.decodeResource(
                    this@MapsActivity.resources,
                    com.mapbox.mapboxsdk.R.drawable.mapbox_marker_icon_default
                )
            )
            symbolManager = SymbolManager(mapView, map!!, style)
            symbolManager?.iconAllowOverlap = true
            circleManager = CircleManager(mapView, map!!, style)
        }

        map = mapboxMap
    }
}