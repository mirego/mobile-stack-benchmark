# External Observations

Notes and findings from manually testing and supervising agent work, outside of what the agents themselves reported.

---

## Milestone 1 — Hello World Apps (2026-02-18)

### Agent Workflow Observations

- **Permission prompts were a major pain point.** Initial attempt without tmux caused all agents to prompt for permission on every Write/Edit/Bash call, even with `bypassPermissions` mode. Fix: run Claude Code inside `tmux -CC`, set `"teammateMode": "tmux"` in settings, and add broad `Bash(*)`, `Edit(*)`, `Write(*)` allow rules in `.claude/settings.local.json`.
- **Split pane display requires tmux.** Won't activate from a plain iTerm2 session — `$TMUX` env var must be set. Using `tmux -CC` integrates nicely with iTerm2's native pane management.
- **Agent shutdown can be stubborn.** One agent (rn-expo-dev) took multiple shutdown requests before terminating. The others shut down on first or second request.
- **Token usage is not available from agents.** Agents have no access to their own session token counters. Wall-clock duration is used as a proxy.

### Manual Testing Results

| Stack | Android | iOS | Web | First-try pass? |
|-------|---------|-----|-----|-----------------|
| RN-Expo | pass | pass | pass | Yes |
| KMP-Native | pass | pass | n/a | Yes |
| KMP-CMP | pass | pass | n/a | No — 2 fixes needed |

### Issues Found & Fixed

1. **KMP-CMP: Xcode project failed to load**
   - **Root cause:** Agent hand-crafted `project.pbxproj` with `rootProject` instead of `rootObject` (wrong key name), 12-char hex IDs (too short), and missing build settings (`GENERATE_INFOPLIST_FILE`, `ENABLE_USER_SCRIPT_SANDBOXING = NO`).
   - **Fix:** Rewrote the pbxproj based on the working kmp-native template with proper 20-char hex IDs and correct structure.
   - **Lesson:** AI-generated Xcode project files are fragile. The kmp-native agent got it right; the kmp-cmp agent didn't. Consider having agents copy from a known-good template rather than generating pbxproj from scratch.

2. **KMP-CMP: iOS crash on launch**
   - **Root cause:** Missing `CADisableMinimumFrameDurationOnPhone` entry in Info.plist, required by Compose Multiplatform for proper rendering on high-refresh-rate iPhones.
   - **Fix:** Created `iosApp/iosApp/Info.plist` with both `CADisableMinimumFrameDuration` and `CADisableMinimumFrameDurationOnPhone` set to `true`, and referenced it in the Xcode project build settings.
   - **Lesson:** CMP-specific iOS requirements are not well-known to LLMs. This should be added to agent instructions for future milestones.

### Key Takeaways

- **RN-Expo is the most AI-friendly stack.** `npx create-expo-app` handles project scaffolding, no binary project files to hand-craft, single language. Zero issues.
- **KMP-Native worked surprisingly well.** Despite being the most complex stack (3 modules, 2 UI frameworks), the agent produced a working project on first try. The Xcode project was correctly structured.
- **KMP-CMP had the most post-agent fixes.** Ironic since it's architecturally simpler than KMP-Native (shared UI). The issue was specifically with Xcode project generation and CMP-specific iOS config — not with the Kotlin/Gradle side.
- **Gradle-based builds are reliable.** All Android targets and shared module compilation worked first try across both KMP stacks. The problems were exclusively on the Xcode/iOS side.
- **Scaffold duration correlates with stack complexity:** RN-Expo (~6 min) < KMP-CMP (~10 min) < KMP-Native (~16 min). This may predict feature development effort in later milestones.

---

## Milestone 2 — Movie List Screen (2026-02-18)

### Agent Durations

| Stack | Duration | Tests |
|-------|----------|-------|
| RN-Expo | ~20 min | 12/12 |
| KMP-CMP | ~21 min | 12/12 |
| KMP-Native | ~27 min | 11/11 |

Feature development durations converged across stacks — the initial scaffolding gap from M1 largely disappeared. KMP-Native still took longest.

### Manual Testing Results

