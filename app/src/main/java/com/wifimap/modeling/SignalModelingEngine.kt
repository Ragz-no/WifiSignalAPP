package com.wifimap.modeling

import com.wifimap.models.*
import timber.log.Timber
import kotlin.math.*

/**
 * Implements signal propagation heuristics and heatmap generation
 */
class SignalModelingEngine {

    companion object {
        private const val FREE_SPACE_LOSS_2_4 = 40.0  // dB at 1m for 2.4 GHz
        private const val FREE_SPACE_LOSS_5 = 46.0    // dB at 1m for 5 GHz
        private const val PATH_LOSS_EXPONENT = 2.5    // Free space: 2.0, Indoor: 2.5-4.0
        private const val WALL_ATTENUATION = 5.0      // dB per wall
        private const val ELEVATION_GAIN = 0.5        // dB per meter elevation
    }

    /**
     * Generates signal heatmap from scan points using propagation model
     */
    fun generateHeatmap(
        scanPoints: List<ScanPoint>,
        floorplan: Floorplan,
        cellSize: Double = 0.5
    ): SignalHeatmap {
        Timber.d("Generating heatmap from ${scanPoints.size} scan points")

        val gridWidth = (floorplan.width / cellSize).toInt()
        val gridHeight = (floorplan.height / cellSize).toInt()
        val rssiGrid = Array(gridHeight) { IntArray(gridWidth) { -100 } }

        // Populate heatmap using interpolation from scan points
        for (y in 0 until gridHeight) {
            for (x in 0 until gridWidth) {
                val worldX = x * cellSize
                val worldY = y * cellSize
                rssiGrid[y][x] = interpolateRssi(worldX, worldY, scanPoints)
            }
        }

        Timber.d("Heatmap generated: ${gridWidth}x${gridHeight} grid")
        return SignalHeatmap(gridWidth, gridHeight, cellSize, rssiGrid)
    }

    /**
     * Interpolates RSSI at a specific location using inverse distance weighting
     */
    private fun interpolateRssi(x: Double, y: Double, scanPoints: List<ScanPoint>): Int {
        if (scanPoints.isEmpty()) return -100

        if (scanPoints.size == 1) {
            return scanPoints[0].rssi
        }

        // IDW interpolation with power of 2
        var totalWeight = 0.0
        var weightedRssi = 0.0

        for (point in scanPoints) {
            val distance = sqrt((x - point.latitude) * (x - point.latitude) + 
                              (y - point.longitude) * (y - point.longitude))
            
            if (distance < 0.01) {
                // Very close to scan point
                return point.rssi
            }

            val weight = 1.0 / (distance * distance)
            totalWeight += weight
            weightedRssi += point.rssi * weight
        }

        return (weightedRssi / totalWeight).toInt().coerceIn(-120, -30)
    }

    /**
     * Calculates expected RSSI at a location with given router parameters
     */
    fun calculateExpectedRssi(
        sourceX: Double,
        sourceY: Double,
        sourceElevation: Double,
        targetX: Double,
        targetY: Double,
        targetElevation: Double,
        band: Band = Band.BAND_2_4GHZ,
        wallCount: Int = 0
    ): Int {
        // Calculate 3D distance
        val horizontalDistance = sqrt((targetX - sourceX) * (targetX - sourceX) + 
                                     (targetY - sourceY) * (targetY - sourceY))
        val verticalDistance = abs(targetElevation - sourceElevation)
        val distance3D = sqrt(horizontalDistance * horizontalDistance + 
                            verticalDistance * verticalDistance)

        // Base loss from distance
        val pathLoss = if (band == Band.BAND_2_4GHZ) {
            FREE_SPACE_LOSS_2_4 + 10 * PATH_LOSS_EXPONENT * log10(distance3D.coerceAtLeast(1.0))
        } else {
            FREE_SPACE_LOSS_5 + 10 * PATH_LOSS_EXPONENT * log10(distance3D.coerceAtLeast(1.0))
        }

        // Apply wall attenuation
        val wallLoss = wallCount * WALL_ATTENUATION

        // Apply elevation gain (assumes 2m is optimal, higher or lower decreases gain)
        val elevationGain = if (sourceElevation >= 1.5 && sourceElevation <= 2.5) {
            ELEVATION_GAIN * 2
        } else if (sourceElevation >= 1.0 && sourceElevation <= 3.0) {
            ELEVATION_GAIN
        } else {
            0.0
        }

        // TX power (typical router: 20 dBm)
        val txPower = 20.0
        val rssi = txPower - pathLoss - wallLoss + elevationGain

        return rssi.toInt().coerceIn(-120, -30)
    }

