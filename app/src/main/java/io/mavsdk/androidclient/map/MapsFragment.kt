package io.mavsdk.androidclient.map

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.mapboxsdk.utils.ColorUtils
import io.mavsdk.androidclient.R
import io.mavsdk.androidclient.abstract_drone_layer.Mission
import io.mavsdk.androidclient.inspection_setup.InspectionSetupViewModel

class MapsFragment : Fragment(), OnMapReadyCallback {
    companion object {
        private const val TAG = "MapsFragment"
    }

    private val viewModel: MapsViewModel by viewModels()
    private val locationViewModel: InspectionSetupViewModel by activityViewModels()
    private var circleManager: CircleManager? = null
    private var symbolManager: SymbolManager? = null

    private lateinit var mapView: MapView
    private var map: MapboxMap? = null

    private var currentPositionMarker: Symbol? = null
    private val waypoints: MutableList<Circle> = ArrayList()
    private val currentPositionObserver =
        Observer { newLatLng: LatLng ->
            updateVehiclePosition(newLatLng)
        }
    private val currentMissionPlanObserver =
        Observer { latLngList: List<LatLng> ->
            updateMarkers(latLngList)
        }

    private var needLazyInitPosition = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(requireContext(), getString(R.string.access_token))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView: View = LayoutInflater.from(context).inflate(
            R.layout.maps_fg, container, false
        )
        rootView.findViewById<Button>(R.id.takeoff_button).setOnClickListener {
            locationViewModel.droneState.value?.run(Mission.TakeOff) {
                Log.d(TAG, "TakeOff success")
            }
        }
        rootView.findViewById<Button>(R.id.land_button).setOnClickListener {
            locationViewModel.droneState.value?.run(Mission.Land) {
                Log.d(TAG, "Land success")
            }
        }
        rootView.findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            locationViewModel.droneState.value?.let { viewModel.startMission(it) }
        }
        mapView = rootView.findViewById<MapView>(R.id.mapView).apply {
            onCreate(savedInstanceState)
            getMapAsync(this@MapsFragment)
        }

        return rootView
    }

    // Update [currentPositionMarker] position with a new [position].
    private fun updateVehiclePosition(newLatLng: LatLng?) {
        if (newLatLng == null || map == null || symbolManager == null) {
            // Not ready
            needLazyInitPosition = true
            return
        }
        needLazyInitPosition = false
        Log.d(TAG, "updateVehiclePosition $newLatLng")

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
            if (latLngList.isEmpty()) {
                it.deleteAll()
            } else {
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
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        locationViewModel.deviceLocation.observe(this, currentPositionObserver)
        viewModel.currentMissionPlanLiveData.observe(this, currentMissionPlanObserver)
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        locationViewModel.deviceLocation.removeObserver(currentPositionObserver)
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
                    this.resources,
                    com.mapbox.mapboxsdk.R.drawable.mapbox_marker_icon_default
                )
            )
            symbolManager = SymbolManager(mapView, map!!, style)
            symbolManager?.iconAllowOverlap = true
            circleManager = CircleManager(mapView, map!!, style)

            if (needLazyInitPosition) {
                locationViewModel.deviceLocation.value?.let {
                    updateVehiclePosition(it)
                }
            }
        }

        map = mapboxMap
    }
}