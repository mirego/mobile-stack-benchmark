# Stack 1 — React Native + Expo

Movie Catalog benchmark app built with React Native, Expo SDK 54, TypeScript, and Expo Router v4.

## Prerequisites

| Tool | Version |
|------|---------|
| Node.js | 18+ (LTS recommended) |
| npm | 9+ |
| Expo CLI | Installed via `npx expo` |
| iOS Simulator | Xcode 15+ with iOS 17+ simulator |
| Android Emulator | Android Studio with API 28+ emulator |
| Web browser | Any modern browser (Chrome recommended) |

## Setup

1. **Install dependencies:**

   ```bash
   cd rn-expo
   npm install
   ```

2. **Configure the TMDB API key:**

   Copy the example config and add your API key:

   ```bash
   cp config.example.ts src/config.ts
   ```

   Edit `src/config.ts` and replace `YOUR_TMDB_API_KEY_HERE` with your actual TMDB API key:

   ```ts
   export const TMDB_API_KEY = "your_actual_key_here";
   export const TMDB_BASE_URL = "https://api.themoviedb.org/3";
   export const TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p";
   ```

   > **Note:** `src/config.ts` is git-ignored and must never be committed.

## Running the App

### iOS Simulator

```bash
npx expo start --ios
```

Or start the dev server and press `i`:

```bash
npx expo start
# Then press 'i' in the terminal
```

### Android Emulator

```bash
npx expo start --android
```

Or start the dev server and press `a`:

```bash
npx expo start
# Then press 'a' in the terminal
```

### Web Browser

```bash
npx expo start --web
```

Or start the dev server and press `w`:

```bash
npx expo start
# Then press 'w' in the terminal
```

## Running Tests

```bash
npm test
```

## Build Commands

### Cold Build (Development)

```bash
# Clear caches first
rm -rf node_modules/.cache .expo
npx expo start --clear
```

### Measure Cold Build Time

```bash
# iOS native build
rm -rf ios
time npx expo run:ios

# Android native build
rm -rf android
time npx expo run:android
```

### Incremental Build

Change a file and save — Metro bundler will hot-reload automatically.

## Project Structure

```
rn-expo/
├── app/                        # Expo Router screens
│   ├── _layout.tsx             # Root layout with QueryClientProvider
│   ├── index.tsx               # Movie list screen
│   └── movie/[id].tsx          # Movie detail screen
├── src/
│   ├── domain/
│   │   ├── entities.ts         # Movie, MovieDetail, Genre, MoviePage types
│   │   └── MovieRepository.ts  # Repository interface
│   ├── data/
│   │   ├── dtos.ts             # DTO types (mirror API JSON)
│   │   ├── mappers.ts          # DTO → domain mapping
│   │   ├── TmdbApiClient.ts    # Axios HTTP client
│   │   └── MovieRepositoryImpl.ts
│   ├── application/
│   │   ├── GetPopularMoviesUseCase.ts
│   │   └── GetMovieDetailUseCase.ts
│   ├── presentation/
│   │   ├── useMovieListViewModel.ts   # Custom hook (ViewModel)
│   │   └── useMovieDetailViewModel.ts
│   ├── components/
│   │   ├── MovieListItem.tsx
│   │   └── GenreChip.tsx
│   └── config.ts               # API key (git-ignored)
├── config.example.ts           # Committed placeholder config
├── __tests__/
│   ├── application/            # Use case tests
│   └── presentation/           # ViewModel tests
└── README.md
```

## Technology Stack

| Concern | Choice | Version |
|---------|--------|---------|
| Framework | Expo SDK | 54 |
| Language | TypeScript | strict mode |
| Navigation | Expo Router | v4 (file-based) |
| HTTP | Axios | latest |
| Server state | TanStack Query | v5 |
| Images | expo-image | latest |
| Testing | Jest + React Testing Library | latest |
| Linting | ESLint + Prettier | latest |
