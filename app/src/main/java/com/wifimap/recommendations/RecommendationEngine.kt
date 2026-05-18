package com.wifimap.recommendations

import com.wifimap.models.*
import timber.log.Timber
import kotlin.math.sqrt

/**
 * Generates router placement recommendations based on scan data and modeling
 */
class RecommendationEngine {

    /**
     * Generates top 3 placement recommendations
     */
    fun generateTopRecommendations(
        scanPoints: List<ScanPoint>,
        floorplan: Floorplan,
        heatmap: SignalHeatmap,
        topN: Int = 3
    ): List<PlacementRecommendation> {
        Timber.d("Generating top $topN recommendations from ${scanPoints.size} scan points")

        // Generate candidate locations using grid sampling
        val candidates = generateCandidateLocations(floorplan)

        // Use modeling engine to rank candidates
        val modelingEngine = com.wifimap.modeling.SignalModelingEngine()
        val recommendations = modelingEngine.rankCandidateLocations(
            candidates,
            floorplan,
            heatmap,
            determineOptimalBand(scanPoints)
        )

        return recommendations.take(topN)
    }

    /**
     * Generates candidate locations on a grid
     */
    private fun generateCandidateLocations(floorplan: Floorplan): List<Pair<Double, Double>> {
        val candidates = mutableListOf<Pair<Double, Double>>()
        val spacing = 1.0 // 1 meter grid spacing

        var y = 1.0
        while (y < floorplan.height - 1.0) {
            var x = 1.0
            while (x < floorplan.width - 1.0) {
                candidates.add(Pair(x, y))
                x += spacing
            }
            y += spacing
        }

        Timber.d("Generated ${candidates.size} candidate locations")
        return candidates
    }

    /**
     * Determines optimal band from scan data
     */
    private fun determineOptimalBand(scanPoints: List<ScanPoint>): Band {
        val band2_4Count = scanPoints.count { it.band == Band.BAND_2_4GHZ }
        val band5Count = scanPoints.count { it.band == Band.BAND_5GHZ }
        
        return if (band5Count > band2_4Count) Band.BAND_5GHZ else Band.BAND_2_4GHZ
    }

    /**
     * Calculates RSSI improvement expected from recommendation
     */
    fun calculateRssiImprovement(
        recommendation: PlacementRecommendation,
        baselineRssi: Int
    ): Int {
        return recommendation.expectedRssi - baselineRssi
    }

    /**
     * Calculates coverage improvement percentage
     */
    fun calculateCoverageImprovement(
        beforeCoverage: Double,
        afterCoverage: Double
    ): Double {
        return afterCoverage - beforeCoverage
    }

    /**
     * Generates brief, actionable recommendations
     */
    fun generateActionSummary(recommendations: List<PlacementRecommendation>): Map<Int, List<String>> {
        return recommendations.associate { rec ->
            rec.rank to rec.actionItems
        }
    }

    /**
     * Evaluates recommendation feasibility
     */
    fun evaluateFeasibility(
        recommendation: PlacementRecommendation,
        floorplan: Floorplan,
        obstacles: List<Pair<Double, Double>> = emptyList()
    ): Boolean {
        // Check if within bounds
        if (recommendation.x < 0 || recommendation.x > floorplan.width ||
            recommendation.y < 0 || recommendation.y > floorplan.height) {
            return false
        }

        // Check distance to obstacles
        for (obstacle in obstacles) {
            val distance = sqrt(
                (recommendation.x - obstacle.first) * (recommendation.x - obstacle.first) +
                (recommendation.y - obstacle.second) * (recommendation.y - obstacle.second)
            )
            if (distance < 0.5) { // At least 50cm away from obstacles
                return false
            }
        }

        return true
    }
}
