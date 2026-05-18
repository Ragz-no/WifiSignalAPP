# Quick Start Guide

## Installation & Setup

### Prerequisites
- **Android Studio** Flamingo or later
- **Android SDK** 34+
- **Min SDK:** 30 (Android 11)
- **Target SDK:** 34 (Android 14)
- **Java:** 11 or higher
- **Kotlin:** 1.9.10

### Step 1: Environment Setup

1. **Install Android Studio:**
   - Download from [developer.android.com](https://developer.android.com/studio)
   - Install Android SDK 34 via SDK Manager
   - Ensure min API 30 is available

2. **Verify Java:**
   ```bash
   java -version  # Should show Java 11+
   ```

### Step 2: Open Project

1. **In Android Studio:**
   - File → Open
   - Navigate to your WiFi Signal App folder
   - Click OK

2. **Gradle Sync:**
   - Android Studio will auto-sync (or click "Sync Now" if prompted)
   - Wait for dependencies to download (~5-10 minutes first time)

### Step 3: Project Structure Verification

After opening, verify this structure:
```
WiFi Signal App/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/wifimap/
│   │   │   ├── MainActivity.kt
│   │   │   ├── models/DataModels.kt
│   │   │   ├── scanning/WiFiScanManager.kt
│   │   │   ├── floorplan/FloorplanManager.kt
│   │   │   ├── modeling/SignalModelingEngine.kt
│   │   │   └── recommendations/RecommendationEngine.kt
│   │   └── res/values/
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

### Step 4: Build the Project

**In Android Studio:**
```
Build → Make Project
```

Or **from terminal:**
```bash
cd /path/to/WiFi Signal App
./gradlew build
```

### Step 5: Run on Emulator or Device

**On Emulator:**
```
Run → Run 'app'
```

Or select your emulator/device and click the Run button (▶)

**On Physical Device:**
1. Enable Developer Mode (tap Build Number 7 times in Settings)
2. Enable USB Debugging
3. Connect device via USB
4. Run → Run 'app'

### Step 6: Grant Permissions

When app launches, grant these permissions:
- ✓ Access precise location
- ✓ Access WiFi network state
- ✓ Change network state

---

## Building & Testing

### Build APK
```bash
./gradlew build
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

### Build Release APK
```bash
./gradlew build -PreleaseType=production
```
Output: `app/build/outputs/apk/release/app-release.apk`

### Run Unit Tests
```bash
./gradlew test
```

### Run Instrumented Tests (on device)
```bash
./gradlew connectedAndroidTest
```

### Clean Build
```bash
./gradlew clean build
```

---

## IDE Shortcuts

### Android Studio
| Action | Shortcut |
|--------|----------|
| Build | Ctrl+F9 |
| Run | Shift+F10 |
| Debug | Shift+F9 |
| Sync Gradle | Ctrl+Shift+A → "Sync" |
| Logcat | Alt+6 |
| Device Monitor | Ctrl+Shift+A → "Device" |

---

## Troubleshooting

### Gradle Sync Failed
```bash
# Clean and retry
./gradlew clean
./gradlew build

# Or reset Gradle cache
rm -rf ~/.gradle
./gradlew build
```

### Build Error: "SDK not found"
- Open SDK Manager (Tools → SDK Manager)
- Ensure API 34 and API 30 are installed
- Set `compileSdk = 34` in `app/build.gradle.kts`

### Build Error: "Java version mismatch"
```bash
# Check Java version
java -version

# Should be 11 or higher
# Update in Android Studio: File → Project Structure → SDK Location
```

### Emulator Issues
```bash
# List available emulators
emulator -list-avds

# Start emulator manually
emulator -avd <emulator_name>
```

### Permission Denied on Linux/Mac
```bash
chmod +x gradlew
./gradlew build
```

---

## Development Workflow

### 1. Add New Feature

**Example: Add custom logging**

```kotlin
// In MainActivity.kt
import timber.log.Timber

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    Timber.d("App started")
    // Your code...
}
```

### 2. Run Code
```bash
./gradlew installDebug
```

### 3. View Logs
```
View → Tool Windows → Logcat
```

Filter by package:
```
package:com.wifimap
```

### 4. Debug
- Set breakpoint (click line number)
- Run → Debug 'app'
- Step through code (F10/F11)

---

## Project Configuration

### app/build.gradle.kts

Key settings:
```kotlin
android {
    namespace = "com.wifimap"
    compileSdk = 34      // Latest API
    
    defaultConfig {
        minSdk = 30      // Android 11
        targetSdk = 34   // Android 14
        versionCode = 1
        versionName = "1.0.0"
    }
}
```

### AndroidManifest.xml

Required permissions:
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
```

---

## Next Development Steps

### Phase 1: Core UI (Week 1-2)
- [ ] Implement floorplan drawing canvas
- [ ] Create scan progress UI
- [ ] Build recommendation display

### Phase 2: Integration (Week 3)
- [ ] Connect WiFiScanManager to UI
- [ ] Integrate SignalModelingEngine
- [ ] Link RecommendationEngine output

### Phase 3: Enhancement (Week 4+)
- [ ] Heatmap visualization
- [ ] Save/load functionality
- [ ] Export reports

---

## Useful Resources

### Official Documentation
- [Android Developer Docs](https://developer.android.com)
- [Kotlin Documentation](https://kotlinlang.org/docs)
- [AndroidX Documentation](https://developer.android.com/jetpack)

### Helpful Links
- [Material Design](https://material.io/design)
- [WiFi Android API](https://developer.android.com/guide/topics/connectivity/wifi)
- [Coroutines Guide](https://kotlinlang.org/docs/coroutines-overview.html)

### Community
- Stack Overflow: [Tag: android]
- Reddit: r/androiddev
- Android Discord Communities

---

## Support

For issues:
1. Check Logcat for error messages
2. Review [README.md](README.md) for architecture
3. Check [API_REFERENCE.md](API_REFERENCE.md) for usage examples
4. Review [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) for overview

---

**Ready to develop! Happy coding! 🚀**
