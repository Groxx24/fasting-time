# Fasting Time

An intermittent fasting timer for Android and iOS. The first screen asks for the window to fast in
every day, from one time on the clock until another. After that the app shows which phase it is by
that window (fasting or eating) and the hours, minutes and seconds left of it, as big text in the
middle of the screen on an animated backdrop that shows the phase. The app only follows the clock:
it never asks whether the user really started or stopped, and keeps no record. A notification
shows the phase while the app is closed. Built with Compose Multiplatform. Single Gradle module
`:app`, package `com.fasting.time`, plus the Xcode host project in `iosApp/`.

## Source sets

| Source set | Holds |
|---|---|
| `app/src/commonMain` | Everything shared: UI, ViewModels, domain, data. New code goes here by default. |
| `app/src/androidMain` | `MainActivity`, the manifest, launcher resources and platform implementations. |
| `app/src/iosMain` | `MainViewController` (called from Swift) and platform implementations. |
| `iosApp/` | A thin SwiftUI shell that hosts the shared UI, and the `PhaseActivity` widget extension that draws the Live Activity. No app logic in Swift. |

Common code must not use `java.*` or `android.*`. When a platform API is needed, put an interface
in `commonMain` next to its caller and implement it in `Xxx.android.kt` and `Xxx.ios.kt` (see
`data/window/FastingWindowStore.kt`).

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
| Data source | `data/window/` | `FastingWindowStore` and the per-platform storage behind it. |

Rules that follow from this:

- Domain models live in `domain/model/` and are plain Kotlin.
- A ViewModel never touches a repository or data source directly, even for a trivial read. Add a
  use case.
- Each screen has a stateful `XxxRoute` (gets the ViewModel, collects state) and a stateless
  `XxxScreen(state, onEvent…)` that can be previewed and tested without a ViewModel. The setup
  screen has no route of its own: `MainRoute` hands it the window and saves the one it confirms.
- One class, one reason to change. Depend on interfaces across layer boundaries and pass
  dependencies in through constructors; never construct a repository or data source inside its
  consumer.
- Prefer adding a new use case over widening an existing one.

## Technologies

- **Compose Multiplatform** for all UI, shared by both platforms. No XML layouts, no Fragments,
  no SwiftUI screens. The one exception is the Live Activity, which the system renders outside
  the app and so has to be SwiftUI.
- **Kotlin Coroutines and Flow** for all async work. `suspend` functions for one-shot calls, `Flow`
  for observed data, `viewModelScope` in ViewModels. No callbacks, no RxJava, no LiveData.
- **`kotlin.time`** (`Clock`, `Instant`, `Duration`) for all time. The opt-in it still needs in
  this Kotlin version is given once, in `app/build.gradle.kts`.

Not decided yet: dependency injection, navigation, persistence, and anything else. Do not add a
library for these without asking first. Until then, dependencies are wired by hand in
`di/AppContainer.kt` and nowhere else. Each platform creates one container
(`FastingTimeApplication`, `MainViewController`), passing in the few dependencies that need a
platform object to build (the `FastingWindowStore`, the `TimeZoneRepository`, the
`PhaseNotifier`), and routes build their ViewModel from it with `viewModel { … }`.

## Timer

- A `FastingWindow` is two times on the clock: when the fast starts and when it ends. It repeats
  every day and runs past midnight when it ends earlier than it starts. It is saved in
  `FastingWindowStore` (`SharedPreferences` on Android, `NSUserDefaults` on iOS) as two counts of
  minutes since midnight.
- Until a window is saved the app shows only the setup screen, which offers 20:00 to 12:00. From
  the timer, the line showing the window leads back to the same screen to change it.
- There are two phases, `Fasting` inside the window and `Eating` outside it. Nothing is started
  or stopped by hand: the phase and the time left of it are worked out from the wall clock and
  the window on every reading, never from a counter that ticks in memory. That is what keeps it
  right after the app was closed or the phone restarted, so nothing runs in the background. Take
  the time through the injected `Clock`, never `Clock.System` directly, so tests can set it.
- The timer counts down: to the end of the window while fasting, to its start while eating.
  The line above it names what the countdown leads to ("Eating in", "Fasting in"), and the line
  below it the phase it is now and until when. The notifications use the same words.
