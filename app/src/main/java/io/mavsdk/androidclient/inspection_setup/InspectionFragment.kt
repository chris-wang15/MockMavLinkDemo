package io.mavsdk.androidclient.inspection_setup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.mavsdk.androidclient.R
import io.mavsdk.androidclient.abstract_drone_layer.Mission

class InspectionFragment : Fragment() {
    companion object {
        private const val TAG = "InspectionFragment"
    }

    private val viewModel: InspectionSetupViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView: View = LayoutInflater.from(context).inflate(
            R.layout.inspection_fg, container, false
        )
        initInspectionScreen(rootView)
        return rootView
    }

    private fun initInspectionScreen(rootView: View) {
        val androidInfoText = rootView.findViewById<TextView>(R.id.android_info_des)
        androidInfoText.text = viewModel.getAndroidDeviceInfo()

        val mavRadioButton = rootView.findViewById<RadioButton>(R.id.radio_mav)
        val mockButton = rootView.findViewById<RadioButton>(R.id.radio_test)
        viewModel.server.observe(viewLifecycleOwner) { server ->
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

        val detectedDevice = rootView.findViewById<TextView>(R.id.detect_drone_info)
        viewModel.detectedDevice.observe(viewLifecycleOwner) {
            detectedDevice.text = it
        }

        rootView.findViewById<Button>(R.id.takeoff_button).let {
            it.setOnClickListener {
                viewModel.droneState.value?.run(Mission.TakeOff) {
                    Log.d(TAG, "takeOff pressed")
                }
            }
        }

        rootView.findViewById<Button>(R.id.land_button).let {
            it.setOnClickListener {
                viewModel.droneState.value?.run(Mission.Land) {
                    Log.d(TAG, "land pressed")
                }
            }
        }

        rootView.findViewById<Button>(R.id.video_button).let {
            it.setOnClickListener {
                viewModel.droneState.value?.run(Mission.StartVideo) {
                    Log.d(TAG, "start video stream pressed")
                }
            }
        }

        rootView.findViewById<Button>(R.id.run_server_button).let {
            it.setOnClickListener {
                viewModel.startServer()
            }
        }

        rootView.findViewById<Button>(R.id.stop_server_button).let {
            it.setOnClickListener {
                viewModel.stopServer()
            }
        }
    }
}