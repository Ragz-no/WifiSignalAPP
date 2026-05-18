package com.wifimap.recommendations

import com.wifimap.models.RouterPlacement
import com.wifimap.models.SignalSample
import com.wifimap.modeling.PropagationModel
import kotlin.math.sqrt

/**
 * Engine for generating router placement recommendations based on WiFi samples.
 */
class RouterRecommendationEngine(private val propagationModel: PropagationModel) {

    /**
     * Generates top 3 router placement recommendations.
     *
     * @param samples Collected WiFi signal samples
     * @param floorplanWidth Width of floorplan in meters
     * @param floorplanHeight Height of floorplan in meters
     * @param routerHeight Current or proposed router height in meters
     */
    fun generateRecommendations(
        samples: List<SignalSample>,
        floorplanWidth: Double,
        floorplanHeight: Double,
        routerHeight: Double = 1.5
    ): List<RouterPlacement> {
        if (samples.isEmpty()) return emptyList()

        val candidates = mutableListOf<RouterPlacement>()

        // Generate candidate positions using a grid approach
        val gridStep = 1.0  // 1 meter grid
        var x = gridStep
        while (x < floorplanWidth) {
            var y = gridStep
            while (y < floorplanHeight) {
                val placement = evaluateLocation(
                    x = x,
                    y = y,
                    height = routerHeight,
                    samples = samples,
                    floorplanWidth = floorplanWidth,
                    floorplanHeight = floorplanHeight
                )
                candidates.add(placement)
                y += gridStep
            }
            x += gridStep
        }

        // Add some elevated placements (on shelves/walls)
        x = gridStep
        while (x < floorplanWidth) {
            var y = gridStep
            while (y < floorplanHeight) {
                val placement = evaluateLocation(
                    x = x,
                    y = y,
                    height = 2.0,  // Higher placement
                    samples = samples,
                    floorplanWidth = floorplanWidth,
                    floorplanHeight = floorplanHeight
                )
                candidates.add(placement)
                y += gridStep
            }
            x += gridStep
        }

        // Sort by ranking score and return top 3
        return candidates.sortedByDescending { it.rankingScore }.take(3)
    }

    /**
     * Evaluates a specific location for router placement.
     */
    private fun evaluateLocation(
        x: Double,
        y: Double,
        height: Double,
        samples: List<SignalSample>,
        floorplanWidth: Double,
        floorplanHeight: Double
    ): RouterPlacement {
        // Calculate average RSSI improvement
        var totalRssiGain = 0
        for (sample in samples) {
            val estimatedRssi = propagationModel.estimateRSSIAtPoint(
                sample.x, sample.y, x, y, height
            )
            totalRssiGain += (estimatedRssi - sample.rssi)
        }
        val avgRssiImprovement = (totalRssiGain / samples.size).coerceIn(-30, 30)

        // Estimate coverage at this location
        val testHeatmap = propagationModel.generateHeatmap(
            samples = samples,
            floorplanId = "test",
            gridResolution = 2.0,
            width = floorplanWidth,
            height = floorplanHeight
        )
        val coveragePercent = propagationModel.calculateCoveragePercentage(testHeatmap)

        // Calculate centrality score (prefer central locations)
        val centerX = floorplanWidth / 2
        val centerY = floorplanHeight / 2
        val distanceFromCenter = sqrt(
            (x - centerX) * (x - centerX) + (y - centerY) * (y - centerY)
        )
        val maxDistance = sqrt(centerX * centerX + centerY * centerY)
        val centralityScore = (1.0 - (distanceFromCenter / maxDistance)) * 25

        // Calculate elevation score (prefer elevated placements)
        val elevationScore = when {
            height >= 2.0 -> 20.0  // High placement
            height >= 1.5 -> 15.0
            else -> 5.0
        }

        // Overall ranking score (0-100)
        val rankingScore = (coveragePercent / 100 * 40) +  // Coverage weight
                (avgRssiImprovement.coerceIn(-30, 30) / 30 * 25) +  // RSSI improvement weight
                centralityScore +
                elevationScore

        // Generate recommendations
        val recommendations = mutableListOf<String>()
        if (height >= 2.0) {
            recommendations.add("Place router on elevated shelf or wall mount")
        } else {
            recommendations.add("Place router at mid-height (1-1.5m)")
        }

        if (distanceFromCenter < maxDistance / 3) {
            recommendations.add("Central location - optimal for coverage")
        } else {
            recommendations.add("Move router towards center of area by ${(distanceFromCenter / 2).toInt()}m")
        }

        if (avgRssiImprovement > 5) {
            recommendations.add("Position offers +${avgRssiImprovement}dBm improvement over current location")
        }

        return RouterPlacement(
            x = x,
            y = y,
            height = height,
            estimatedCoveragePercent = coveragePercent,
            rssiImprovement = avgRssiImprovement,
            rankingScore = rankingScore,
            recommendations = recommendations
        )
    }
}
