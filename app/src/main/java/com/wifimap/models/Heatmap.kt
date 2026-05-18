package com.wifimap.models

import android.graphics.Bitmap

/**
 * Represents a signal strength heatmap for visualization.
 *
 * @param bitmap The heatmap as a bitmap image
 * @param rssiValues 2D array of RSSI values at grid points
 * @param gridResolution Resolution of the grid (cells per meter)
 * @param minRssi Minimum RSSI value in the heatmap
 * @param maxRssi Maximum RSSI value in the heatmap
 * @param floorplanId Reference to the floorplan
 */
data class Heatmap(
    val bitmap: Bitmap?,
    val rssiValues: Array<Array<Int>>?,
    val gridResolution: Double,
    val minRssi: Int,
    val maxRssi: Int,
    val floorplanId: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Heatmap

        if (bitmap != other.bitmap) return false
        if (rssiValues != null) {
            if (other.rssiValues == null) return false
            if (!rssiValues.contentDeepEquals(other.rssiValues)) return false
        } else if (other.rssiValues != null) return false
        if (gridResolution != other.gridResolution) return false
        if (minRssi != other.minRssi) return false
        if (maxRssi != other.maxRssi) return false
        if (floorplanId != other.floorplanId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bitmap?.hashCode() ?: 0
        result = 31 * result + (rssiValues?.contentDeepHashCode() ?: 0)
        result = 31 * result + gridResolution.hashCode()
        result = 31 * result + minRssi
        result = 31 * result + maxRssi
        result = 31 * result + floorplanId.hashCode()
        return result
    }
}
