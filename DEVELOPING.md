# Developing

## Setup

Install [Android Studio](https://developer.android.com/studio). It includes the JDK and Android SDK. Open the project folder and let Gradle sync.

To build from the command line, set `JAVA_HOME` to Android Studio's bundled JDK (`jbr`), then:

```sh
./gradlew assembleDebug testDebugUnitTest lintDebug
```

The project needs JDK 21. If Android Studio bundles a different version, update Android Studio.

## Ways to work

- **Android Studio**: edit, preview Compose screens, run on an emulator or a USB-connected phone (Run ▶).
- **Claude Code in the cloud**: no local install. Changes are built and tested by CI; test on your phone with the CI debug APK (below).
- **Command line**: any editor, `./gradlew`, and `adb install` to a USB-connected phone. No Compose previews.

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
