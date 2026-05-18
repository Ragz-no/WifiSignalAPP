package com.wifimap.models

/**
 * Represents a candidate router placement location with estimated improvements.
 *
 * @param x X coordinate in meters
 * @param y Y coordinate in meters
 * @param height Height above floor in meters (0.0 to 2.5m typical)
 * @param estimatedCoveragePercent Percentage of area with acceptable signal (>-70dBm)
 * @param rssiImprovement Expected RSSI improvement in dBm
 * @param rankingScore Overall placement quality score (0-100)
 * @param recommendations Action items for optimal placement
 */
data class RouterPlacement(
    val x: Double,
    val y: Double,
    val height: Double,
    val estimatedCoveragePercent: Double,
    val rssiImprovement: Int,
    val rankingScore: Double,
    val recommendations: List<String>
)
