# Stack Benchmark: Movie Catalog App — Specifications

## 1. Project Overview

### Goal
Compare three mobile development stacks by implementing the same app independently, then evaluating each against defined criteria. The implementations must be structurally equivalent so differences in effort, testability, and performance are attributable to the stack rather than design choices.

### Stacks
| # | Folder | Technologies | Targets |
|---|--------|-------------|---------|
| 1 | `rn-expo/` | React Native + Expo SDK | iOS, Android, Web |
| 2 | `kmp-native/` | Kotlin Multiplatform + Jetpack Compose (Android) + SwiftUI (iOS) | iOS, Android |
| 3 | `kmp-cmp/` | Kotlin Multiplatform + Compose Multiplatform | iOS, Android |

### Repository Layout
```
stack-benchmark/
├── SPECS.md          ← this file
├── rn-expo/          ← Stack 1
├── kmp-native/       ← Stack 2
└── kmp-cmp/          ← Stack 3
```

---

## 2. App Features (MVP)

### Screen 1 — Movie List
- Displays a paginated list of popular movies from TMDB `/movie/popular`
- Each list item shows: poster thumbnail, title, release year, vote average (e.g. ★ 7.8)
- Infinite scroll / load-more: when the user reaches the end of the list, the next page is fetched
- **Loading state** (first page): full-screen centered spinner
- **Loading more state** (subsequent pages): spinner at the bottom of the list
- **Error state**: centered message + "Retry" button
- Tapping a list item navigates to Movie Detail

### Screen 2 — Movie Detail
- Navigated to by tapping a movie in the list; receives the movie `id` as parameter
- Displays: backdrop image (full-width, ~220 dp tall), poster overlaid bottom-left, title, tagline (italic), vote average, genres (horizontal chips), runtime (minutes), release date, overview text
- **Loading state**: centered spinner
- **Error state**: message + "Retry" button
- Back navigation to the list

### Out of Scope (for this benchmark)
- User authentication / accounts
- Search / filter
- Favorites or any local persistence
- Offline caching (network-only, no disk cache for responses)

---

## 3. TMDB API Reference

### Base URL
```
https://api.themoviedb.org/3
```

### Authentication
All requests use a query parameter:
```
?api_key=<TMDB_API_KEY>
```

**The API key must never be hardcoded in committed source files.** See Section 5 for per-stack config mechanisms. The key for this project is `70914b5a203986e7815619855987404f`.

### Image URLs
- Poster (list): `https://image.tmdb.org/t/p/w342{poster_path}`
- Poster (detail overlay): `https://image.tmdb.org/t/p/w342{poster_path}`
- Backdrop (detail header): `https://image.tmdb.org/t/p/w780{backdrop_path}`

### Endpoints

#### GET /movie/popular
Fetches paginated popular movies.

| Query param | Type | Default | Description |
|-------------|------|---------|-------------|
| `page` | Int | 1 | Page number (1-based) |

**Response (relevant fields):**
```json
{
  "page": 1,
  "total_pages": 500,
  "results": [
    {
      "id": 12345,
      "title": "Movie Title",
      "overview": "Synopsis text...",
      "poster_path": "/abc123.jpg",
      "backdrop_path": "/xyz789.jpg",
      "release_date": "2024-01-15",
      "vote_average": 7.8,
      "vote_count": 1200
    }
  ]
}
```

#### GET /movie/{movie_id}
Fetches detailed information for a single movie.

**Response (relevant fields):**
```json
{
  "id": 12345,
  "title": "Movie Title",
  "tagline": "The official tagline",
  "overview": "Synopsis text...",
  "poster_path": "/abc123.jpg",
  "backdrop_path": "/xyz789.jpg",
  "release_date": "2024-01-15",
  "vote_average": 7.8,
  "vote_count": 1200,
  "runtime": 142,
  "genres": [
    { "id": 28, "name": "Action" },
    { "id": 12, "name": "Adventure" }
  ]
}
```