| Stack | Android | iOS | Web | First-try pass? |
|-------|---------|-----|-----|-----------------|
| RN-Expo | pass | pass | pass | No — stale cache |
| KMP-Native | pass | pass | n/a | No — 4 fixes needed |
| KMP-CMP | pass | pass | n/a | No — 4 fixes needed |

No stack worked fully on first try for M2. All required manual intervention.

### Issues Found & Fixed

1. **RN-Expo: Stale content after build**
   - **Symptom:** iOS and Android still showed the Hello World UI after M2 agent completed.
   - **Fix:** `npx expo start --clear` to purge Metro cache, then Cmd+D → Reload on iOS simulator.
   - **Lesson:** Expo's hot reload doesn't always pick up large structural changes. Cache clear is needed.

2. **All stacks: Confusing app titles**
   - **Symptom:** All apps showed generic titles ("Movies", "Popular Movies"), making it hard to identify which stack was running.
   - **Fix:** Renamed titles to match the stack name — "RN-Expo", "KMP-Native", "KMP-CMP".
   - **Lesson:** Should include app titling in agent instructions from the start.

3. **Both KMP stacks (Android): config.properties not found by classloader**
   - **Symptom:** TMDB API returned `{ status_code: 7, status_message: "Invalid API key" }`. The `config.properties` file in `commonMain/resources` wasn't accessible via Android's classloader.
   - **Fix:** Added `sourceSets["main"].resources.srcDirs("src/commonMain/resources")` in `shared/build.gradle.kts`. Also enhanced the Android `ConfigReader` with a multi-step classloader fallback chain.
   - **Lesson:** KMP `commonMain/resources` don't automatically appear on the Android classpath. This is a well-known KMP gotcha that both agents missed.

4. **Both KMP stacks (iOS): config.properties not bundled in iOS app**
   - **Symptom:** `NSBundle.mainBundle.pathForResource` returned nil — the config file wasn't in the iOS app bundle.
   - **Fix:** Copied `config.properties` to `iosApp/iosApp/` and added it to the Xcode project's `PBXResourcesBuildPhase` as a bundle resource.
   - **Lesson:** KMP resources in `commonMain` are not automatically bundled for iOS. Files must be explicitly added to the Xcode project. Both agents failed to handle this.

5. **KMP-CMP (iOS): ExperimentalForeignApi opt-in required**
   - **Symptom:** Compile error on `NSString.stringWithContentsOfFile` — needs `@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)`.
   - **Fix:** Added the opt-in annotation to the iOS `Config.ios.kt`.
   - **Lesson:** Kotlin/Native cinterop APIs increasingly require explicit opt-in. Agents don't consistently apply these.

6. **Both Compose stacks: LazyColumn duplicate key crash**
   - **Symptom:** `kotlin.IllegalArgumentException: Key '991494' was already used` — crash when scrolling and loading page 2+.
   - **Root cause:** TMDB API returns duplicate movie IDs across pages. Both agents used `key = { it.id }` in the LazyColumn `items()` call, which requires unique keys.
   - **Fix:** Removed the `key` parameter from `items()` in both `MovieListScreen.kt` files (kmp-native and kmp-cmp).
   - **Lesson:** Agents assume API data has unique IDs and don't code defensively for duplicates. This is a real-world data quality issue.

7. **KMP-Native (iOS): StateFlow doesn't conform to AsyncSequence**
   - **Symptom:** Swift compile error — `for await state in self.viewModel.uiState` doesn't work because Kotlin's `StateFlow` doesn't conform to Swift's `AsyncSequence` protocol.
   - **Root cause:** Without KMP-NativeCoroutines or SKIE, there's no automatic bridging from Kotlin Flow to Swift async/await.
   - **Fix:** Created a `MovieListStateObserver` helper class in `iosMain` that collects the `StateFlow` using a `CoroutineScope` and calls back into Swift via a closure. Updated the Swift `MovieListObservable` to use this observer instead of `for await`.
   - **Lesson:** This is the core interop challenge of KMP-Native. The agent generated Swift code that assumed SKIE/KMP-NativeCoroutines was present. Without these libraries, manual bridging is required for every Flow observation. This is a significant DX cost unique to the KMP-Native stack.

