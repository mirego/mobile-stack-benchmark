# Stack 3 — KMP + Compose Multiplatform (kmp-cmp)

Movie Catalog benchmark app built with Kotlin Multiplatform and Compose Multiplatform (JetBrains). All UI is shared in `commonMain`.

## Prerequisites

| Tool | Version |
|------|---------|
| JDK | 17+ (tested with Temurin 21) |
| Android Studio | Hedgehog (2023.1.1) or later |
| Xcode | 15+ (for iOS) |
| Kotlin | 2.1.0 (managed by Gradle) |
| Compose Multiplatform | 1.7.3 (managed by Gradle) |
| Min Android | API 28 (Android 9.0) |
| Min iOS | iOS 17.0 |

## Setup

### 1. Clone and provide the API key

Copy the example config and fill in your TMDB API key:

```bash
cp config.example.properties shared/src/commonMain/resources/config.properties
```

Edit `shared/src/commonMain/resources/config.properties`:
```properties
tmdb_api_key=YOUR_REAL_API_KEY
tmdb_base_url=https://api.themoviedb.org/3
tmdb_image_base_url=https://image.tmdb.org/t/p
```

> The `config.properties` file is git-ignored and must not be committed.

### 2. Verify Java

```bash
java -version   # must be 17+
```

## Run on Android

### Via command line

```bash
./gradlew :androidApp:assembleDebug
# Install on connected device/emulator:
adb install androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

### Via Android Studio

1. Open the `kmp-cmp/` directory in Android Studio
2. Select the `androidApp` run configuration
3. Choose an emulator or device (API 28+)
4. Click Run

## Run on iOS

### Via Xcode

1. Build the shared iOS framework first:
   ```bash
   ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
   ```
2. Open `iosApp/iosApp.xcodeproj` in Xcode
3. Select an iOS 17+ simulator
4. Build and Run (Cmd+R)

> The Xcode project includes a build phase that runs `./gradlew :shared:embedAndSignAppleFrameworkForXcode` automatically, so after the first manual framework build, subsequent builds from Xcode will recompile the shared framework as needed.

## Run Tests

```bash
# Common tests (runs on JVM via Android unit test runner)
./gradlew :shared:testDebugUnitTest

# All tests
./gradlew :shared:allTests
```

## Build Time Measurement

### Cold build

```bash
# Clean all caches
./gradlew clean
rm -rf ~/.gradle/caches/build-cache-*

# Time the full build (shared + Android)
time ./gradlew :shared:compileCommonMainKotlinMetadata :shared:compileDebugKotlinAndroid :androidApp:assembleDebug
```

### Incremental build

```bash
# Make a small change (e.g., edit a string in App.kt), then:
time ./gradlew :androidApp:assembleDebug
```

## Project Structure

```
kmp-cmp/
├── shared/                          ← All shared code (domain, data, presentation, UI)
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/kotlin/
│       │   ├── domain/              ← Entities, repository interface, use cases
│       │   ├── data/                ← DTOs, mappers, API client, repository impl
│       │   ├── presentation/        ← ViewModels (StateFlow)
│       │   ├── ui/                  ← Compose Multiplatform screens & components
│       │   ├── navigation/          ← NavHost, route definitions
│       │   └── di/                  ← Koin modules
│       ├── androidMain/kotlin/      ← Android Ktor engine
│       ├── iosMain/kotlin/          ← iOS entry point + Ktor engine
│       └── commonTest/kotlin/       ← Unit tests
├── androidApp/                      ← Minimal Android wrapper
├── iosApp/                          ← Minimal iOS wrapper (Xcode project)
├── config.example.properties        ← Committed config template
└── README.md
```

## Technology Stack

| Concern | Choice |
|---------|--------|
| Shared code | Kotlin Multiplatform |
| UI | Compose Multiplatform (JetBrains) |
| HTTP | Ktor Client |
| JSON | kotlinx.serialization |
| Async | kotlinx.coroutines + StateFlow |
| DI | Koin Multiplatform |
| Navigation | Compose Multiplatform Navigation |
| Image loading | Coil 3 (multiplatform) |
| Testing | kotlin.test + kotlinx-coroutines-test |
