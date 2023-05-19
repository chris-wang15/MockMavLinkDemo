package io.mavsdk.androidclient

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.tools.timezone.util.RxBus
import io.mavsdk.androidclient.abstract_drone_layer.Mission
import io.mavsdk.androidclient.inspection_setup.InspectionSetupViewModel
import io.reactivex.disposables.Disposable

@SuppressLint("SetTextI18n")
class MapsActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MapsActivity"
    }

    private lateinit var appBarConfiguration: AppBarConfiguration

    private val viewModel: InspectionSetupViewModel by viewModels()

    private var rxBusDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mapbox.getInstance(this, getString(R.string.access_token))

        setContentView(R.layout.activity_maps)

        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment_content_main
        ) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        initInspectionScreen()

        rxBusDisposable?.dispose()
        rxBusDisposable = RxBus.addToObserve(Event::class.java).subscribe { event ->
            when (event) {
                is Event.GetDrone -> event.onDroneInfo(viewModel.droneState.value)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_maps, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.to_inspection_screen -> viewModel.showInspectionScreen()
            R.id.to_map_fragment -> {
                if (viewModel.droneState.value == null) {
                    Toast.makeText(this, "no drone detected", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.hidInspectionScreen()
                    val controller = findNavController(R.id.nav_host_fragment_content_main)
                    if (controller.currentDestination?.id != R.id.maps_fragment_nav) {
                        controller.navigate(
                            R.id.action_virtual_control_fragment_nav_to_maps_fragment_nav
                        )
                    }
                }
            }
            R.id.to_manual_control -> {
                if (viewModel.droneState.value == null) {
                    Toast.makeText(this, "no drone detected", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.hidInspectionScreen()
                    val controller = findNavController(R.id.nav_host_fragment_content_main)
                    if (controller.currentDestination?.id != R.id.virtual_control_fragment_nav) {
                        controller.navigate(
                            R.id.action_maps_fragment_to_virtual_control_fragment
                        )
                    }
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        rxBusDisposable?.dispose()
        rxBusDisposable = null
    }

    private fun initInspectionScreen() {
        val androidInfoText = findViewById<TextView>(R.id.android_info_des)
        androidInfoText.text = viewModel.getAndroidDeviceInfo()

        val mavRadioButton = findViewById<RadioButton>(R.id.radio_mav)
        val mockButton = findViewById<RadioButton>(R.id.radio_test)
        viewModel.server.observe(this) { server ->
            if (server.getName() == "MockDroneServer") {
                mockButton.isChecked = true
                mavRadioButton.isChecked = false
            } else {
                mockButton.isChecked = false
                mavRadioButton.isChecked = true
            }
        }
        mavRadioButton.setOnClickListener {
            viewModel.selectServerMode(it.context, false)
        }
        mockButton.setOnClickListener {
            viewModel.selectServerMode(it.context, true)
        }

        val detectedDevice = findViewById<TextView>(R.id.detect_drone_info)
        viewModel.detectedDevice.observe(this) {
            detectedDevice.text = it
        }

        findViewById<Button>(R.id.takeoff_button).let {
            it.setOnClickListener {
                viewModel.droneState.value?.run(Mission.TakeOff) {
                    Log.d(TAG, "takeOff pressed")
                }
            }
        }

        findViewById<Button>(R.id.land_button).let {
            it.setOnClickListener {
                viewModel.droneState.value?.run(Mission.Land) {
                    Log.d(TAG, "land pressed")
                }
            }
        }

        findViewById<Button>(R.id.video_button).let {
            it.setOnClickListener {
                viewModel.droneState.value?.run(Mission.StartVideo) {
                    Log.d(TAG, "start video stream pressed")
                }
            }
        }

        findViewById<Button>(R.id.run_server_button).let {
            it.setOnClickListener {
                viewModel.startServer()
            }
        }

        findViewById<Button>(R.id.stop_server_button).let {
            it.setOnClickListener {
                viewModel.stopServer()
            }
        }

        val inspectionScreen = findViewById<ViewGroup>(R.id.inspection_setup_container)
        viewModel.visibleState.observe(this) { visible ->
            inspectionScreen.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }
}