### Key Takeaways

- **Config/resource loading is a cross-cutting KMP pain point.** Both KMP agents failed to correctly wire `commonMain/resources` for both Android and iOS. This required 4 separate fixes (2 stacks x 2 platforms). RN-Expo had zero resource loading issues.
- **KMP-Native's iOS interop is the most fragile.** The StateFlow/AsyncSequence mismatch is a fundamental gap — agents generate Swift code that looks correct but doesn't compile without additional bridging infrastructure. This is the biggest DX differentiator between KMP-Native and KMP-CMP.
- **Compose Multiplatform shares bugs across platforms.** The duplicate key crash hit both Android and iOS identically in both Compose stacks. One fix applied to both platforms — a strength of shared UI.
- **RN-Expo required the least post-agent work.** Only a cache clear and title rename — no code fixes. The KMP stacks needed multiple code-level interventions.
- **Agent-generated code doesn't handle real-world API quirks.** The TMDB duplicate ID issue is the kind of edge case that only surfaces with live data, and agents optimistically assume clean inputs.
- **Feature development durations equalized.** Unlike M1 where scaffolding time varied 3x (6 vs 16 min), M2 feature work was within 35% across stacks (20–27 min). The complexity gap narrows once the project exists.

---

## Milestone 3 — Movie Detail Screen (2026-02-18)

### Agent Durations

| Stack | Duration | Tests |
|-------|----------|-------|
| RN-Expo | ~3 min | 16/16 (4 new) |
| KMP-CMP | ~3 min | All pass (5 new) |
| KMP-Native | ~4 min | All pass (5 new) |

Dramatically faster than M2. All backend layers were already scaffolded from M2, so agents only needed to implement the presentation + UI layer. Durations nearly identical across all stacks.

### Manual Testing Results

| Stack | Android | iOS | Web | First-try pass? |
|-------|---------|-----|-----|-----------------|
| RN-Expo | pass | pass | pass | No — 1 fix needed |
| KMP-Native | pass | pass | n/a | Yes |
| KMP-CMP | pass | pass | n/a | Yes |

### Issues Found & Fixed

1. **RN-Expo: Duplicate key warning in FlatList**
   - **Symptom:** React warning: "Encountered two children with the same key" when scrolling to page 2+.
   - **Root cause:** Same TMDB duplicate movie ID issue from M2. The agent used `keyExtractor={(item) => item.id.toString()}` which breaks when TMDB returns the same movie across pages.
   - **Fix:** Changed to `keyExtractor={(_, index) => index.toString()}` to use array index instead.
   - **Lesson:** Despite this exact issue being encountered and fixed in M2 for the Compose stacks, the RN-Expo agent made the same mistake. Agents don't learn from sibling agents' failures — each operates in isolation. The M2 fix for Compose (removing `key = { it.id }`) didn't propagate as a lesson to the RN agent.

### Key Takeaways

- **M3 was the smoothest milestone yet.** Only 1 issue across all 3 stacks, compared to 2 in M1 and 7 in M2. The detailed lessons-learned in agent prompts paid off significantly.
- **KMP stacks had zero post-agent fixes.** First time both KMP stacks worked on first try. The explicit instructions about StateFlow observer pattern and not touching pbxproj files eliminated the M2 pain points entirely.
- **Lessons from prior milestones must be explicitly re-taught.** The duplicate key issue was fixed in M2 for Compose but recurred in M3 for React. Agents don't transfer knowledge across stacks or milestones — every lesson must be explicitly stated in each agent's prompt.
- **Feature velocity accelerates as architecture stabilizes.** M3 took ~3-4 min per agent vs ~20-27 min for M2. Once the project scaffolding and data layers exist, adding a new screen is fast regardless of stack.
- **Duration gap between stacks nearly vanished.** RN-Expo and KMP-CMP both at ~3 min, KMP-Native at ~4 min. The extra minute for KMP-Native reflects the dual-UI work (Compose + SwiftUI + StateFlow observer), but it's marginal at this scale.