- `ObserveFastingTimerUseCase` emits once a second, only while collected.
- There is no date library. The only thing asked of the platform is the offset from UTC
  (`TimeZoneRepository`), which turns the clock's instant into local time since midnight. Times
  are shown on a 24-hour clock on both platforms.

## Notification

- `PhaseNotifier` (`domain/notification/`) is the one thing the domain asks for; each platform
  implements it in `ui/notification/`, with the words shared in `PhaseNotificationText.kt` so
  they match the timer screen. `ShowPhaseNotificationUseCase` works out the phase and the moment
  it ends and hands them over. `MainViewModel` calls it at launch and after a window is chosen.
- Nothing of the app keeps running for it. Each platform leaves the waiting to the system.
- Android: one silent notification that counts down and times out when the phase ends. An inexact
  alarm for that moment wakes `PhaseNotificationReceiver`, which calls the use case again; so do
  a restart and a change of the clock or time zone, which lose or move the alarm. The
  notification is ongoing from Android 14, where the user can still swipe it away; before that
  it is a plain one. A phase whose notification the user removed is remembered by the moment it
  ends and not shown again; the next phase is. `MainActivity` asks for the permission.
- iOS shows the phase in two ways, because neither is enough alone (`LockScreenPhaseNotifier`):
  - A Live Activity counts down on the Lock Screen and in the Dynamic Island. Only the open app
    can start one, and the system ends it eight hours later, so it covers that long after the app
    was last opened and not a whole fast. It is given the phase after the current one too, and
    turns to it by itself when the current one ends (that is its stale date). If the user removes
    it, it stays away until the next phase.
  - Two local notifications repeat daily, one when each phase begins, and wait in Notification
    Center until removed. They need no app running, so they still come when the Live Activity
    is gone. Opening the app clears the one about the phase that is over.
- Kotlin cannot reach ActivityKit. `PhaseLiveActivity` (`iosMain`) is the interface the notifier
  talks to; `LiveActivityController.swift` implements it and is passed to `MainViewController`.
  The words and the moments are still worked out in Kotlin. `iosApp/Shared/` holds the
  `PhaseAttributes` both Swift targets compile; `iosApp/PhaseActivity/` is the widget extension,
  bundle id `com.fasting.time.PhaseActivity`.

## Build

Use Android Studio's bundled JDK; the system default is too new for this Gradle version:

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:compileKotlinIosSimulatorArm64   # checks that shared code compiles for iOS
```

To run on iOS, open `iosApp/iosApp.xcodeproj` in Xcode (16 or newer); its build phase calls Gradle
to build the shared framework. The deployment target is iOS 16.2, the first with the Live Activity
API used here. Set `TEAM_ID` in `iosApp/Configuration/Config.xcconfig` to run on a device.

Library versions in `gradle/libs.versions.toml` are pinned on purpose: this setup is limited to
Android Gradle Plugin 8.13 and compile SDK 36, and newer Compose Multiplatform releases need
plugin 9.1+ and SDK 37. Do not bump them without upgrading the toolchain as a whole.

## UI conventions

- The app is always dark, in both system themes. Colours live in `ui/theme/Theme.kt`; the system
  bars are forced to match in `MainActivity` and in `iosApp/iosApp/Info.plist`.
- `MainScreen` (`ui/main/`) owns the backdrop and the safe-area padding, and shows the setup or
  the timer on it. Each of those draws only its own content. Which one shows is plain state
  there, not a navigation graph.
- Anything drawn over the backdrop other than the timer sits on `Modifier.panel`
  (`ui/components/Panel.kt`), which keeps text readable over both skies.
- The backdrop (`ui/fasting/FastingBackdrop.kt`) is drawn on one `Canvas` with no image assets:
  the fork and the knife are `Path`s. Its animation values are read in the draw phase only, so a
  frame repaints without recomposing; keep it that way.
- User-visible text goes in `commonMain/composeResources/values/strings.xml`; read it through the
  generated `Res` class (`com.fasting.time.resources`). Format arguments must be positional (`%1$d`, `%2$s`). Only what the Android manifest needs stays in `androidMain/res`.
