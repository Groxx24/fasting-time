# Fasting Time

An intermittent fasting timer for Android and iOS, in two tabs. Timer: hours, minutes and seconds
of the phase under way (fasting or eating) as big text in the middle of the screen, on an animated
backdrop that shows which phase it is, with a button to start the other phase. History: every
finished fast and meal, summed up by day and grouped by week or month. Built with Compose
Multiplatform. Single Gradle module `:app`, package `com.fasting.time`, plus the Xcode host project
in `iosApp/`.

## Source sets

| Source set | Holds |
|---|---|
| `app/src/commonMain` | Everything shared: UI, ViewModels, domain, data. New code goes here by default. |
| `app/src/androidMain` | `MainActivity`, the manifest, launcher resources and platform implementations. |
| `app/src/iosMain` | `MainViewController` (called from Swift) and platform implementations. |
| `iosApp/` | A thin SwiftUI shell that hosts the shared UI. No app logic in Swift. |

Common code must not use `java.*` or `android.*`. When a platform API is needed, put an interface
in `commonMain` next to its caller and implement it in `Xxx.android.kt` and `Xxx.ios.kt` (see
`data/phase/PhaseStore.kt`).

## Architecture

Follow SOLID, MVVM and Clean Architecture. Every feature goes through the same four layers:

```
UI (Compose)  →  ViewModel  →  Use case  →  Repository  →  data source (stores)
```

Dependencies point inward only: `ui` → `domain` ← `data`. The `domain` package must not import
Android, Compose or any platform API.

| Layer | Package | Responsibility |
|---|---|---|
| UI | `ui/<feature>/` | Composables. Render state, emit events. No business logic, no data access. |
| ViewModel | `ui/<feature>/` | Holds one immutable `UiState` as `StateFlow`, turns UI events into use-case calls. Talks only to use cases. |
| Use case | `domain/usecase/` | One business action per class, exposed as `operator fun invoke`. Talks only to repository interfaces. |
| Repository | interface in `domain/repository/`, implementation in `data/repository/` | The single source of truth for a kind of data. Maps stored values to domain models. |
| Data source | `data/phase/`, `data/session/` | `PhaseStore`, `SessionLogStore` and the per-platform storage behind them. |

Rules that follow from this:

- Domain models live in `domain/model/` and are plain Kotlin.
- A ViewModel never touches a repository or data source directly, even for a trivial read. Add a
  use case.
- Each screen has a stateful `XxxRoute` (gets the ViewModel, collects state) and a stateless
  `XxxScreen(state, onEvent…)` that can be previewed and tested without a ViewModel.
- One class, one reason to change. Depend on interfaces across layer boundaries and pass
  dependencies in through constructors; never construct a repository or data source inside its
  consumer.
- Prefer adding a new use case over widening an existing one.

## Technologies

- **Compose Multiplatform** for all UI, shared by both platforms. No XML layouts, no Fragments,
  no SwiftUI screens.
- **Kotlin Coroutines and Flow** for all async work. `suspend` functions for one-shot calls, `Flow`
  for observed data, `viewModelScope` in ViewModels. No callbacks, no RxJava, no LiveData.
- **`kotlin.time`** (`Clock`, `Instant`, `Duration`) for all time. The opt-in it still needs in
  this Kotlin version is given once, in `app/build.gradle.kts`.

Not decided yet: dependency injection, navigation, persistence, and anything else. Do not add a
library for these without asking first. Until then, dependencies are wired by hand in
`di/AppContainer.kt` and nowhere else. Each platform creates one container
(`FastingTimeApplication`, `MainViewController`), passing in the few dependencies that need a
platform object to build (the `PhaseStore`), and routes build their ViewModel from it with
`viewModel { … }`.

## Timer

- There are two phases, `Fasting` and `Eating`. Starting one ends the other; before the first tap
  there is none, and the screen offers only "Start fasting". There is always exactly one button.
- The phase under way and the moment it began are saved in `PhaseStore` (`SharedPreferences` on
  Android, `NSUserDefaults` on iOS).
- The time shown is always the wall clock minus the saved start, never a counter that ticks in
  memory. That is what keeps it right after the app was closed or the phone restarted, so nothing
  runs in the background. Take the time through the injected `Clock`, never `Clock.System`
  directly, so tests can set it.
- `ObserveFastingTimerUseCase` emits once a second, only while collected.

## History

- `StartPhaseUseCase` puts the phase it ends in the log as a `Session` (phase, start, end). A
  phase shorter than a minute is taken for a tap by mistake and is not logged.
- The log is one string in `SessionLogStore` (the same key-value storage as the phase), written
  whole on every change; `StoredSessionRepository` owns the format. That is enough for a few
  sessions a day. No persistence library is chosen, so moving it to a database means replacing
  that repository and nothing else.
- `ObserveHistoryUseCase` does all the summing up. A session counts for the local day it ended
  on. A day keeps its longest fast, so a fast broken early doesn't hide a longer one, and adds up
  its eating. Days are grouped into weeks, which start on Monday, or months.
- The record is the longest fast ever finished. The History tab compares the fast under way to
  it, and shows that one in the record's place once it is longer.
- There is no date library. `Day` (`domain/model/`) turns days since 1970 into a calendar date
  itself, and the only thing asked of the platform is the offset from UTC (`TimeZoneRepository`).
  Month and weekday names are string arrays in the resources.
- `scripts/seed-sample-history.sh android|ios` fills the emulator's or the booted simulator's
  log with sample sessions, replacing what is there, so the History tab has something to show.

## Build

Use Android Studio's bundled JDK; the system default is too new for this Gradle version:

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:compileKotlinIosSimulatorArm64   # checks that shared code compiles for iOS
```

To run on iOS, open `iosApp/iosApp.xcodeproj` in Xcode (16 or newer); its build phase calls Gradle
to build the shared framework. Set `TEAM_ID` in `iosApp/Configuration/Config.xcconfig` to run on a
device.

Library versions in `gradle/libs.versions.toml` are pinned on purpose: this setup is limited to
Android Gradle Plugin 8.13 and compile SDK 36, and newer Compose Multiplatform releases need
plugin 9.1+ and SDK 37. Do not bump them without upgrading the toolchain as a whole.

## UI conventions

- The app is always dark, in both system themes. Colours live in `ui/theme/Theme.kt`; the system
  bars are forced to match in `MainActivity` and in `iosApp/iosApp/Info.plist`.
- `MainScreen` (`ui/main/`) owns the backdrop, the safe-area padding and the tab bar. A tab's
  screen draws only its own content. The two tabs are plain state there, not a navigation graph.
- Anything drawn over the backdrop other than the timer sits on `Modifier.panel`
  (`ui/components/Panel.kt`), which keeps text readable over both skies.
- The backdrop (`ui/fasting/FastingBackdrop.kt`) is drawn on one `Canvas` with no image assets:
  the fork and the knife are `Path`s. Its animation values are read in the draw phase only, so a
  frame repaints without recomposing; keep it that way.
- User-visible text goes in `commonMain/composeResources/values/strings.xml`; read it through the
  generated `Res` class (`com.fasting.time.resources`), and shared icons go in
  `composeResources/drawable/`. Format arguments must be positional (`%1$d`, `%2$s`). Only what the Android manifest needs stays in `androidMain/res`.