---

## Benchmark: Lines of Code (after M3)

Counting non-empty lines of app value code only. Excludes all config/scaffolding (build scripts, Gradle wrappers, Xcode project files, config.properties, manifests, READMEs, .gitignore).

### Per-Category Breakdown

| Category | RN-Expo | KMP-Native | KMP-CMP |
|----------|--------:|-----------:|--------:|
| Domain (entities, repository interface, use cases) | 32 | 57 | 56 |
| Data (DTOs, mappers, API client, repository impl) | 118 | 144 | 130 |
| Application / DI | 29 | 31 | 37 |
| Presentation (ViewModels, UI state) | 158 | 134 | 141 |
| UI — screens + components | 400 | 708 (398 Android + 310 iOS) | 522 (shared) |
| Platform bridge (expect/actual, FlowObserver, KoinHelper) | — | 108 | 56 |
| Navigation | — | — | 71 |
| **Production subtotal** | **737** | **1,182** | **942** |
| Tests | 366 | 338 | 375 |
| **Grand total** | **1,103** | **1,520** | **1,317** |

### Analysis

- **RN-Expo is the leanest at 737 production lines.** Single language (TypeScript), single UI codebase, no platform bridging. The simplicity is reflected directly in line count.
- **KMP-Native is the largest at 1,182 production lines (60% more than RN-Expo).** The UI must be written twice: 398 lines of Jetpack Compose for Android + 310 lines of SwiftUI for iOS. An additional 108 lines of platform bridging code (FlowObserver, KoinHelper) is needed to connect Kotlin StateFlow to Swift.
- **KMP-CMP lands in the middle at 942 production lines (28% more than RN-Expo).** Shared Compose UI eliminates the dual-write problem, but Compose Multiplatform is slightly more verbose than React Native for equivalent screens.
- **Shared logic is nearly identical across KMP stacks.** Domain (57 vs 56), data (144 vs 130), presentation (134 vs 141) — the difference is entirely in the UI layer and platform glue.
- **KMP-Native's dual-UI penalty: 708 lines vs KMP-CMP's 522 lines** for equivalent screens. That's a 36% overhead, plus 108 lines of bridging code that KMP-CMP doesn't need at all.
- **Test code is comparable (~340–375 lines).** KMP stacks share tests across platforms, which is a strength. RN-Expo's Jest tests are slightly more verbose but in the same ballpark.
- **The domain and data layers are trivially small in all stacks** (150–200 lines). The bulk of the code — and the bulk of the divergence — is in the UI and presentation layers.

---

## Benchmark: Build Times (after M3)

Measured on Apple Silicon Mac. Gradle home cache (`~/.gradle/`) and CocoaPods cache were warm (simulating a developer who has built other projects before, but this project is freshly checked out). Project-level build dirs and caches were fully deleted before cold builds.

### Cold Build (clean compilation)

**Android:**

| Stack | Time | Method |
|-------|-----:|--------|
| RN-Expo | **2m 23s** | `npm install` + `expo prebuild --clean` + `./gradlew assembleDebug` |
| KMP-Native | **7.4s** | `./gradlew :androidApp:assembleDebug` |
| KMP-CMP | **4.6s** | `./gradlew :androidApp:assembleDebug` |

**iOS:**

| Stack | Time | Method |
|-------|-----:|--------|
| RN-Expo | **1m 40s** | `xcodebuild` (after `pod install`) |
| KMP-Native | **19.8s** | `xcodebuild` (includes Kotlin/Native framework compilation) |
| KMP-CMP | **37.9s** | `xcodebuild` (includes Gradle "Compile Kotlin Framework" build phase) |

### Incremental Build (one ViewModel string change)

Changed a single string in `MovieDetailViewModel` (shared/commonMain) and rebuilt. RN-Expo excluded — Metro hot-reloads JS changes without recompilation.

**Android:**

| Stack | Time | Speed-up vs cold |
|-------|-----:|-----------------|
| KMP-Native | **1.4s** | 5.3x faster |
| KMP-CMP | **3.6s** | 1.3x faster |