---

## 4. Shared Architecture Blueprint

All three stacks follow **Clean Architecture** with an **MVVM presentation layer**. This ensures the comparison is structurally fair and each layer is independently testable.

```
┌──────────────────────────────────────────────────────────────┐
│  UI Layer                                                    │
│  Screens · Components · Composables · SwiftUI Views          │
│  (platform-specific in Stack 1 & 2; shared in Stack 3)      │
├──────────────────────────────────────────────────────────────┤
│  Presentation Layer                                          │
│  MovieListViewModel · MovieDetailViewModel                   │
│  (shared in KMP stacks; React hooks in Stack 1)             │
├──────────────────────────────────────────────────────────────┤
│  Domain Layer                                                │
│  Entities · Use Cases · Repository Interfaces                │
│  (always a shared concept across all stacks)                 │
├──────────────────────────────────────────────────────────────┤
│  Data Layer                                                  │
│  Repository Impl · DTOs · HTTP Client / API                  │
│  (always a shared concept across all stacks)                 │
└──────────────────────────────────────────────────────────────┘
```

### 4.1 Domain Layer

#### Entities

```
Movie
  id            : Int
  title         : String
  overview      : String
  posterPath    : String?       // e.g. "/abc123.jpg" (no base URL)
  backdropPath  : String?
  releaseDate   : String        // "YYYY-MM-DD"
  voteAverage   : Double
  voteCount     : Int

MovieDetail                     // all Movie fields, plus:
  tagline       : String?
  runtime       : Int?          // minutes; null if not set
  genres        : List<Genre>

Genre
  id            : Int
  name          : String

MoviePage
  movies        : List<Movie>
  page          : Int
  totalPages    : Int
```

#### Repository Interface

```
interface MovieRepository {
    suspend fun getPopularMovies(page: Int): Result<MoviePage>
    suspend fun getMovieDetail(id: Int): Result<MovieDetail>
}
```

`Result<T>` is the language-native wrapper: `kotlin.Result` for KMP stacks; a custom `sealed class Result<T>` for TypeScript (since the native `Promise` handles async, use a `{ data, error }` pattern or a small custom sealed type).

#### Use Cases

```
class GetPopularMoviesUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(page: Int): Result<MoviePage>
}

class GetMovieDetailUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(id: Int): Result<MovieDetail>
}
```

Use cases are intentionally thin — their value is testability and a stable contract between presentation and data.

### 4.2 Data Layer

#### DTOs (mirror JSON response exactly)

```
MovieDto           { id, title, overview, poster_path, backdrop_path,
                     release_date, vote_average, vote_count }
GenreDto           { id, name }
MovieDetailDto     { ...MovieDto fields + tagline, runtime, genres: List<GenreDto> }
PopularMoviesResponseDto { page, total_pages, results: List<MovieDto> }
```

#### Mapping Functions

```
MovieDto.toDomain(): Movie
MovieDetailDto.toDomain(): MovieDetail
GenreDto.toDomain(): Genre
PopularMoviesResponseDto.toDomain(): MoviePage
```

Mapping lives in the data layer and is never exposed to domain or presentation.

#### Remote Data Source / API Client

- A class (or interface + impl) that executes HTTP calls and returns DTOs
- Handles HTTP error codes and network failures, converting them to domain errors before returning
- The API key is injected at construction time (not hardcoded)

#### Repository Implementation

- Implements `MovieRepository`
- Calls the remote data source
- Maps DTOs to domain models using mapping functions
- Propagates errors as `Result.failure` / equivalent

### 4.3 Presentation Layer

#### MovieListViewModel

**State:**
```
data class MovieListUiState(
    val isLoading: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val error: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = Int.MAX_VALUE,
    val isLoadingMore: Boolean = false
)
```

**Intent / Actions:**
- `loadFirstPage()` — clears existing movies, loads page 1
- `loadNextPage()` — loads `currentPage + 1` if not already loading and page < totalPages
- `retry()` — re-invokes the last failed action

