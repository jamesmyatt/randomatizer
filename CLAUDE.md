# Randomatizer

Offline, ad-free, tracking-free dice roller for Android. Apache-2.0. F-Droid compatible.

## Hard rules

- No `INTERNET` (or any network) permission. `verify<Variant>Permissions` fails the build if the merged manifest has it. Never disable or weaken that task.
- No Google Play Services, Firebase, analytics or proprietary dependencies. Add dependencies only via `gradle/libs.versions.toml`, and only if FOSS.
- Roll history lives in memory only (`RollerViewModel`). Never persist it: no files, DataStore, Room, `SavedStateHandle` or `rememberSaveable`.
- Only settings (mode, show total, dice colors, last dice selection, panel expanded) may be persisted, via `SettingsRepository` (DataStore).
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

- The app UI always uses the system dynamic color scheme and follows system light/dark mode. It is monochrome: accents use `onSurface`/`surface`, not `primary`.
- Dice colors: System (face `surface`, pips and outline `onSurface`), System inverted (face `onSurface`, pips `surface`) or Custom.
- Custom dice: face must differ from pips. Face vs pips below 3:1 shows a warning. Face vs background below 3:1 gets an outline in the pips color (no message). Face and pips both below 3:1 against a background shows a warning. Check light and dark backgrounds.

## Commands

- Build and test: `./gradlew assembleDebug testDebugUnitTest`
- Lint: `./gradlew lintDebug`
- Unit tests use `SeededRandomSource` (test sources) for determinism.

## Versioning

- `versionName` is `MAJOR.PATCH`, set in `gradle.properties` (`appVersionMajor`, `appVersionPatch`). `versionCode = MAJOR * 1000 + PATCH`.
- Most code changes bump MAJOR and reset PATCH to 0. Bump PATCH only for small fixes, docs or build-only changes.
- Bump the version at most once per branch/PR, relative to the base branch. If the branch already bumps it, don't bump again for further commits; just update that version's changelog (use a MAJOR bump if any change on the branch needs one).
- Every version bump adds `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`. These files are the only changelog: they feed GitHub Release notes and F-Droid. Don't add a separate `CHANGELOG.md`.

## Releases

- Maintainer steps are in `RELEASING.md` (not the README, which is for users). Pushing tag `v<MAJOR>.<PATCH>` runs `.github/workflows/release.yml`, which builds a signed APK and publishes a GitHub Release. The tag must match `gradle.properties` and the changelog must exist.
- Release signing is read only from environment variables (`RANDOMATIZER_KEYSTORE_FILE`, `RANDOMATIZER_KEYSTORE_PASSWORD`, `RANDOMATIZER_KEY_ALIAS`, `RANDOMATIZER_KEY_PASSWORD`), fed from repository secrets. Never commit keystores, passwords or `.b64` files.
- Don't push release tags unless asked.

## Conventions

- Kotlin official code style. Package `io.github.jamesmyatt.randomatizer`.
- American English (International) everywhere: strings, docs and code. All strings in `res/values/strings.xml`.
