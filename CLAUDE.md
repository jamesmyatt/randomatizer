# Randomatizer

Offline, ad-free, tracking-free dice roller for Android. Apache-2.0. F-Droid compatible.

## Hard rules

- No `INTERNET` (or any network) permission. `verify<Variant>Permissions` fails the build if the merged manifest has it. Never disable or weaken that task.
- No Google Play Services, Firebase, analytics or proprietary dependencies. Add dependencies only via `gradle/libs.versions.toml`, and only if FOSS.
- Roll history lives in memory only (`RollerViewModel`). Never persist it: no files, DataStore, Room, `SavedStateHandle` or `rememberSaveable`.
- Only settings (mode, show total, theme, dice face color, last dice selection, dice panel and history expanded) may be persisted, via `SettingsRepository` (DataStore).
- All roll randomness goes through `RandomSource` / `DiceRoller`. Never use `%` to map random numbers to a range; use `nextInt(bound)`. The roll animation's random faces are cosmetic and use `kotlin.random.Random`.
- `dice/`, `history/`, `color/` and `settings/AppSettings.kt` must not import Android classes, so they stay unit-testable on the JVM.

## Stack

Kotlin, Jetpack Compose, Material 3, single `:app` module, Gradle Kotlin DSL, version catalog.
minSdk 31, targetSdk 37, compileSdk 37.2. AGP built-in Kotlin with the Compose compiler plugin. JDK 21 toolchain.

## Layout

- `dice/`: `RandomSource`, `SecureRandomSource`, `StandardDie`, `DiceRoller`, `Roll`, `AdvancedSelection`.
- `history/`: `RollHistory` (capped at 100, newest first) and history formatting.
- `color/`: WCAG contrast and `dieStyle` rules.
- `settings/`: `AppSettings` and the DataStore `SettingsRepository`.
- `ui/`: `RollerViewModel`, `RollerScreen`, `SettingsSheet`, dice drawing, palette, theme.

## UI rules

- The app UI always uses the system dynamic color scheme. Theme setting: System, Light or Dark, applied with `UiModeManager.setApplicationNightMode` so the system starts the app in the right mode. It is monochrome: accents use `onSurface`/`surface`, not `primary`.
- Mode is an "Advanced mode" switch in Settings. Switching carries the dice over (`AppSettings.withMode`): Basic → Advanced keeps the same d6s; Advanced → Basic keeps the number of dice, up to 12.
- History is pinned to the bottom and collapsible. Expanded, it fills the space below the Roll button; the area above is capped at 60% of the height and scrolls.
- Die size depends only on the number of dice (`dieSize` in `ui/DieSize.kt`): fill the available width in one row, clamped to 78–168 dp (4 per row on a typical phone), so 4d6 looks the same in both modes.
- Dice color sets the face only: Background (`surface`, default), Foreground (`onSurface`) or Custom (18-swatch palette). Pips and numbers are the darker or lighter of `surface` and `onSurface`, chosen by whether black or white contrasts more with the face, so a face gets the same kind of pips in light and dark.
- Face vs background below 3:1 gets an outline in the pips color (no message). Custom face vs pips below 3:1 in light or dark shows a warning.

## Commands

- Build and test: `./gradlew assembleDebug testDebugUnitTest`
- Format: `./gradlew spotlessApply` (ktlint, `.editorconfig`). CI fails on `spotlessCheck`.
- Lint: `./gradlew lintDebug`. Warnings are errors.
- Screenshots: `./gradlew testDebugUnitTest -Proborazzi.test.record=true` writes PNGs to `app/build/outputs/roborazzi/`. Check them after UI changes.
- Unit tests use `SeededRandomSource` (test sources) for determinism.
- For commits that only change docs (`*.md`, `fastlane/` text), add `[skip ci]` to the commit message. Don't skip CI if any code, resources, build or workflow files change.
- GitHub Actions are pinned to commit SHAs; Renovate updates them and all dependencies.
- `.claude/hooks/session-start.sh` installs the Android SDK; keep its package list in step with compileSdk.
- CI uploads the debug APK as an artifact. Debug builds use application ID suffix `.debug` and the name "Randomatizer debug" (`app/src/debug/res`).

## Versioning

- `versionName` is `MAJOR.PATCH`, set in `gradle.properties` (`appVersionMajor`, `appVersionPatch`). `versionCode = MAJOR * 1000 + PATCH`.
- Most code changes bump MAJOR and reset PATCH to 0. Bump PATCH only for small fixes, docs or build-only changes.
- Bump the version at most once per branch/PR, relative to the base branch. If the branch already bumps it, don't bump again for further commits; just update that version's changelog (use a MAJOR bump if any change on the branch needs one).
- Every version bump adds `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`. These files are the only changelog: they feed GitHub Release notes and F-Droid. Don't add a separate `CHANGELOG.md`.

## Releases

- Developer setup and phone testing are in `DEVELOPING.md`; maintainer release steps in `RELEASING.md`. The README is for users. Pushing tag `v<MAJOR>.<PATCH>` runs `.github/workflows/release.yml`, which builds a signed APK and publishes a GitHub Release. The tag must match `gradle.properties` and the changelog must exist.
- Release signing is read only from environment variables (`RANDOMATIZER_KEYSTORE_FILE`, `RANDOMATIZER_KEYSTORE_PASSWORD`, `RANDOMATIZER_KEY_ALIAS`, `RANDOMATIZER_KEY_PASSWORD`), fed from repository secrets. Never commit keystores, passwords or `.b64` files.
- Don't push release tags unless asked.

## Conventions

- Kotlin official code style. Package `io.github.jamesmyatt.randomatizer`.
- American English (International) everywhere: strings, docs and code. All strings in `res/values/strings.xml`.