**iOS:**

| Stack | Time | Speed-up vs cold |
|-------|-----:|-----------------|
| KMP-Native | **23.0s** | ~same as cold |
| KMP-CMP | **47.2s** | ~same as cold (slower) |

### Analysis

- **RN-Expo has the slowest cold builds by a wide margin.** 2m 23s (Android) and 1m 40s (iOS) — driven by React Native's compilation pipeline (Hermes, CocoaPods, Metro bundling). However, **dev iteration is instant** thanks to Metro hot-reload — no recompilation needed for JS/TS changes.
- **KMP stacks have fast cold builds.** Both under 40s for either platform. KMP-Native Android is the fastest overall at 7.4s.
- **KMP-CMP iOS is notably slower than KMP-Native iOS** (38s vs 20s cold, 47s vs 23s incremental). The "Compile Kotlin Framework" Xcode build phase runs unconditionally ("Based on dependency analysis" is unchecked), meaning Compose Multiplatform's iOS framework is recompiled on every build regardless of what changed.
- **KMP iOS incremental builds show almost no improvement over cold.** The Kotlin/Native framework must be recompiled for any shared code change, which dominates build time. This is a known pain point of KMP iOS development.
- **KMP Android incremental builds are fast.** Gradle's incremental compilation and configuration cache work well — KMP-Native drops from 7.4s to 1.4s.
- **The dev loop trade-off is clear:** RN-Expo pays upfront (slow cold build) but iterates instantly. KMP stacks build fast from clean but pay ~20-47s per iOS change during development. For rapid UI iteration, RN-Expo's hot-reload is a significant DX advantage.

---

## Benchmark: App Cold Start Time (after M3)

Measured using `adb shell am start -W` (Android, reports TotalTime = time to first frame) and `xcrun simctl launch` (iOS). Apps were force-stopped between each run to ensure true cold start. 3 runs per app, median reported.

### Android Cold Start (TotalTime, ms)

| Stack | Run 1 | Run 2 | Run 3 | Median |
|-------|------:|------:|------:|-------:|
| RN-Expo | 1234 | 971 | 932 | **971ms** |
| KMP-Native | 1229 | 1052 | 1055 | **1055ms** |
| KMP-CMP | 983 | 846 | 871 | **871ms** |

### iOS Cold Start

iOS cold start time-to-first-frame cannot be reliably measured from CLI. `xcrun simctl launch` only measures process spawn (~250ms for all apps — not meaningful). Accurate measurement requires Xcode Instruments (App Launch template).

### Analysis

- **All three stacks are in the same ballpark on Android** (~870–1055ms). The differences are small enough that they'd be imperceptible to users.
- **KMP-CMP is the fastest** at 871ms median. Compose Multiplatform with minimal native overhead.
- **RN-Expo is mid-range** at 971ms. The Hermes JS engine initialization adds ~100ms over pure native, but Hermes's ahead-of-time compilation keeps it competitive.
- **KMP-Native is the slowest** at 1055ms. The additional Koin DI initialization and ViewModel setup on top of Compose adds slight overhead compared to KMP-CMP.
- **First run is consistently slower** across all stacks (1229–1234ms). This is expected — the OS caches framework pages after the first launch.
- **No stack has a cold start problem.** All are under 1.1s median, well within acceptable range for a production app.
- **Caveat: RN-Expo debug APK doesn't work standalone** — it requires Metro bundler. The cold start numbers above are debug builds for all stacks, but the RN-Expo debug build was valid (it loaded). A release build of RN-Expo cold-starts at ~230ms median — significantly faster — but isn't directly comparable to the KMP debug builds.

---

## Benchmark: Scroll Performance — Android (after M3)

Measured using `adb shell dumpsys gfxinfo` after 10 automated fast scroll gestures (`adb shell input swipe`, 150ms duration each) on the movie list. Stats reset before scrolling. The "Janky frames" metric uses Android's modern HWUI deadline-based detection.

