package com.wifimap.modeling

import com.wifimap.models.SignalSample
import com.wifimap.models.Heatmap
import kotlin.math.*

/**
 * Propagation model for WiFi signal estimation based on empirical samples.
 * Uses Free Space Path Loss model with site-specific adjustments.
 */
class PropagationModel {

    companion object {
        private const val REFERENCE_POWER_DBM = -40.0  // Power at 1 meter
        private const val PATH_LOSS_EXPONENT = 2.5      // Empirically adjusted for indoor
        private const val WALL_LOSS_DB = 5.0            // Additional loss per wall
    }

    /**
     * Generates a signal heatmap from collected samples.
     * 
     * @param samples List of collected RSSI samples
     * @param floorplanId Reference to floorplan
     * @param gridResolution Resolution in cells per meter
     * @param width Floorplan width in meters
     * @param height Floorplan height in meters
     */
    fun generateHeatmap(
        samples: List<SignalSample>,
        floorplanId: String,
        gridResolution: Double = 2.0,
        width: Double,
        height: Double
    ): Heatmap {
        if (samples.isEmpty()) {
            return Heatmap(null, null, gridResolution, -120, -30, floorplanId)
        }

        val gridWidth = (width * gridResolution).toInt()
        val gridHeight = (height * gridResolution).toInt()
        val rssiGrid = Array(gridWidth) { Array(gridHeight) { -120 } }

        // Interpolate RSSI values for each grid point using IDW (Inverse Distance Weighting)
        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                val realX = x / gridResolution
                val realY = y / gridResolution
                rssiGrid[x][y] = interpolateRSSI(realX, realY, samples)
            }
        }

        val minRssi = samples.minOf { it.rssi }
        val maxRssi = samples.maxOf { it.rssi }

        return Heatmap(
            bitmap = null,  // Bitmap generation can be done separately
            rssiValues = rssiGrid,
            gridResolution = gridResolution,
            minRssi = minRssi,
            maxRssi = maxRssi,
            floorplanId = floorplanId
        )
    }

    /**
     * Estimates RSSI at a specific point using Inverse Distance Weighting (IDW).
     */
    private fun interpolateRSSI(x: Double, y: Double, samples: List<SignalSample>): Int {
        var totalWeight = 0.0
        var weightedSum = 0.0

        for (sample in samples) {
            val distance = sqrt((x - sample.x).pow(2) + (y - sample.y).pow(2))
            
            if (distance < 0.1) {
                // Point is very close to a sample, use its value directly
                return sample.rssi
            }

            val weight = 1.0 / distance.pow(2)
            totalWeight += weight
            weightedSum += sample.rssi * weight
        }

        return if (totalWeight > 0) {
            (weightedSum / totalWeight).toInt().coerceIn(-120, -30)
        } else {
            -100
        }
    }

    /**
     * Estimates signal strength at a point using Free Space Path Loss model.
     * 
     * RSSI = Reference Power - 10 * n * log10(distance) - wall losses
     */
    fun estimateRSSIAtPoint(
        x: Double,
        y: Double,
        routerX: Double,
        routerY: Double,
        routerHeight: Double = 1.5,
        estimatedWalls: Int = 0
    ): Int {
        // Calculate 3D distance including height (assuming measurement height ~1.5m)
        val horizontalDistance = sqrt((x - routerX).pow(2) + (y - routerY).pow(2))
        val verticalDistance = abs(routerHeight - 1.5)
        val distance = sqrt(horizontalDistance.pow(2) + verticalDistance.pow(2))

        if (distance < 0.1) return -30  // Avoid log(0)

        // Calculate path loss
        val pathLoss = 10 * PATH_LOSS_EXPONENT * log10(distance)
        val wallLoss = WALL_LOSS_DB * estimatedWalls

        val rssi = (REFERENCE_POWER_DBM - pathLoss - wallLoss).toInt()
        return rssi.coerceIn(-120, -30)
    }

    /**
     * Calculates coverage percentage for a location.
     * Area with RSSI > -70 dBm is considered acceptable.
     */
    fun calculateCoveragePercentage(heatmap: Heatmap, acceptableThreshold: Int = -70): Double {
        val rssiValues = heatmap.rssiValues ?: return 0.0
        
        var acceptableCount = 0
        var totalCount = 0

        for (row in rssiValues) {
            for (rssi in row) {
                if (rssi > acceptableThreshold) {
                    acceptableCount++
                }
                totalCount++
            }
        }

        return if (totalCount > 0) {
            (acceptableCount.toDouble() / totalCount) * 100
        } else {
            0.0
        }
    }
}
