# Randomatizer

Offline dice roller for board games on Android. Free, open source (Apache-2.0), ad-free and tracking-free.

## Install

Requires Android 12 (API 31) or later.

Download the APK from [GitHub Releases](https://github.com/jamesmyatt/randomatizer/releases), or get automatic updates with [Obtainium](https://obtainium.imranr.dev/):

[Add to Obtainium](https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/jamesmyatt/randomatizer)

## Features

- **Basic**: 1–10 d6, shown as pips.
- **Advanced**: up to 10 each of d4, d6, d8, d10, d12, d20 and d100, shown as numbers.
- Results and optional total. No modifiers.
- Session history, never saved.
- Minimalist, flat, monochrome design.
- System colors and light/dark mode. Customizable dice colors with contrast warnings.

## Principles

- **Private**: works offline, no `INTERNET` permission (enforced at build time), no ads or tracking.
- **Open**: Apache-2.0, no proprietary dependencies, F-Droid compatible.
- **Fair**: `SecureRandom` with no modulo bias.

## Changelog

See [GitHub Releases](https://github.com/jamesmyatt/randomatizer/releases).

## Build

Requires JDK 21 and the Android SDK (`ANDROID_HOME` or `local.properties`).

```sh
./gradlew assembleDebug testDebugUnitTest
```

## License

Apache-2.0. See [LICENSE](LICENSE).
