# WiFi Signal Mapping App - API Reference

## Core Data Models

### ScanPoint
WiFi measurement at a specific location.

```kotlin
data class ScanPoint(
    val latitude: Double,          // X coordinate in floorplan
    val longitude: Double,         // Y coordinate in floorplan
    val rssi: Int,                 // Signal strength in dBm (-30 to -120)
    val band: Band,                // WiFi band (2.4 GHz or 5 GHz)
    val throughput: Double,        // Estimated throughput in Mbps
    val timestamp: Long = System.currentTimeMillis()
)
```

### Band
Enumeration for WiFi frequency bands.

```kotlin
enum class Band(val frequency: String) {
    BAND_2_4GHZ("2.4 GHz"),  // 2400-2500 MHz
    BAND_5GHZ("5 GHz")       // 5000-6000 MHz
}
```

### Floorplan
Represents a building floorplan with rooms and dimensions.

```kotlin
data class Floorplan(
    val width: Double,                              // Width in meters
    val height: Double,                             // Height in meters
    val rooms: List<Room> = emptyList(),            // List of rooms
    val corners: List<Pair<Double, Double>> = emptyList()  // Custom corners
)
```

### Room
Individual room in a floorplan.

```kotlin
data class Room(
    val name: String,          // Room name (e.g., "Living Room")
    val x: Double,             // X position in meters
    val y: Double,             // Y position in meters
    val width: Double,         // Width in meters
    val height: Double         // Height in meters
)
```

### SignalHeatmap
Grid-based signal strength interpolation.

```kotlin
data class SignalHeatmap(
    val gridWidth: Int,        // Number of cells horizontally
    val gridHeight: Int,       // Number of cells vertically
    val cellSize: Double,      // Size of each cell in meters (e.g., 0.5m)
    val rssiGrid: Array<IntArray>  // 2D array of RSSI values
)
```

### PlacementRecommendation
Router placement suggestion with expected improvements.

```kotlin
data class PlacementRecommendation(
    val rank: Int,                          // Ranking 1-3
    val x: Double,                          // X position in meters
    val y: Double,                          // Y position in meters
    val elevationMeter: Double,             // Recommended height (typically 2.0m)
    val expectedRssi: Int,                  // Expected RSSI at this location
    val rssiImprovement: Int,               // Improvement vs current
    val coverageIncrease: Double,           // Coverage improvement percentage
    val actionItems: List<String>,          // Actionable steps
    val score: Double                       // Placement quality score
)
```

### VerificationResult
Before/after comparison after implementing recommendation.

```kotlin
data class VerificationResult(
    val beforeHeatmap: SignalHeatmap,       // Original heatmap
    val afterHeatmap: SignalHeatmap,        // New heatmap after move
    val averageRssiImprovement: Int,        // Average RSSI gain in dBm
    val coverageImprovementPercentage: Double,  // Coverage increase %
    val timestamp: Long = System.currentTimeMillis()
)
```

---

## WiFiScanManager

Handles all WiFi scanning operations.

### Constructor
```kotlin
constructor(context: Context)
```

### Methods

#### scanAtLocation()
Performs a WiFi scan at a specific location.

```kotlin
suspend fun scanAtLocation(
    latitude: Double,
    longitude: Double,
    band: Band? = null
): ScanPoint
```

**Parameters:**
- `latitude` - X coordinate in floorplan (meters)
- `longitude` - Y coordinate in floorplan (meters)
- `band` - Optional: filter by specific band (null = all bands)

**Returns:** `ScanPoint` with RSSI, detected band, and throughput

**Example:**
```kotlin
val scanPoint = wifiScanManager.scanAtLocation(5.0, 3.5, Band.BAND_2_4GHZ)
println("RSSI: ${scanPoint.rssi} dBm")
println("Throughput: ${scanPoint.throughput} Mbps")
```

#### guidedWalkScan()
Performs multiple scans at sequential locations.

```kotlin
suspend fun guidedWalkScan(
    locations: List<Pair<Double, Double>>
): List<ScanPoint>
```

**Parameters:**
- `locations` - List of (X, Y) coordinates to scan

**Returns:** List of `ScanPoint` results

**Example:**
```kotlin
val locations = listOf(
    Pair(1.0, 1.0),
    Pair(5.0, 1.0),
    Pair(5.0, 5.0),
    Pair(1.0, 5.0)
)
val results = wifiScanManager.guidedWalkScan(locations)
```

