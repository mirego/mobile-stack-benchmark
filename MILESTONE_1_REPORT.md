# Milestone 1 Report — Hello World Apps

**Date:** 2026-02-18
**Objective:** Scaffold each stack with a "Hello World" screen, full directory structure, and verified builds.

---

## Agent Team Summary

| Stack | Agent | Duration | Build Status |
|-------|-------|----------|-------------|
| RN-Expo | rn-expo-dev | ~6 min | TS compiles, web export builds |
| KMP-CMP | kmp-cmp-dev | ~10 min | Android APK + iOS targets compile |
| KMP-Native | kmp-native-dev | ~16 min | Android APK + iOS framework compile |

Token usage was not available from agent sessions. Duration is used as a proxy for scaffold effort.

---

## Stack 1 — React Native + Expo (`rn-expo/`)

### What was built
- Expo SDK 54 with TypeScript strict mode
- Expo Router v4 (file-based routing) with `app/` directory
- TanStack Query v5 provider wired in root layout
- All dependencies installed: axios, @tanstack/react-query, expo-image, expo-router, react-native-web, jest-expo, eslint, prettier, @testing-library/react-native

### Hello World screen
- `app/index.tsx` — "Hello from React Native + Expo!" centered on screen
- `app/movie/[id].tsx` — placeholder detail screen with route param
- `app/_layout.tsx` — root layout with Stack navigator and QueryClientProvider

### Directory structure
Full Section 5.1 structure scaffolded:
- `src/domain/` — entities.ts, MovieRepository.ts
- `src/data/` — dtos.ts, mappers.ts, TmdbApiClient.ts, MovieRepositoryImpl.ts
- `src/application/` — GetPopularMoviesUseCase.ts, GetMovieDetailUseCase.ts
- `src/presentation/` — useMovieListViewModel.ts, useMovieDetailViewModel.ts
- `src/components/` — MovieListItem.tsx, GenreChip.tsx
- `__tests__/application/` and `__tests__/presentation/`

### Config
- `src/config.ts` — real TMDB API key (git-ignored)
- `config.example.ts` — committed placeholder

### Build verification
- TypeScript compiles with zero errors (`npx tsc --noEmit`)
- Web export builds successfully (`npx expo export --platform web`)

### Key versions
Expo SDK 54, TypeScript strict, Expo Router v4, TanStack Query v5

### Run commands
```bash
cd rn-expo
npm install
npx expo start        # then press i (iOS), a (Android), or w (Web)
npx expo start --ios  # direct iOS launch
npm test              # run tests
```

---

## Stack 2 — KMP-Native (`kmp-native/`)

### What was built
- Full KMP project: shared module + androidApp (Jetpack Compose) + iosApp (SwiftUI)
- "Hello from KMP-Native!" displayed centered on both platforms
- Complete module structure from Section 5.2 with all M2/M3 placeholder files

### Project structure
```
kmp-native/
├── shared/                      ← KMP shared module
│   └── src/
│       ├── commonMain/          ← Greeting, Platform, domain/data/presentation/di
│       ├── androidMain/         ← Platform.android.kt
│       ├── iosMain/             ← Platform.ios.kt
│       └── commonTest/          ← Test dirs with .gitkeep
├── androidApp/                  ← Jetpack Compose MainActivity
├── iosApp/                      ← SwiftUI with Xcode project
├── gradle/                      ← Wrapper (8.9) + libs.versions.toml
├── config.example.properties    ← Committed placeholder
├── .gitignore                   ← Ignores config.properties, build dirs
└── README.md
```

### Config
- `shared/src/commonMain/resources/config.properties` — TMDB key, git-ignored
- `config.example.properties` — committed placeholder

### Build verification
- `./gradlew :shared:build :androidApp:assembleDebug` — BUILD SUCCESSFUL (138 tasks)
- Shared module compiles for: Android, iosX64, iosArm64, iosSimulatorArm64

### Key versions
Kotlin 2.1.0, AGP 8.7.3, Gradle 8.9, Ktor 3.0.3, Koin 4.0.0, Compose BOM 2024.12.01

### Run commands
```bash
cd kmp-native
./gradlew :androidApp:assembleDebug          # Android
# iOS: open iosApp/iosApp.xcodeproj in Xcode, Cmd+R
./gradlew :shared:allTests                   # Tests
./gradlew clean && time ./gradlew :shared:build :androidApp:assembleDebug  # Cold build time
```

---

## Stack 3 — KMP-CMP (`kmp-cmp/`)

### What was built
- Full KMP + Compose Multiplatform project with shared UI in commonMain
- `SharedApp()` composable displaying "Hello from KMP-CMP!" centered on screen
- androidApp = minimal wrapper (MainActivity → `setContent { SharedApp() }`)
- iosApp = minimal wrapper (ComposeUIViewController wrapping SharedApp)
- Complete module structure from Section 5.3 with all M2/M3 placeholder files

### Project structure
```
kmp-cmp/
├── shared/                      ← All shared code (domain, data, presentation, UI)
│   └── src/
│       ├── commonMain/          ← App.kt, Platform, domain/data/presentation/ui/nav/di
│       ├── androidMain/         ← Platform.android.kt
│       ├── iosMain/             ← Platform.ios.kt + MainViewController.kt
│       └── commonTest/          ← Test dirs
├── androidApp/                  ← Minimal Compose wrapper
├── iosApp/                      ← SwiftUI + ComposeView wrapper, Xcode project
├── gradle/                      ← Wrapper (8.11.1)
├── config.example.properties    ← Committed placeholder
├── .gitignore
└── README.md
```

### Config
- `shared/src/commonMain/resources/config.properties` — TMDB key, git-ignored
- `config.example.properties` — committed placeholder

### Build verification
- `./gradlew :shared:compileCommonMainKotlinMetadata :shared:compileDebugKotlinAndroid :androidApp:assembleDebug` — BUILD SUCCESSFUL
- Full `:shared:build` compiles iOS targets (iosX64, iosArm64, iosSimulatorArm64)
- Note: full build with framework linking needs 4GB+ heap

### Key versions
Kotlin 2.1.0, Compose Multiplatform 1.7.3, AGP 8.7.3, Gradle 8.11.1

### Run commands
```bash
cd kmp-cmp
./gradlew :androidApp:assembleDebug          # Android
# iOS: open iosApp/iosApp.xcodeproj in Xcode, Cmd+R
./gradlew :shared:testDebugUnitTest          # Tests
./gradlew clean && time ./gradlew :shared:build :androidApp:assembleDebug  # Cold build time
```

---

## Early Observations

- **RN-Expo** was the fastest to scaffold (~6 min) — single language, less boilerplate, mature CLI tooling
- **KMP-Native** took the longest (~16 min) — two separate UI layers (Compose + SwiftUI) plus shared KMP module
- **KMP-CMP** was in between (~10 min) — shared UI reduces platform-specific code vs KMP-Native
- The duration trend (RN < CMP < Native) may foreshadow the feature development effort pattern in later milestones
