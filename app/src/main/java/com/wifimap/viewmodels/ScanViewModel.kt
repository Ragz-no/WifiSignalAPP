package com.wifimap.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wifimap.models.SignalSample
import com.wifimap.wifi.WifiScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for managing WiFi scanning operations.
 */
class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val wifiScanner = WifiScanner(application.applicationContext)

    private val _samples = MutableLiveData<List<SignalSample>>()
    val samples: LiveData<List<SignalSample>> = _samples

    private val _isScanning = MutableLiveData(false)
    val isScanning: LiveData<Boolean> = _isScanning

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val samplesCollection = mutableListOf<SignalSample>()

    /**
     * Collects a WiFi sample at the given coordinates.
     */
    fun collectSample(x: Double, y: Double, ssid: String) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                _isScanning.postValue(true)
                val sample = wifiScanner.collectSample(x, y, ssid)
                
                if (sample != null) {
                    samplesCollection.add(sample)
                    _samples.postValue(samplesCollection.toList())
                } else {
                    _errorMessage.postValue("Could not find network: $ssid")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Scan failed: ${e.message}")
            } finally {
                _isScanning.postValue(false)
            }
        }
    }

    /**
     * Clears all collected samples.
     */
    fun clearSamples() {
        samplesCollection.clear()
        _samples.postValue(emptyList())
    }

    /**
     * Gets the current number of collected samples.
     */
    fun getSampleCount(): Int = samplesCollection.size
}