#### getCurrentRssi()
Gets current WiFi signal strength without full scan.

```kotlin
fun getCurrentRssi(): Int
```

**Returns:** Current RSSI in dBm

---

## FloorplanManager

Handles floorplan creation and management.

### Methods

#### createCustomFloorplan()
Creates a floorplan from custom corner coordinates.

```kotlin
fun createCustomFloorplan(
    width: Double,
    height: Double,
    corners: List<Pair<Double, Double>>
): Floorplan
```

**Example:**
```kotlin
val corners = listOf(
    Pair(0.0, 0.0),
    Pair(10.0, 0.0),
    Pair(10.0, 8.0),
    Pair(0.0, 8.0)
)
val floorplan = FloorplanManager().createCustomFloorplan(10.0, 8.0, corners)
```

#### createQuickFloorplan()
Creates a floorplan from room definitions.

```kotlin
fun createQuickFloorplan(rooms: List<Room>): Floorplan
```

**Example:**
```kotlin
val rooms = listOf(
    Room("Living Room", 0.0, 0.0, 5.0, 4.0),
    Room("Kitchen", 5.0, 0.0, 3.0, 4.0),
    Room("Bedroom", 0.0, 4.0, 4.0, 4.0)
)
val floorplan = FloorplanManager().createQuickFloorplan(rooms)
```

#### importFloorplanFromFile()
Imports floorplan from file.

```kotlin
fun importFloorplanFromFile(filePath: String): Floorplan
```

**Note:** Requires image processing implementation

#### isWithinBounds()
Checks if coordinates are within floorplan.

```kotlin
fun isWithinBounds(floorplan: Floorplan, x: Double, y: Double): Boolean
```

#### validateFloorplan()
Validates floorplan structure.

```kotlin
fun validateFloorplan(floorplan: Floorplan): Boolean
```

---

## SignalModelingEngine

Advanced signal propagation and heatmap generation.

### Methods

#### generateHeatmap()
Creates interpolated heatmap from scan points.

```kotlin
fun generateHeatmap(
    scanPoints: List<ScanPoint>,
    floorplan: Floorplan,
    cellSize: Double = 0.5
): SignalHeatmap
```

**Parameters:**
- `scanPoints` - List of WiFi measurements
- `floorplan` - Floorplan layout
- `cellSize` - Grid cell size in meters (default 0.5m)

**Returns:** `SignalHeatmap` with interpolated RSSI grid

**Example:**
```kotlin
val heatmap = engine.generateHeatmap(
    scanPoints,
    floorplan,
    cellSize = 0.5  // 50cm resolution
)
println("Grid: ${heatmap.gridWidth}x${heatmap.gridHeight}")
```

#### calculateExpectedRssi()
Calculates expected RSSI at a location.

```kotlin
fun calculateExpectedRssi(
    sourceX: Double,
    sourceY: Double,
    sourceElevation: Double,
    targetX: Double,
    targetY: Double,
    targetElevation: Double,
    band: Band = Band.BAND_2_4GHZ,
    wallCount: Int = 0
): Int
```

**Parameters:**
- `source*` - Router position and elevation
- `target*` - Target measurement point
- `band` - WiFi band
- `wallCount` - Number of walls between router and target

**Returns:** Expected RSSI in dBm

**Example:**
```kotlin
val expectedRssi = engine.calculateExpectedRssi(
    sourceX = 5.0, sourceY = 2.5, sourceElevation = 2.0,
    targetX = 1.0, targetY = 1.0, targetElevation = 1.5,
    band = Band.BAND_2_4GHZ,
    wallCount = 1
)
println("Expected: $expectedRssi dBm")
```

#### rankCandidateLocations()
Ranks placement candidates.

```kotlin
fun rankCandidateLocations(
    candidates: List<Pair<Double, Double>>,
    floorplan: Floorplan,
    heatmap: SignalHeatmap,
    band: Band = Band.BAND_2_4GHZ
): List<PlacementRecommendation>
```

**Returns:** Sorted list of recommendations

---

## RecommendationEngine

Generates actionable router placement recommendations.

### Methods

#### generateTopRecommendations()
Generates top 3 placement spots.

