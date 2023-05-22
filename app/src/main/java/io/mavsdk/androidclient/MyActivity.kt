package io.mavsdk.androidclient

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.mapbox.mapboxsdk.plugins.annotation.*
import io.mavsdk.androidclient.inspection_setup.InspectionSetupViewModel
import io.mavsdk.androidclient.preflight_check.manual.OnScreenJoystick
import io.mavsdk.androidclient.preflight_check.manual.OnScreenJoystickListener
import io.mavsdk.androidclient.preflight_check.manual.VirtualControlViewModel
import io.mavsdk.androidclient.util.Event
import io.mavsdk.androidclient.util.RxBus
import io.reactivex.disposables.Disposable
import kotlin.math.abs

@SuppressLint("SetTextI18n")
class MyActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MapsActivity"
    }

    private lateinit var appBarConfiguration: AppBarConfiguration

    private val viewModel: InspectionSetupViewModel by viewModels()
    private val manualScreenViewModel: VirtualControlViewModel by viewModels()

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

        initVirtualControlScreen()

        rxBusDisposable?.dispose()
        rxBusDisposable = RxBus.addToObserve(Event::class.java).subscribe { event ->
            when (event) {
                is Event.HideScreen -> manualScreenViewModel.hideScreen()
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
        val controller = findNavController(R.id.nav_host_fragment_content_main)
        when (item.itemId) {
            R.id.to_inspection_screen -> {
                manualScreenViewModel.hideScreen()
                if (controller.currentDestination?.id != R.id.inspection_setup_fragment_nav) {
                    controller.navigate(R.id.inspection_setup_fragment_nav)
                }
            }
            R.id.to_map_fragment -> {
                if (viewModel.droneState.value == null) {
                    Toast.makeText(this, "no drone detected", Toast.LENGTH_SHORT).show()
                } else {
                    manualScreenViewModel.hideScreen()
                    if (controller.currentDestination?.id != R.id.maps_fragment_nav) {
                        controller.navigate(R.id.maps_fragment_nav)
                    }
                }
            }
            R.id.to_manual_control -> {
                if (viewModel.droneState.value == null) {
                    Toast.makeText(this, "no drone detected", Toast.LENGTH_SHORT).show()
                } else {
                    manualScreenViewModel.showScreen()
                }
            }
            R.id.to_gallery -> {
                if (viewModel.droneState.value == null) {
                    Toast.makeText(this, "no drone detected", Toast.LENGTH_SHORT).show()
                } else {
                    manualScreenViewModel.hideScreen()
                    if (controller.currentDestination?.id != R.id.gallery_fg_nav) {
                        controller.navigate(R.id.gallery_fg_nav)
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

    override fun onPause() {
        super.onPause()
        manualScreenViewModel.hideScreen()
    }

    private fun initVirtualControlScreen() {
        val mScreenJoystickRight: OnScreenJoystick = findViewById(R.id.directionJoystickRight)
        val mScreenJoystickLeft: OnScreenJoystick = findViewById(R.id.directionJoystickLeft)
        mScreenJoystickRight.setJoystickListener(object : OnScreenJoystickListener {
            override fun onTouch(joystick: OnScreenJoystick?, pX: Float, pY: Float) {
                val x = if (abs(pX) < 0.02) 0 else pX
                val y = if (abs(pY) < 0.02) 0 else pY
                Log.d(TAG, "onTouch right x: $x, y: $y")
            }
        })

        mScreenJoystickLeft.setJoystickListener(object : OnScreenJoystickListener {
            override fun onTouch(joystick: OnScreenJoystick?, pX: Float, pY: Float) {
                val x = if (abs(pX) < 0.02) 0 else pX
                val y = if (abs(pY) < 0.02) 0 else pY
                Log.d(TAG, "onTouch left x: $x, y: $y")
            }
        })

        val screen = findViewById<View>(R.id.virtual_control_container)
        manualScreenViewModel.visibleState.observe(this) { visible ->
            screen.visibility = if (visible) View.VISIBLE else View.GONE
            mScreenJoystickRight.onVisibilityChanged(visible)
            mScreenJoystickLeft.onVisibilityChanged(visible)
        }
    }
}