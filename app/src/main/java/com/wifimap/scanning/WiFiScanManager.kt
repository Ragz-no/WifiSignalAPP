package com.wifimap.scanning

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.wifimap.models.ScanPoint
import com.wifimap.models.Band
import timber.log.Timber

/**
 * Handles WiFi scanning, RSSI measurement, and band detection
 */
class WiFiScanManager(private val context: Context) {

    private val wifiManager: WifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    /**
     * Performs a WiFi scan at a specific location
     */
    suspend fun scanAtLocation(
        latitude: Double,
        longitude: Double,
        band: Band? = null
    ): ScanPoint = withContext(Dispatchers.Default) {
        try {
            val startScan = System.currentTimeMillis()
            wifiManager.startScan()
            
            // In production, use ScanResultsCallback for async results
            // For now, simulate scan delay
            Thread.sleep(2000)

            val results = wifiManager.scanResults
            
            // Filter by band if specified
            val filteredResults = if (band != null) {
                results.filter { result ->
                    when (band) {
                        Band.BAND_2_4GHZ -> result.frequency in 2400..2500
                        Band.BAND_5GHZ -> result.frequency in 5000..6000
                    }
                }
            } else {
                results
            }

            // Calculate average RSSI from all detected networks
            val averageRssi = if (filteredResults.isNotEmpty()) {
                filteredResults.map { it.level }.average().toInt()
            } else {
                -100 // Poor signal
            }

            // Detect band
            val detectedBand = if (filteredResults.isNotEmpty()) {
                val band2_4GHz = filteredResults.count { it.frequency in 2400..2500 }
                val band5GHz = filteredResults.count { it.frequency in 5000..6000 }
                if (band5GHz > band2_4GHz) Band.BAND_5GHZ else Band.BAND_2_4GHZ
            } else {
                Band.BAND_2_4GHZ
            }

            // Simulate throughput measurement (in production, use actual iperf3 or similar)
            val throughput = calculateThroughput(averageRssi)

            Timber.d("Scan complete at ($latitude, $longitude): RSSI=$averageRssi, Band=$detectedBand, Throughput=$throughput Mbps")

            ScanPoint(
                latitude = latitude,
                longitude = longitude,
                rssi = averageRssi,
                band = detectedBand,
                throughput = throughput,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Timber.e(e, "WiFi scan failed at ($latitude, $longitude)")
            throw e
        }
    }

    /**
     * Performs a guided walk test, collecting multiple scan points
     */
    suspend fun guidedWalkScan(
        locations: List<Pair<Double, Double>>
    ): List<ScanPoint> = withContext(Dispatchers.Default) {
        val scanResults = mutableListOf<ScanPoint>()
        
        for ((index, location) in locations.withIndex()) {
            try {
                Timber.d("Scan point ${index + 1}/${locations.size}")
                val point = scanAtLocation(location.first, location.second)
                scanResults.add(point)
                
                // Brief delay between scans
                Thread.sleep(500)
            } catch (e: Exception) {
                Timber.e(e, "Failed to scan at location ${index + 1}")
            }
        }
        
        Timber.d("Guided walk complete: ${scanResults.size}/${locations.size} points collected")
        scanResults
    }

    /**
     * Estimates throughput based on RSSI using empirical model
     * RSSI ranges from -30 (excellent) to -120 (poor)
     */
    private fun calculateThroughput(rssi: Int): Double {
        return when {
            rssi > -50 -> 100.0 + (rssi + 30) * 1.0  // Excellent
            rssi > -60 -> 80.0 + (rssi + 50) * 1.0   // Very Good
            rssi > -70 -> 50.0 + (rssi + 60) * 1.0   // Good
            rssi > -80 -> 20.0 + (rssi + 70) * 1.0   // Fair
            else -> maxOf(1.0, (rssi + 100) * 0.5)    // Poor
        }
    }

    /**
     * Gets current WiFi signal strength without full scan
     */
    fun getCurrentRssi(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            wifiManager.connectionInfo?.rssi ?: -100
        } else {
            @Suppress("DEPRECATION")
            wifiManager.connectionInfo?.rssi ?: -100
        }
    }
}
