# KMP-Native — Stack 2

Kotlin Multiplatform with **Jetpack Compose** (Android) and **SwiftUI** (iOS).

## Prerequisites

| Tool | Version |
|------|---------|
| JDK | 17+ (tested with Temurin 21) |
| Android Studio | Hedgehog (2023.1.1) or later |
| Xcode | 15+ |
| Kotlin | 2.1.0 (managed by Gradle) |
| Gradle | 8.9 (via wrapper) |
| Android SDK | API 35 (compileSdk), min API 28 |
| iOS | Minimum deployment target 17.0 |

## Project Structure

```
kmp-native/
├── shared/                      ← KMP shared module (domain, data, presentation)
│   └── src/
│       ├── commonMain/          ← Shared Kotlin code
│       ├── androidMain/         ← Android-specific implementations
│       ├── iosMain/             ← iOS-specific implementations
│       └── commonTest/          ← Shared tests
├── androidApp/                  ← Android app (Jetpack Compose)
├── iosApp/                      ← iOS app (SwiftUI)
└── README.md
```

## API Key Setup

1. The API key config file lives at `shared/src/commonMain/resources/config.properties` and is **git-ignored**.
2. Copy the example file and fill in your key:
   ```bash
   cp config.example.properties shared/src/commonMain/resources/config.properties
   ```
3. Edit the file and replace `YOUR_TMDB_API_KEY_HERE` with your actual TMDB API key.

## Build & Run

### Android

**Using the command line:**

```bash
# Build the debug APK
./gradlew :androidApp:assembleDebug

# Install on a connected device/emulator
./gradlew :androidApp:installDebug
```

**Using Android Studio:**

1. Open the `kmp-native/` directory in Android Studio
2. Wait for Gradle sync to complete
3. Select the `androidApp` run configuration
4. Click Run (or press Shift+F10)

### iOS

**Using Xcode:**

1. First, ensure the shared framework can build:
   ```bash
   ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
   ```
2. Open `iosApp/iosApp.xcodeproj` in Xcode
3. Select an iOS 17+ simulator as the destination
4. Build and run (Cmd+R)

The Xcode project includes a **Run Script** build phase that automatically invokes Gradle to build and embed the KMP shared framework before compiling Swift sources.

### Full Build (shared + Android)

```bash
./gradlew build
```

## Running Tests

```bash
# Run shared module tests (common + Android unit tests)
./gradlew :shared:allTests

# Run only common tests on the JVM (Android) target
./gradlew :shared:testDebugUnitTest
```

## Build Time Measurement

**Cold build:**
```bash
# Clean all build caches
./gradlew clean
rm -rf ~/.gradle/caches/build-cache-*

# Time the full build
time ./gradlew :shared:build :androidApp:assembleDebug
```

**Incremental build:**
```bash
# Make a small change (e.g., modify a string in Greeting.kt), then:
time ./gradlew :androidApp:assembleDebug
```

## Technology Stack

| Concern | Choice |
|---------|--------|
| Shared code | Kotlin Multiplatform (commonMain) |
| HTTP | Ktor Client 3.0.3 (OkHttp engine on Android, Darwin engine on iOS) |
| JSON | kotlinx.serialization 1.7.3 |
| Async | kotlinx.coroutines 1.9.0 + StateFlow |
| DI | Koin 4.0.0 |
| Android UI | Jetpack Compose (BOM 2024.12.01) |
| iOS UI | SwiftUI |
| Android Navigation | Jetpack Navigation Compose |
| iOS Navigation | SwiftUI NavigationStack |
| Testing | kotlin.test + kotlinx-coroutines-test |
