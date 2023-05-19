package io.mavsdk.androidclient.preflight_check.manual

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import io.mavsdk.androidclient.R
import io.mavsdk.androidclient.abstract_drone_layer.Drone
import io.mavsdk.androidclient.inspection_setup.InspectionSetupViewModel
import kotlin.math.abs

class VirtualControlFragment : Fragment() {
    private val locationViewModel: InspectionSetupViewModel by activityViewModels()
    private lateinit var drone: LiveData<Drone?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        drone = locationViewModel.droneState
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView: View =
            LayoutInflater.from(context).inflate(R.layout.virual_control_fg, container, false)

        val mScreenJoystickRight: OnScreenJoystick =
            rootView.findViewById(R.id.directionJoystickRight)
        val mScreenJoystickLeft: OnScreenJoystick =
            rootView.findViewById(R.id.directionJoystickLeft)
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

        return rootView
    }

    companion object {
        private const val TAG = "VirtualControlFragment"
    }
}