package com.wifimap.wifi

import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.wifimap.models.SignalSample
import com.wifimap.models.WifiBand
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Handles WiFi scanning and RSSI sampling for signal strength measurements.
 */
class WifiScanner(private val context: Context) {

    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    /**
     * Scans for available WiFi networks and returns signal strength.
     * This is a synchronous scan - use with coroutines for non-blocking behavior.
     */
    fun performWifiScan(): List<ScanResult> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            wifiManager.scanResults
        } else {
            wifiManager.scanResults
        }
    }

    /**
     * Collects a single WiFi sample at the given coordinates.
     * 
     * @param x X coordinate in meters
     * @param y Y coordinate in meters
     * @param ssid Target WiFi network SSID
     */
    fun collectSample(x: Double, y: Double, ssid: String): SignalSample? {
        val scanResults = performWifiScan()
        val targetNetwork = scanResults.find { it.SSID == ssid }

        return targetNetwork?.let { network ->
            val band = determineBand(network.frequency)
            val throughput = estimateThroughput(network.level)

            SignalSample(
                x = x,
                y = y,
                rssi = network.level,
                band = band,
                throughput = throughput,
                frequency = network.frequency,
                ssid = ssid,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Determines WiFi band from frequency.
     */
    private fun determineBand(frequency: Int): WifiBand {
        return when {
            frequency in 2400..2500 -> WifiBand.BAND_2_4GHZ
            frequency in 5000..5999 -> WifiBand.BAND_5GHZ
            frequency in 6000..6999 -> WifiBand.BAND_6GHZ
            else -> WifiBand.BAND_2_4GHZ
        }
    }

    /**
     * Estimates throughput based on RSSI signal strength.
     * More accurate measurements would require actual throughput tests.
     *
     * RSSI ranges:
     * -30 dBm: Excellent (150+ Mbps)
     * -50 dBm: Very Good (50-150 Mbps)
     * -70 dBm: Good (10-50 Mbps)
     * -90 dBm: Poor (1-10 Mbps)
     */
    private fun estimateThroughput(rssi: Int): Double {
        return when {
            rssi >= -50 -> 100.0
            rssi >= -70 -> 30.0
            rssi >= -85 -> 5.0
            else -> 1.0
        }
    }

    /**
     * Gets the current connected WiFi network's signal strength.
     */
    fun getCurrentWifiSignal(): Int? {
        return try {
            wifiManager.connectionInfo?.rssi
        } catch (e: Exception) {
            null
        }
    }
}
