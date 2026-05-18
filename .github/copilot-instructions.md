<!-- WiFi Signal Mapping App - Project Setup Instructions -->

# WiFi Signal Mapping App - Copilot Instructions

This is a Kotlin/Android application for mapping WiFi signal strength and providing router placement recommendations.

## Core Features
- **Input**: Floorplan import/drawing, quick room-by-room selector
- **Scan**: Guided walk test sampling RSSI, band (2.4/5 GHz), and throughput
- **Model**: Propagation heuristics, signal heatmap, candidate ranking
- **Recommendations**: Top 3 placement spots with RSSI improvements
- **Verification**: Re-run tests and display delta maps

## Project Structure
- `app/` - Main Android application module
- `app/src/main/java/com/wifimap/` - Core application source
- `app/src/main/res/` - Resources (layouts, drawables, strings)
- `gradle/` - Gradle configuration files
- `build.gradle` - Project build configuration

## Development Guidelines
- Use Kotlin for all application code
- Follow Android Architecture Components (MVVM pattern)
- Use Coroutines for async operations
- Implement proper error handling and logging
- Target Android API 30+