    /**
     * Ranks candidate locations by signal quality and coverage
     */
    fun rankCandidateLocations(
        candidates: List<Pair<Double, Double>>,
        floorplan: Floorplan,
        heatmap: SignalHeatmap,
        band: Band = Band.BAND_2_4GHZ
    ): List<PlacementRecommendation> {
        Timber.d("Ranking ${candidates.size} candidate locations")

        val recommendations = candidates.mapIndexed { index, (x, y) ->
            val coverage = calculateCoverage(x, y, heatmap)
            val averageSignal = calculateAverageSignal(x, y, heatmap, radius = 5.0)
            val rssiImprovement = averageSignal + 50 // Relative to worst case
            
            PlacementRecommendation(
                rank = index + 1,
                x = x,
                y = y,
                elevationMeter = 2.0, // Default to optimal height
                expectedRssi = averageSignal,
                rssiImprovement = rssiImprovement,
                coverageIncrease = coverage,
                actionItems = generateActionItems(x, y, floorplan),
                score = calculatePlacementScore(x, y, floorplan, heatmap)
            )
        }.sortedByDescending { it.score }

        return recommendations.mapIndexed { index, rec -> rec.copy(rank = index + 1) }
    }

    /**
     * Calculates coverage percentage around a location
     */
    private fun calculateCoverage(x: Double, y: Double, heatmap: SignalHeatmap): Double {
        val cellX = (x / heatmap.cellSize).toInt()
        val cellY = (y / heatmap.cellSize).toInt()
        val radius = (5.0 / heatmap.cellSize).toInt()

        var cellsAboveThreshold = 0
        var totalCells = 0

        for (dy in -radius..radius) {
            for (dx in -radius..radius) {
                val nx = cellX + dx
                val ny = cellY + dy
                
                if (nx >= 0 && nx < heatmap.gridWidth && ny >= 0 && ny < heatmap.gridHeight) {
                    totalCells++
                    if (heatmap.rssiGrid[ny][nx] > -70) { // Threshold for usable signal
                        cellsAboveThreshold++
                    }
                }
            }
        }

        return if (totalCells > 0) (cellsAboveThreshold.toDouble() / totalCells) * 100 else 0.0
    }

    /**
     * Calculates average signal strength in a radius around location
     */
    private fun calculateAverageSignal(
        x: Double,
        y: Double,
        heatmap: SignalHeatmap,
        radius: Double
    ): Int {
        val cellX = (x / heatmap.cellSize).toInt()
        val cellY = (y / heatmap.cellSize).toInt()
        val cellRadius = (radius / heatmap.cellSize).toInt()

        var sum = 0
        var count = 0

        for (dy in -cellRadius..cellRadius) {
            for (dx in -cellRadius..cellRadius) {
                val nx = cellX + dx
                val ny = cellY + dy
                
                if (nx >= 0 && nx < heatmap.gridWidth && ny >= 0 && ny < heatmap.gridHeight) {
                    sum += heatmap.rssiGrid[ny][nx]
                    count++
                }
            }
        }

        return if (count > 0) sum / count else -100
    }

    /**
     * Calculates overall placement score (higher is better)
     */
    private fun calculatePlacementScore(
        x: Double,
        y: Double,
        floorplan: Floorplan,
        heatmap: SignalHeatmap
    ): Double {
        // Prefer central locations
        val distanceToCenter = sqrt((x - floorplan.width / 2) * (x - floorplan.width / 2) +
                                  (y - floorplan.height / 2) * (y - floorplan.height / 2))
        val centerBonus = 100.0 - (distanceToCenter / floorplan.width) * 50

        // Factor in current signal quality
        val coverage = calculateCoverage(x, y, heatmap)
        val signalBonus = coverage

        return centerBonus + signalBonus
    }

    /**
     * Generates action items based on placement location
     */
    private fun generateActionItems(
        x: Double,
        y: Double,
        floorplan: Floorplan
    ): List<String> {
        val actions = mutableListOf<String>()

        // Check if location is central
        val centerX = floorplan.width / 2
        val centerY = floorplan.height / 2
        val distance = sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY))

        if (distance > floorplan.width / 4) {
            actions.add("Move 2-3 meters toward center for better coverage")
        }

        // Recommend elevation
        actions.add("Place router on elevated shelf 1.5-2.5 meters high")
        actions.add("Orient antennas vertically for horizontal coverage")

        return actions
    }
}
