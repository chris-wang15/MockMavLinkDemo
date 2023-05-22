package io.mavsdk.androidclient.preflight_check.manual

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VirtualControlViewModel : ViewModel() {
    private val _visibleState: MutableLiveData<Boolean> = MutableLiveData(false)
    val visibleState: LiveData<Boolean> = _visibleState

    fun showScreen() {
        _visibleState.value = true
    }

    fun hideScreen() {
        _visibleState.value = false
    }
}