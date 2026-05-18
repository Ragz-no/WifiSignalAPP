package com.wifimap.models

/**
 * Represents a WiFi signal sample collected during a guided walk test.
 * 
 * @param x X coordinate in meters relative to the floorplan
 * @param y Y coordinate in meters relative to the floorplan
 * @param rssi Received Signal Strength Indicator in dBm (-30 to -120)
 * @param band WiFi band (2.4GHz or 5GHz)
 * @param throughput Estimated throughput in Mbps
 * @param frequency WiFi frequency in MHz
 * @param ssid WiFi network SSID
 * @param timestamp Time when sample was collected
 */
data class SignalSample(
    val x: Double,
    val y: Double,
    val rssi: Int,
    val band: WifiBand,
    val throughput: Double,
    val frequency: Int,
    val ssid: String,
    val timestamp: Long
)

enum class WifiBand {
    BAND_2_4GHZ,
    BAND_5GHZ,
    BAND_6GHZ
}
