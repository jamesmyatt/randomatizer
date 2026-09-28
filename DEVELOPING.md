# Developing

## Setup

Install [Android Studio](https://developer.android.com/studio). It includes the JDK and Android SDK. Open the project folder and let Gradle sync.

To build from the command line, set `JAVA_HOME` to Android Studio's bundled JDK (`jbr`), then:

```sh
./gradlew spotlessApply assembleDebug testDebugUnitTest lintDebug
```

- `spotlessApply` formats Kotlin with ktlint (settings in `.editorconfig`). CI runs `spotlessCheck` and fails on unformatted code.
- Lint warnings fail the build.

The project needs JDK 21. If Android Studio bundles a different version, update Android Studio.

## Ways to work

- **Android Studio**: edit, preview Compose screens, run on an emulator or a USB-connected phone (Run ▶).
- **Remote, CI only**: no local install. Edit in the browser or with a cloud coding agent; CI builds and tests each push. Test on your phone with the CI debug APK (below).
- **Command line**: any editor, `./gradlew`, and `adb install` to a USB-connected phone. No Compose previews.

Cloud coding sessions run `.claude/hooks/session-start.sh`, which installs the Android SDK. The environment must allow `dl.google.com`.

## Screenshots

`ScreenshotTest` renders the main screens with Robolectric and Roborazzi, without a device. To write the PNGs to `app/build/outputs/roborazzi/`:

```sh
./gradlew testDebugUnitTest -Proborazzi.test.record=true
```

CI does this on every run and uploads them as `randomatizer-screenshots-<commit>` under Artifacts.

## Dependencies

Renovate opens weekly PRs for Gradle, library and GitHub Actions updates. Actions are pinned to commit SHAs.

## Testing on a phone

Debug builds install alongside the release app as "Randomatizer debug" (application ID `io.github.jamesmyatt.randomatizer.debug`, version `<version>-debug`).

### From CI

1. Open the CI run for the commit (PR → Checks, or the Actions tab).
2. Download `randomatizer-debug-<commit>` under Artifacts. It's kept for 14 days.
3. Unzip it and install `app-debug.apk` on the phone. Allow installs from your browser or file manager if asked.

Each CI run signs the debug APK with a different key, so uninstall the previous "Randomatizer debug" before installing a new one. This resets its settings.

### From Android Studio or the command line

Connect the phone by USB with USB debugging on, then press Run in Android Studio, or:

```sh
./gradlew installDebug
```

Local builds use your own debug key, so they update in place. To switch between a CI build and a local build, uninstall first.
