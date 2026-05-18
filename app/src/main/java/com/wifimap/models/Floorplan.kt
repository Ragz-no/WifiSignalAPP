package com.wifimap.models

import android.graphics.Bitmap

/**
 * Represents a floorplan with dimensions and optional room definitions.
 *
 * @param id Unique identifier
 * @param name Name of the floorplan
 * @param bitmap Visual representation of the floorplan
 * @param widthMeters Width of the floorplan in meters
 * @param heightMeters Height of the floorplan in meters
 * @param pixelPerMeter Conversion factor between pixels and meters
 * @param rooms List of rooms in the floorplan
 */
data class Floorplan(
    val id: String,
    val name: String,
    val bitmap: Bitmap?,
    val widthMeters: Double,
    val heightMeters: Double,
    val pixelPerMeter: Double,
    val rooms: List<Room> = emptyList()
)

data class Room(
    val id: String,
    val name: String,
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
    val isImportant: Boolean = false
)