**State transitions:**
```
Initial          → loadFirstPage() → isLoading=true
isLoading=true   → success         → isLoading=false, movies=page1Movies, currentPage=1
isLoading=true   → failure         → isLoading=false, error="message"
movies loaded    → loadNextPage()  → isLoadingMore=true
isLoadingMore    → success         → isLoadingMore=false, movies=movies+newPage, currentPage++
error shown      → retry()         → re-runs last failed load
```

#### MovieDetailViewModel

**State:**
```
data class MovieDetailUiState(
    val isLoading: Boolean = false,
    val movieDetail: MovieDetail? = null,
    val error: String? = null
)
```

**Actions:**
- `loadDetail(id: Int)`
- `retry()`

### 4.4 UI Layer — Visual Specification

The UI does not need to be pixel-perfect across stacks but must be functionally equivalent. Use each platform's idiomatic components.

**Movie List Screen**
- Vertical scrollable list
- Each item (approx 100 dp tall): poster thumbnail on the left (w≈80 dp), title in bold, release year and vote average on the right or below
- First-load: centered spinner replaces the list
- Load-more: spinner row at the bottom of the list
- Error: centered text + "Retry" button, no list

**Movie Detail Screen**
- Backdrop image: full-width, fixed height ~220 dp
- Poster thumbnail: ~120 dp wide, overlaid at bottom-left of backdrop (overlaps content below)
- Content below backdrop: title (large/bold), tagline (italic, muted), vote average (★ X.X)
- Genres: horizontal row of small chip/badge widgets
- Runtime: "142 min" and release date: "2024-01-15"
- Overview: multi-line text
- Back button uses platform-native navigation affordance

---

## 5. Stack-Specific Implementation

### 5.1 Stack 1 — RN-Expo (`rn-expo/`)

#### Technology Stack
| Concern | Choice | Version |
|---------|--------|---------|
| Framework | Expo SDK | 52+ |
| Language | TypeScript | strict mode |
| Navigation | Expo Router | v4 (file-based) |
| HTTP | Axios | latest |
| Server state | TanStack Query (React Query) | v5 |
| Images | expo-image | latest |
| Testing | Jest + React Testing Library | latest |
| Linting | ESLint + Prettier | — |

#### Directory Structure
```
rn-expo/
├── app/                        ← Expo Router screens
│   ├── index.tsx               ← Movie list screen
│   └── movie/[id].tsx          ← Movie detail screen
├── src/
│   ├── domain/
│   │   ├── entities.ts         ← Movie, MovieDetail, Genre, MoviePage types
│   │   └── MovieRepository.ts  ← Repository interface (TypeScript interface)
│   ├── data/
│   │   ├── dtos.ts             ← DTO types
│   │   ├── mappers.ts          ← DTO → domain mapping
│   │   ├── TmdbApiClient.ts    ← Axios client
│   │   └── MovieRepositoryImpl.ts
│   ├── application/
│   │   ├── GetPopularMoviesUseCase.ts
│   │   └── GetMovieDetailUseCase.ts
│   ├── presentation/
│   │   ├── useMovieListViewModel.ts   ← custom hook (ViewModel)
│   │   └── useMovieDetailViewModel.ts
│   ├── components/
│   │   ├── MovieListItem.tsx
│   │   └── GenreChip.tsx
│   └── config.ts               ← API key (git-ignored)
├── config.example.ts           ← Committed placeholder
├── __tests__/
│   ├── application/
│   └── presentation/
└── README.md
```

#### Config File (git-ignored)
```ts
// src/config.ts  (DO NOT COMMIT — listed in .gitignore)
export const TMDB_API_KEY = "70914b5a203986e7815619855987404f";
export const TMDB_BASE_URL = "https://api.themoviedb.org/3";
export const TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p";
```
```ts
// config.example.ts  (committed)
export const TMDB_API_KEY = "YOUR_TMDB_API_KEY_HERE";
export const TMDB_BASE_URL = "https://api.themoviedb.org/3";
export const TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p";
```

