package com.wifimap.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete

/**
 * Room Database for persisting WiFi scan data and recommendations.
 */
@Database(entities = [SampleEntity::class, PlacementEntity::class], version = 1)
abstract class WifiDatabase : RoomDatabase() {
    abstract fun sampleDao(): SampleDao
    abstract fun placementDao(): PlacementDao

    companion object {
        @Volatile
        private var INSTANCE: WifiDatabase? = null

        fun getDatabase(context: Context): WifiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WifiDatabase::class.java,
                    "wifi_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

@Entity(tableName = "signal_samples")
data class SampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val floorplanId: String,
    val x: Double,
    val y: Double,
    val rssi: Int,
    val band: String,
    val throughput: Double,
    val frequency: Int,
    val ssid: String,
    val timestamp: Long
)

@Entity(tableName = "placements")
data class PlacementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val floorplanId: String,
    val x: Double,
    val y: Double,
    val height: Double,
    val estimatedCoveragePercent: Double,
    val rssiImprovement: Int,
    val rankingScore: Double,
    val recommendations: String,  // JSON string
    val timestamp: Long
)

@Dao
interface SampleDao {
    @Insert
    suspend fun insert(sample: SampleEntity)

    @Query("SELECT * FROM signal_samples WHERE floorplanId = :floorplanId ORDER BY timestamp DESC")
    suspend fun getSamplesByFloorplan(floorplanId: String): List<SampleEntity>

    @Query("SELECT * FROM signal_samples")
    suspend fun getAllSamples(): List<SampleEntity>

    @Delete
    suspend fun deleteSample(sample: SampleEntity)

    @Query("DELETE FROM signal_samples WHERE floorplanId = :floorplanId")
    suspend fun deleteFloorplanSamples(floorplanId: String)
}

@Dao
interface PlacementDao {
    @Insert
    suspend fun insert(placement: PlacementEntity)

    @Query("SELECT * FROM placements WHERE floorplanId = :floorplanId ORDER BY rankingScore DESC LIMIT 3")
    suspend fun getTopRecommendations(floorplanId: String): List<PlacementEntity>

    @Query("SELECT * FROM placements WHERE floorplanId = :floorplanId")
    suspend fun getPlacementsByFloorplan(floorplanId: String): List<PlacementEntity>

    @Delete
    suspend fun deletePlacement(placement: PlacementEntity)

    @Query("DELETE FROM placements WHERE floorplanId = :floorplanId")
    suspend fun deleteFloorplanPlacements(floorplanId: String)
}
