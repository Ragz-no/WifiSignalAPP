package com.wifimap.floorplan

import com.wifimap.models.Floorplan
import com.wifimap.models.Room
import timber.log.Timber
import kotlin.math.sqrt

/**
 * Handles floorplan import, drawing, and room-by-room setup
 */
class FloorplanManager {

    /**
     * Creates a custom floorplan by drawing
     */
    fun createCustomFloorplan(
        width: Double,
        height: Double,
        corners: List<Pair<Double, Double>>
    ): Floorplan {
        Timber.d("Creating custom floorplan: ${width}x${height}m with ${corners.size} corners")
        return Floorplan(
            width = width,
            height = height,
            corners = corners,
            rooms = emptyList()
        )
    }

    /**
     * Creates a quick floorplan from room-by-room input
     */
    fun createQuickFloorplan(rooms: List<Room>): Floorplan {
        val maxX = rooms.maxOfOrNull { it.x + it.width } ?: 10.0
        val maxY = rooms.maxOfOrNull { it.y + it.height } ?: 10.0
        
        Timber.d("Creating quick floorplan from ${rooms.size} rooms: ${maxX}x${maxY}m")
        
        return Floorplan(
            width = maxX + 2.0, // Add margin
            height = maxY + 2.0,
            rooms = rooms
        )
    }

    /**
     * Imports floorplan from file (image or blueprint)
     * This is a placeholder - actual implementation would use image processing
     */
    fun importFloorplanFromFile(filePath: String): Floorplan {
        Timber.d("Importing floorplan from: $filePath")
        // TODO: Implement image processing to extract floorplan
        // For now, return a default floorplan
        return Floorplan(width = 20.0, height = 15.0)
    }

    /**
     * Calculates distance between two points in floorplan
     */
    fun calculateDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
    }

    /**
     * Checks if a point is within floorplan bounds
     */
    fun isWithinBounds(floorplan: Floorplan, x: Double, y: Double): Boolean {
        return x >= 0 && x <= floorplan.width && y >= 0 && y <= floorplan.height
    }

    /**
     * Validates floorplan structure
     */
    fun validateFloorplan(floorplan: Floorplan): Boolean {
        if (floorplan.width <= 0 || floorplan.height <= 0) {
            Timber.w("Invalid floorplan dimensions")
            return false
        }

        if (floorplan.rooms.isNotEmpty()) {
            for (room in floorplan.rooms) {
                if (room.width <= 0 || room.height <= 0) {
                    Timber.w("Invalid room dimensions: ${room.name}")
                    return false
                }
                if (room.x < 0 || room.y < 0 || room.x + room.width > floorplan.width || room.y + room.height > floorplan.height) {
                    Timber.w("Room outside bounds: ${room.name}")
                    return false
                }
            }
        }

        return true
    }
}