#### ViewModel Pattern (React hooks)
ViewModels are custom hooks that use `useReducer` for state and call use-case instances. The `TanStack Query` client is used for caching the raw HTTP responses; the ViewModel hook maps query results to `UiState`.

#### Dependency Injection
No DI framework. Use module-level singleton instances (create once at module import time) and pass through constructors where needed. Avoid global state beyond what TanStack Query manages.

---

### 5.2 Stack 2 — KMP-Native (`kmp-native/`)

#### Technology Stack
| Concern | Choice |
|---------|--------|
| Shared code | Kotlin Multiplatform (commonMain) |
| HTTP | Ktor Client (OkHttp engine on Android, Darwin engine on iOS) |
| JSON | kotlinx.serialization |
| Async | kotlinx.coroutines + StateFlow |
| DI | Koin Multiplatform |
| Android UI | Jetpack Compose |
| iOS UI | SwiftUI |
| Android Navigation | Jetpack Navigation Compose |
| iOS Navigation | SwiftUI NavigationStack |
| KMP↔Swift bridge | KMP-NativeCoroutines (for StateFlow → AsyncStream) |
| Testing | kotlin.test + kotlinx-coroutines-test + MockK |
| Min Android | API 28 (Android 9.0) |
| Min iOS | iOS 17 |

#### Module Structure
```
kmp-native/
├── shared/                          ← KMP module
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/kotlin/
│       │   ├── domain/
│       │   │   ├── entity/          ← Movie, MovieDetail, Genre, MoviePage
│       │   │   ├── repository/      ← MovieRepository interface
│       │   │   └── usecase/         ← GetPopularMoviesUseCase, GetMovieDetailUseCase
│       │   ├── data/
│       │   │   ├── dto/             ← DTOs
│       │   │   ├── mapper/          ← DTO → domain
│       │   │   ├── remote/          ← TmdbApiClient (Ktor)
│       │   │   └── repository/      ← MovieRepositoryImpl
│       │   ├── presentation/
│       │   │   ├── movielist/       ← MovieListViewModel (StateFlow)
│       │   │   └── moviedetail/     ← MovieDetailViewModel (StateFlow)
│       │   └── di/                  ← Koin modules
│       ├── androidMain/kotlin/      ← Android-specific Ktor engine setup
│       ├── iosMain/kotlin/          ← iOS-specific Ktor engine setup
│       └── commonTest/kotlin/
│           ├── usecase/             ← Use case unit tests
│           └── presentation/        ← ViewModel unit tests
├── androidApp/
│   └── src/main/
│       ├── java/.../               ← Compose screens, nav graph
│       └── res/
├── iosApp/
│   └── iosApp/
│       ├── ContentView.swift        ← SwiftUI root
│       ├── MovieListView.swift
│       └── MovieDetailView.swift
└── README.md
```

#### Config
```properties
# shared/src/commonMain/resources/config.properties  (git-ignored)
tmdb_api_key=70914b5a203986e7815619855987404f
tmdb_base_url=https://api.themoviedb.org/3
tmdb_image_base_url=https://image.tmdb.org/t/p
```
```properties
# config.example.properties  (committed)
tmdb_api_key=YOUR_TMDB_API_KEY_HERE
```
The config is loaded at app startup via a `BuildConfig`-style mechanism or a simple `Properties` reader in commonMain.

#### ViewModel in Shared Code
ViewModels expose `StateFlow<UiState>` and are created in `commonMain`. On Android, they extend `ViewModel` from the Android lifecycle library or are wrapped by one. On iOS, they are accessed via KMP-NativeCoroutines, converting `StateFlow` to `AsyncStream` for SwiftUI `@StateObject` / `@ObservableObject` consumption.

---

### 5.3 Stack 3 — KMP-CMP (`kmp-cmp/`)

