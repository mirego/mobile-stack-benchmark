# Milestone 2 Report — Movie List Screen

**Date:** 2026-02-18
**Objective:** Implement TMDB API integration, Movie List ViewModel with full state machine, Movie List UI with infinite scroll, and unit tests.

---

## Agent Team Summary

| Stack | Agent | Duration | Tests | Build Status |
|-------|-------|----------|-------|-------------|
| RN-Expo | rn-expo-dev | ~20 min | 12/12 pass | TS clean, tests pass |
| KMP-CMP | kmp-cmp-dev | ~21 min | 12/12 pass | Gradle OK |
| KMP-Native | kmp-native-dev | ~27 min | 11/11 pass | Gradle OK |

---

## Stack 1 — React Native + Expo (`rn-expo/`)

### Files Created/Modified
**New files (7):**
- `src/di.ts` — Module-level singleton wiring (API client → repository → use cases)
- `src/presentation/useMovieListViewModel.ts` — Full state machine hook with useReducer
- `src/components/MovieListItem.tsx` — List item with poster (expo-image), title, year, vote average
- `src/components/GenreChip.tsx` — Reusable chip component
- `__tests__/application/GetPopularMoviesUseCase.test.ts` — 2 tests
- `__tests__/application/GetMovieDetailUseCase.test.ts` — 2 tests
- `__tests__/presentation/useMovieListViewModel.test.ts` — 8 tests

**Modified files (2):**
- `app/index.tsx` — Movie list screen with FlatList, infinite scroll, loading/error/retry
- `package.json` — Jest 30→29 (jest-expo 54 compatibility), added @types/jest

### Architecture
- Data: Axios API client, DTOs, mappers, repository impl
- Presentation: `useMovieListViewModel` hook with `useReducer` + `useRef` for stale-closure prevention
- UI: FlatList with `onEndReached` for infinite scroll
- DI: Simple module-level singletons

### Fix Applied
- Jest 30→29: jest-expo 54 depends on Jest 29 internals

### Verification
- 12/12 tests passing (`npm test`)
- TypeScript clean (`npx tsc --noEmit`)

---

## Stack 2 — KMP-Native (`kmp-native/`)

### Files Created/Modified
**Data layer (shared/commonMain):**
- `data/mapper/Mappers.kt` — DTO→domain mapping functions
- `data/remote/TmdbApiClient.kt` — Ktor HTTP client for TMDB API
- `data/remote/ConfigReader.kt` — expect/actual config reader
- `data/repository/MovieRepositoryImpl.kt` — Repository with Result wrapping

**Platform-specific:**
- `androidMain/di/PlatformModule.android.kt` — OkHttp engine + ContentNegotiation
- `iosMain/di/PlatformModule.ios.kt` — Darwin engine + ContentNegotiation
- `androidMain/data/remote/ConfigReader.android.kt` — Java Properties config reader
- `iosMain/data/remote/ConfigReader.ios.kt` — NSBundle config reader

**DI:**
- `di/Modules.kt` — Koin modules: platform, data, domain, presentation

**Presentation:**
- `presentation/movielist/MovieListViewModel.kt` — Full state machine with StateFlow

**iOS bridge:**
- `iosMain/di/KoinHelper.kt` — initKoin() + KoinHelper for Swift access

**Android UI:**
- `android/MainActivity.kt` — KmpNativeApp (Koin init)
- `android/MovieListScreen.kt` — Compose LazyColumn, Coil images, infinite scroll

**iOS UI:**
- `iosApp/MovieListView.swift` — SwiftUI List, AsyncImage, infinite scroll
- `iosApp/ContentView.swift` — Updated to show MovieListView

**Tests (shared/commonTest):**
- `usecase/FakeMovieRepository.kt` — Test fake
- `usecase/TestData.kt` — Test fixtures
- `usecase/GetPopularMoviesUseCaseTest.kt` — 2 tests
- `usecase/GetMovieDetailUseCaseTest.kt` — 2 tests
- `presentation/MovieListViewModelTest.kt` — 7 tests

### Verification
- 11/11 tests passing (`./gradlew :shared:allTests`)
- Android build successful (`./gradlew :androidApp:assembleDebug`)
- Xcode project NOT modified (as instructed)

---

## Stack 3 — KMP-CMP (`kmp-cmp/`)

### Files Created/Modified
**Data layer (shared/commonMain):**
- DTOs with kotlinx.serialization
- Mappers: toDomain() extensions
- `TmdbApiClient` — Ktor HTTP client
- `MovieRepositoryImpl` — Repository with Result wrapping

**Config (expect/actual):**
- Android: reads via java.util.Properties
- iOS: reads via NSBundle.mainBundle

**DI:**
- `appModule` with HttpClient, TmdbApiClient, MovieRepository, use cases
- `initKoin()` with idempotent guard

**Presentation:**
- `MovieListViewModel` with StateFlow<MovieListUiState>, full state machine

**UI (ALL shared Compose Multiplatform):**
- `MovieListScreen` — LazyColumn with loading/error/load-more, infinite scroll
- `MovieListItem` — Card with Coil 3 AsyncImage, title, year, vote average
- `GenreChip` — SuggestionChip composable
- `AppNavigation` — Wires ViewModel via Koin
- `SharedApp` — Coil singleton + MaterialTheme

**Tests (shared/commonTest):**
- `GetPopularMoviesUseCaseTest` — 2 tests
- `GetMovieDetailUseCaseTest` — 2 tests
- `MovieListViewModelTest` — 8 tests

### Verification
- 12/12 tests passing (`./gradlew :shared:testDebugUnitTest`)
- Android build successful (`./gradlew :androidApp:assembleDebug`)
- Xcode project and Info.plist NOT modified (as instructed)

### Key Versions
Kotlin 2.1.0, Compose Multiplatform 1.7.3, Coil 3, Koin 4.0.0

---

## Comparative Notes

| Metric | RN-Expo | KMP-Native | KMP-CMP |
|--------|---------|------------|---------|
| Agent duration | ~20 min | ~27 min | ~21 min |
| Files created/modified | 9 | ~20 | ~19 |
| Unit tests | 12 | 11 | 12 |
| UI implementations | 1 (shared) | 2 (Compose + SwiftUI) | 1 (shared Compose) |
| Duration ratio vs RN | 1x | 1.35x | 1.05x |
