# WiFi Signal Mapping App

A Kotlin/Android application that maps WiFi signal strength and provides intelligent router placement recommendations.

## Core Features

### 1. **Input** - Floorplan Setup
- **Draw Floorplan**: Custom drawing interface for creating floorplan layouts
- **Import Floorplan**: Load existing floorplan images/blueprints
- **Quick Room Selector**: Rapid room-by-room setup for speed

### 2. **Scan** - Guided Walk Test
- Samples RSSI (Received Signal Strength Indicator) at multiple locations
- Detects WiFi bands (2.4 GHz / 5 GHz)
- Measures throughput at each scan point
- Guides user through systematic coverage path

### 3. **Model** - Signal Analysis
- **Propagation Heuristics**:
  - Free-space path loss model
  - Wall attenuation calculations
  - Elevation gain optimization
- **Heatmap Generation**: Interpolates signal strength across floorplan using IDW
- **Candidate Ranking**: Scores locations on centrality, signal quality, and coverage

### 4. **Recommendations** - Placement Optimization
- Displays **top 3 router placement spots**
- Shows expected RSSI improvements for each location
- Provides actionable recommendations:
  - Optimal elevation (typically 1.5-2.5m)
  - Distance to move (often 2-3m to center)
  - Antenna orientation (vertical for horizontal coverage)
- Typical improvement: **2x coverage** when moving from corner to central elevated position

### 5. **Verification** - Before/After Testing
- Re-run quick test after applying recommendation
- Display delta map showing coverage improvement
- Calculate average RSSI improvement percentage

## Project Structure

```
WiFi Signal Map/
├── app/
│   ├── src/main/
│   │   ├── java/com/wifimap/
│   │   │   ├── MainActivity.kt              # Main entry point
│   │   │   ├── models/
│   │   │   │   └── DataModels.kt            # Core data classes
│   │   │   ├── scanning/
│   │   │   │   └── WiFiScanManager.kt       # WiFi scan operations
│   │   │   ├── floorplan/
│   │   │   │   └── FloorplanManager.kt      # Floorplan handling
│   │   │   ├── modeling/
│   │   │   │   └── SignalModelingEngine.kt  # Propagation model & heatmap
│   │   │   └── recommendations/
│   │   │       └── RecommendationEngine.kt  # Placement recommendations
│   │   └── res/
│   │       └── values/
│   │           ├── strings.xml              # String resources
│   │           ├── colors.xml               # Color definitions
│   │           └── styles.xml               # Theme styles
│   ├── build.gradle                         # App-level build config
│   └── AndroidManifest.xml                  # Android manifest
├── build.gradle                             # Project-level build config
└── settings.gradle                          # Project structure

```

## Key Components

### DataModels.kt
- `ScanPoint`: WiFi measurement at a location (RSSI, band, throughput)
- `Floorplan`: Room layout with dimensions
- `Band`: 2.4 GHz vs 5 GHz detection
- `SignalHeatmap`: Grid-based RSSI interpolation
- `PlacementRecommendation`: Ranked router placement with actions
- `VerificationResult`: Before/after comparison

### WiFiScanManager.kt
- Performs WiFi scans using Android WifiManager API
- Detects bands from scan results
- Calculates average RSSI from multiple networks
- Estimates throughput based on signal strength empirical model
- Supports batch guided-walk scanning

### FloorplanManager.kt
- Creates custom floorplans from drawn paths
- Supports room-by-room quick setup
- Imports floorplans from files (placeholder for image processing)
- Validates floorplan structure
- Provides boundary checking and distance calculations

### SignalModelingEngine.kt
- **Propagation Model**:
  - Free-space path loss formula
  - Distance-based attenuation (path loss exponent: 2.5)
  - Wall attenuation (5 dB per wall)
  - Elevation gain (0.5 dB/m, optimal at 2m height)
- **Heatmap Generation**: Inverse Distance Weighted (IDW) interpolation
- **Candidate Ranking**: Multi-factor scoring (centrality, coverage, signal quality)

### RecommendationEngine.kt
- Generates grid of candidate locations
- Ranks by placement score and feasibility
- Calculates expected RSSI improvements
- Generates actionable recommendations
- Evaluates obstacle avoidance

## Technical Stack

- **Language**: Kotlin
- **Framework**: Android Framework (API 30+)
- **Architecture**: MVVM pattern
- **Async**: Coroutines
- **Logging**: Timber
- **Dependencies**:
  - AndroidX (Core, AppCompat, Lifecycle, ViewModel)
  - Material Design 3
  - Kotlin Coroutines
  - Retrofit (for future API calls)
  - Gson (JSON serialization)

## Installation & Setup

1. **Prerequisites**:
   - Android Studio Flamingo or later
   - Android SDK 34+
   - Min SDK: 30
   - Java 11+

2. **Build**:
   ```bash
   ./gradlew build
   ```

3. **Run**:
   ```bash
   ./gradlew installDebug
   ```

4. **Debug**:
   ```bash
   ./gradlew assembleDebug
   ```

## WiFi Permissions

The app requires these Android permissions:
- `ACCESS_FINE_LOCATION` - GPS-based scan location
- `ACCESS_COARSE_LOCATION` - Network-based location fallback
- `CHANGE_NETWORK_STATE` - WiFi state management
- `ACCESS_NETWORK_STATE` - Network connectivity check
- `INTERNET` - Future API communication

## Signal Strength Reference

| RSSI Range | Quality | Throughput |
|-----------|---------|-----------|
| -30 to -50 | Excellent | 100+ Mbps |
| -50 to -60 | Very Good | 80+ Mbps |
| -60 to -70 | Good | 50+ Mbps |
| -70 to -80 | Fair | 20+ Mbps |
| -80 to -100 | Poor | 1-20 Mbps |
| Below -100 | Unusable | <1 Mbps |

## Propagation Model Parameters

- **Free-space loss (2.4 GHz)**: 40 dB at 1m
- **Free-space loss (5 GHz)**: 46 dB at 1m
- **Path loss exponent**: 2.5 (typical indoor)
- **Wall attenuation**: 5 dB per wall
- **Elevation optimization**: 1.5-2.5m height
- **Optimal antenna**: Vertical orientation for horizontal coverage

## Future Enhancements

1. **UI/Visualization**:
   - Real-time heatmap display
   - Interactive floorplan drawing
   - 3D signal visualization

2. **Advanced Features**:
   - Multi-band optimization (separate 2.4/5 GHz maps)
   - Mesh network recommendations
   - Channel selection analysis
   - Interference detection

3. **Improvements**:
   - Machine learning for better propagation prediction
   - Database storage for historical scans
   - Cloud sync and sharing
   - Export reports (PDF/CSV)

## License

MIT License - See LICENSE file for details

## Author

Created for comprehensive WiFi signal mapping and optimization.