#### Technology Stack
| Concern | Choice |
|---------|--------|
| Shared code | Kotlin Multiplatform |
| UI | Compose Multiplatform (JetBrains) |
| HTTP | Ktor Client |
| JSON | kotlinx.serialization |
| Async | kotlinx.coroutines + StateFlow |
| DI | Koin Multiplatform |
| Navigation | Compose Multiplatform Navigation (JetBrains) |
| Image loading | Coil 3 (multiplatform) |
| Testing | kotlin.test + kotlinx-coroutines-test + MockK |
| Min Android | API 28 |
| Min iOS | iOS 17 |

#### Module Structure
```
kmp-cmp/
├── shared/                          ← All shared code (domain, data, presentation, UI)
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/kotlin/
│       │   ├── domain/              ← Same structure as kmp-native/shared
│       │   ├── data/
│       │   ├── presentation/
│       │   ├── ui/
│       │   │   ├── movielist/       ← MovieListScreen.kt (Composable)
│       │   │   ├── moviedetail/     ← MovieDetailScreen.kt (Composable)
│       │   │   └── components/      ← Shared UI components
│       │   ├── navigation/          ← NavHost, route definitions
│       │   └── di/                  ← Koin modules
│       ├── androidMain/kotlin/      ← Android entry point / Ktor engine
│       ├── iosMain/kotlin/          ← iOS entry point / Ktor engine
│       └── commonTest/kotlin/
│           ├── usecase/
│           └── presentation/
├── androidApp/                      ← Minimal: MainActivity + setContent { SharedApp() }
├── iosApp/                          ← Minimal: ComposeUIViewController wrapping SharedApp
└── README.md
```

#### Config
Same as KMP-Native: `config.properties` in resources, git-ignored, with a committed example.

---

## 6. Testing Requirements

### Scope
Unit tests are required for **Use Cases** and **ViewModels** in all three stacks. No UI/integration tests for this benchmark.

### Use Case Tests (per use case, per stack)

| Test | Scenario | Setup | Expected |
|------|----------|-------|----------|
| `getPopularMovies_success` | Repository returns data | Mock returns `Result.success(moviePage)` | Use case returns `Result.success(moviePage)` |
| `getPopularMovies_failure` | Repository throws | Mock returns `Result.failure(exception)` | Use case propagates failure |
| `getMovieDetail_success` | Repository returns data | Mock returns `Result.success(detail)` | Use case returns `Result.success(detail)` |
| `getMovieDetail_failure` | Repository throws | Mock returns failure | Use case propagates failure |

Use a **fake/mock** `MovieRepository` — do not call real HTTP in unit tests.

### ViewModel Tests (per ViewModel, per stack)

| Test | Action | Expected state |
|------|--------|----------------|
| `initialState` | — | `isLoading=false, movies=[], error=null` |
| `loadFirstPage_success` | `loadFirstPage()` | loading → `movies=page1, currentPage=1, isLoading=false` |
| `loadFirstPage_failure` | `loadFirstPage()` → repo fails | `isLoading=false, error!=null, movies=[]` |
| `loadNextPage_appendsMovies` | load page 1, then `loadNextPage()` | `movies = page1 + page2, currentPage=2` |
| `loadNextPage_noOpAtEnd` | currentPage == totalPages | `loadNextPage()` does nothing |
| `retry_afterFailure` | failure then `retry()` | re-triggers load |
| `retry_afterSuccess_noOp` | success then `retry()` | no duplicate load |
| `loadFirstPage_resetsState` | call with existing movies | clears old movies, reloads |

### Testing Tools
| Stack | Framework | Mocking |
|-------|-----------|---------|
| RN-Expo | Jest + `@testing-library/react-hooks` | Manual fakes + `jest.fn()` |
| KMP-Native | kotlin.test + kotlinx-coroutines-test | MockK |
| KMP-CMP | kotlin.test + kotlinx-coroutines-test | MockK |