```kotlin
fun generateTopRecommendations(
    scanPoints: List<ScanPoint>,
    floorplan: Floorplan,
    heatmap: SignalHeatmap,
    topN: Int = 3
): List<PlacementRecommendation>
```

**Returns:** Top N recommendations ranked by score

**Example:**
```kotlin
val recommendations = engine.generateTopRecommendations(
    scanPoints, floorplan, heatmap, topN = 3
)
recommendations.forEach { rec ->
    println("Rank ${rec.rank}: (${rec.x}, ${rec.y})")
    println("RSSI: ${rec.expectedRssi} dBm")
    println("Coverage: ${rec.coverageIncrease}%")
    rec.actionItems.forEach { println("  • $it") }
}
```

#### calculateRssiImprovement()
Calculates expected RSSI improvement.

```kotlin
fun calculateRssiImprovement(
    recommendation: PlacementRecommendation,
    baselineRssi: Int
): Int
```

#### calculateCoverageImprovement()
Calculates coverage percentage increase.

```kotlin
fun calculateCoverageImprovement(
    beforeCoverage: Double,
    afterCoverage: Double
): Double
```

#### evaluateFeasibility()
Checks if recommendation is feasible.

```kotlin
fun evaluateFeasibility(
    recommendation: PlacementRecommendation,
    floorplan: Floorplan,
    obstacles: List<Pair<Double, Double>> = emptyList()
): Boolean
```

---

## Complete Workflow Example

```kotlin
// Initialize managers
val wifiScanManager = WiFiScanManager(context)
val floorplanManager = FloorplanManager()
val modelingEngine = SignalModelingEngine()
val recommendationEngine = RecommendationEngine()

// Step 1: Create floorplan
val rooms = listOf(
    Room("Living Room", 0.0, 0.0, 6.0, 5.0),
    Room("Kitchen", 6.0, 0.0, 4.0, 5.0)
)
val floorplan = floorplanManager.createQuickFloorplan(rooms)

// Step 2: Perform guided walk scan
val scanLocations = listOf(
    Pair(1.0, 1.0), Pair(5.0, 1.0), Pair(8.0, 1.0),
    Pair(1.0, 2.5), Pair(5.0, 2.5), Pair(8.0, 2.5),
    Pair(1.0, 4.0), Pair(5.0, 4.0), Pair(8.0, 4.0)
)
val scanPoints = wifiScanManager.guidedWalkScan(scanLocations)

// Step 3: Generate heatmap
val heatmap = modelingEngine.generateHeatmap(scanPoints, floorplan, 0.5)

// Step 4: Get recommendations
val recommendations = recommendationEngine.generateTopRecommendations(
    scanPoints, floorplan, heatmap, topN = 3
)

// Step 5: Display results
recommendations.forEach { rec ->
    println("Place ${rec.rank}: (${String.format("%.1f", rec.x)}, ${String.format("%.1f", rec.y)})")
    println("  Expected RSSI: ${rec.expectedRssi} dBm")
    println("  Coverage: ${String.format("%.1f", rec.coverageIncrease)}%")
    println("  Actions:")
    rec.actionItems.forEach { println("    • $it") }
}

// Step 6: Verification (after moving router)
val verificationScanPoints = wifiScanManager.guidedWalkScan(scanLocations)
val verificationHeatmap = modelingEngine.generateHeatmap(
    verificationScanPoints, floorplan, 0.5
)
```

---

## Signal Strength Reference

| RSSI | Quality | Expected Throughput |
|------|---------|-------------------|
| -30 to -50 | Excellent | 100+ Mbps |
| -50 to -60 | Very Good | 80+ Mbps |
| -60 to -70 | Good | 50+ Mbps |
| -70 to -80 | Fair | 20+ Mbps |
| -80 to -100 | Poor | 1-20 Mbps |
| Below -100 | Unusable | <1 Mbps |

## Propagation Model Constants

```kotlin
// 2.4 GHz
FREE_SPACE_LOSS_2_4 = 40.0 dB    // At 1 meter

// 5 GHz
FREE_SPACE_LOSS_5 = 46.0 dB      // At 1 meter

// Path loss exponent (indoor environment)
PATH_LOSS_EXPONENT = 2.5

// Wall attenuation
WALL_ATTENUATION = 5.0 dB per wall

// Elevation optimization
ELEVATION_GAIN = 0.5 dB per meter
OPTIMAL_HEIGHT = 1.5-2.5 meters
```
