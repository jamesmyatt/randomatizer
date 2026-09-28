# Randomatizer

Offline dice roller for board games on Android. Free, open source (Apache-2.0), ad-free and tracking-free.

## Features

- **Basic mode**: 1–10 d6, shown as pips.
- **Advanced mode**: any mix of d4, d6, d8, d10, d12, d20 and d100 (up to 10 of each), shown as numbers.
- Shows each result and the total (the total can be hidden in Settings). No modifiers.
- Roll history for the current session, kept in memory only and never saved.
- Collapsible dice selection, remembered between sessions with the other settings.
- System dynamic colours and light/dark mode. Dice colours: System, System inverted or Custom, with contrast warnings.

## Privacy

- No `INTERNET` permission. The build fails if one is merged into the manifest.
- No Google Play Services, Firebase, analytics or proprietary dependencies.
- Rolls use `SecureRandom` via `nextInt(bound)`, which has no modulo bias.

## Build

Requires JDK 21 and the Android SDK (`ANDROID_HOME` or `local.properties`).

```sh
./gradlew assembleDebug testDebugUnitTest
```

Requires Android 12 (API 31) or later.

## Licence

Apache-2.0. See [LICENSE](LICENSE).
