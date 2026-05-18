package com.wifimap.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a WiFi scan data point with RSSI, band, and throughput measurements
 */
@Parcelize
data class ScanPoint(
    val latitude: Double,
    val longitude: Double,
    val rssi: Int,
    val band: Band,
    val throughput: Double,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

enum class Band(val frequency: String) {
    BAND_2_4GHZ("2.4 GHz"),
    BAND_5GHZ("5 GHz");
    
    override fun toString(): String = frequency
}

/**
 * Represents a floorplan with dimensions and layout
 */
@Parcelize
data class Floorplan(
    val width: Double,
    val height: Double,
    val rooms: List<Room> = emptyList(),
    val corners: List<Pair<Double, Double>> = emptyList()
) : Parcelable

@Parcelize
data class Room(
    val name: String,
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double
) : Parcelable

/**
 * Represents a signal heatmap with grid-based RSSI values
 */
data class SignalHeatmap(
    val gridWidth: Int,
    val gridHeight: Int,
    val cellSize: Double,
    val rssiGrid: Array<IntArray>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SignalHeatmap

        if (gridWidth != other.gridWidth) return false
        if (gridHeight != other.gridHeight) return false
        if (cellSize != other.cellSize) return false
        if (!rssiGrid.contentDeepEquals(other.rssiGrid)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = gridWidth
        result = 31 * result + gridHeight
        result = 31 * result + cellSize.hashCode()
        result = 31 * result + rssiGrid.contentDeepHashCode()
        return result
    }
}

/**
 * Represents a router placement recommendation
 */
data class PlacementRecommendation(
    val rank: Int,
    val x: Double,
    val y: Double,
    val elevationMeter: Double,
    val expectedRssi: Int,
    val rssiImprovement: Int,
    val coverageIncrease: Double,
    val actionItems: List<String>,
    val score: Double
)

/**
 * Verification result comparing before and after
 */
data class VerificationResult(
    val beforeHeatmap: SignalHeatmap,
    val afterHeatmap: SignalHeatmap,
    val averageRssiImprovement: Int,
    val coverageImprovementPercentage: Double,
    val timestamp: Long = System.currentTimeMillis()
)