**Important:** RN-Expo was tested with a **release APK** (debug APK cannot run without Metro). KMP stacks were tested with **debug APKs**. This gives RN-Expo a slight advantage from release optimizations (Hermes AOT, ProGuard/R8).

### Frame Stats

| Metric | RN-Expo (release) | KMP-Native (debug) | KMP-CMP (debug) |
|--------|------------------:|-------------------:|----------------:|
| Total frames rendered | 356 | 347 | 301 |
| Janky frames | 2 (0.56%) | 10 (2.88%) | 4 (1.33%) |
| 50th percentile | 19ms | 17ms | 21ms |
| 90th percentile | 22ms | 18ms | 27ms |
| 95th percentile | 24ms | 23ms | 29ms |
| 99th percentile | 32ms | 32ms | 32ms |

### Analysis

- **KMP-Native has the best median frame time** (17ms p50) but the most jank (2.88%). It's usually the fastest renderer but has occasional hitches — likely from image loading or GC pauses during scrolling.
- **RN-Expo has the least jank** (0.56%) with good overall frame times (19ms p50). The release build with Hermes AOT compilation delivers smooth, consistent scrolling.
- **KMP-CMP has the worst frame times** (21ms p50, 27ms p90) — Compose Multiplatform's rendering pipeline adds overhead. Still only 1.33% jank though.
- **All stacks are in the ~50-60 FPS range** on the emulator. The p50 values (17-21ms) are close to the 16.7ms target for 60fps. On physical hardware, all would likely hit 60fps consistently.
- **The 99th percentile is identical** (32ms) across all stacks — worst-case frames take the same time regardless of framework, likely dominated by image decode or layout recalculation.
- **None of the stacks have a scrolling performance problem.** The differences are measurable but imperceptible to users. All deliver smooth movie list scrolling.

---

## Benchmark: Scroll Performance — iOS (after M3, enriched cells)

Measured using Xcode Instruments **Animation Hitches** template on a physical iPhone device. Movie list cells were enriched with star rating, vote count, overview text, and a color-coded popularity pill to produce more realistic rendering workload. Each app was scrolled through the movie list for ~7-8 seconds.

### Frame Data (from Instruments "Displayed Surfaces")

| Metric | RN-Expo | KMP-Native | KMP-CMP |
|--------|---------|------------|---------|
| Frame duration | 16.70–16.71ms | 16.67–16.68ms | Variable |
| CPU → Display Latency | N/A | N/A (SwiftUI) | 28–48ms |
| Visible hitches | Minor, at scroll start | None | Heavy (many red bars) |
| Overall smoothness | Very smooth | Smoothest | Noticeably janky |

### Analysis

- **KMP-Native (SwiftUI) is the smoothest.** Rock-solid 16.67–16.68ms frame durations — perfect 60fps with zero visible hitches. SwiftUI's diffing-based renderer handles the enriched cells effortlessly. No CPU→Display Latency column because SwiftUI uses a different rendering pipeline than Core Animation compositing.
- **RN-Expo is nearly as smooth.** Consistent 16.70–16.71ms frames with only minor hitches at the very start of scrolling (likely initial React Native bridge warmup). Once scrolling is underway, performance is indistinguishable from native.
- **KMP-CMP has the worst iOS scroll performance by a significant margin.** Heavy red bars (hitches) throughout scrolling and CPU→Display Latency of 28–48ms. Compose Multiplatform's Skia-based rendering on iOS adds measurable overhead — the CPU produces frames faster than the display can consume them, creating a persistent latency gap.
- **The iOS story differs from Android.** On Android, all three stacks performed similarly (~17-21ms p50). On iOS, KMP-CMP falls behind because its Skia canvas renderer doesn't benefit from iOS's native compositing optimizations the way SwiftUI and React Native's Fabric renderer do.
- **KMP-Native's SwiftUI advantage is clear on iOS.** By using the platform's native UI framework, it gets the best possible scroll performance — something KMP-CMP cannot match with its cross-platform Skia approach.
- **RN-Expo punches above its weight on iOS.** React Native's new Fabric architecture with direct JSI bindings delivers near-native scroll performance, validating the "bridge-less" architecture investment.