### Test Locations
| Stack | Path |
|-------|------|
| RN-Expo | `rn-expo/__tests__/application/`, `rn-expo/__tests__/presentation/` |
| KMP-Native | `kmp-native/shared/src/commonTest/kotlin/` |
| KMP-CMP | `kmp-cmp/shared/src/commonTest/kotlin/` |

---

## 7. Comparison Criteria & Measurement Methodology

These criteria guide the final report. Agents should be aware of them and structure code to make measurement straightforward.

### 1. Effort to Add New Features
**How measured:** When adding Milestone 2 (movie list), record:
- Number of files created or modified
- Lines of code added (net)
- Subjective friction notes (e.g., "needed to touch 6 files to add one field")

### 2. Effort to Maintain / Fix Bugs
**How measured:** Qualitative assessment — how many layers must be touched to change one thing? How easy is it to find where a behavior is implemented? Note any platform-specific workarounds needed.

### 3. Ease of Testing
**How measured:**
- Lines of test boilerplate per test (setup, mocking)
- Mocking complexity (manual fakes vs framework)
- Time to run the full test suite (record in README)

### 4. Build Time
**How measured (record in each stack's README):**
- **Cold build:** delete build caches, run full build, time it (`time` command or Gradle/Xcode build report). Repeat 3×, take median.
- **Incremental build:** change one string in the ViewModel, rebuild, time it. Repeat 3×.
- **Dev build vs release build:** record both if they differ significantly

Build commands will be finalized at Milestone 1 completion.

### 5. Runtime Performance
**How measured:**
- **Cold start:** time from process launch to first frame (use `adb shell am start -W` for Android; Xcode Instruments for iOS)
- **Scroll FPS:** scroll the movie list rapidly, record average FPS via platform tools (Android GPU Profiler, Xcode Instruments → Core Animation)
- **Memory:** peak memory during scroll (Android Profiler / Instruments)

---

## 8. Milestones

### Milestone 1 — Hello World ← **Current Target**
Each stack must produce a runnable multi-platform app where:
- [ ] A single screen displays "Hello from [Stack Name]!" centered on screen
- [ ] The app runs on both target platforms:
  - Stack 1: iOS Simulator + Android Emulator + Web browser
  - Stack 2: iOS Simulator + Android Emulator
  - Stack 3: iOS Simulator + Android Emulator
- [ ] Directory/module structure matches Section 5 (folders created, correctly named, even if mostly empty)
- [ ] A `README.md` at the stack root documents:
  - Prerequisites and version requirements
  - Step-by-step setup instructions (including how to provide the API key)
  - How to run on each target platform
  - How to run tests
  - Command for cold build + how to measure build time
- [ ] **Stop here and await approval before proceeding to Milestone 2**

### Milestone 2 — Movie List Screen
- TMDB API integration (HTTP client, DTOs, repository)
- Use cases wired up
- MovieListViewModel with full state machine
- Movie List UI: list, loading, error, load-more
- Unit tests for use cases and ViewModel

### Milestone 3 — Movie Detail Screen
- Navigation from list to detail (pass movie id)
- MovieDetailViewModel with full state machine
- Movie Detail UI: backdrop, poster, metadata, genres
- Unit tests for detail ViewModel

### Milestone 4 — Final Polish & Benchmark
- All tests passing
- Build time measurements recorded in each README
- Runtime performance measurements noted
- Final comparison report written (`BENCHMARK_REPORT.md`)

---

## 9. General Conventions (All Stacks)

- **No hardcoded API keys** in committed files — always read from git-ignored config
- **No magic strings** — use named constants for route names, config keys, image sizes
- **Error messages** are user-facing strings (not raw exception messages)
- **Null safety** — handle null poster/backdrop paths gracefully (show placeholder)
- **Release date display** — parse "YYYY-MM-DD" and display only the year in the list; display full date in detail
- **Vote average** — display as "★ 7.8" (one decimal place)
- **Runtime** — display as "142 min"; if null, display "N/A"
