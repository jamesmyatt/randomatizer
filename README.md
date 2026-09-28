# Randomatizer

[![CI](https://github.com/jamesmyatt/randomatizer/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/jamesmyatt/randomatizer/actions/workflows/ci.yml?query=branch%3Amain)

Offline dice roller for board games on Android. Free, open source (Apache-2.0), ad-free and tracking-free.

## Install

Requires Android 12 (API 31) or later.

Download the APK from [GitHub Releases](https://github.com/jamesmyatt/randomatizer/releases), or get automatic updates with [Obtainium](https://obtainium.imranr.dev/):

[Add to Obtainium](https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/jamesmyatt/randomatizer)

## Features

- 2 modes:
  - **Basic**: 1–12 d6, shown as pips.
  - **Advanced**: up to 12 each of d4, d6, d8, d10, d12, d20 and d100, shown as numbers.
- Results and optional total. No modifiers.
- Optional, collapsible session history, never saved.
- Minimalist, flat, monochrome design.
- System colors. Light, dark or system theme. Custom dice colors, with pips that always contrast.

## Principles

- **Private**: works offline, no `INTERNET` permission (enforced at build time), no ads or tracking.
- **Open**: Apache-2.0, no proprietary dependencies, F-Droid compatible.
- **Fair**: `SecureRandom` with no modulo bias.

## Changelog

See [GitHub Releases](https://github.com/jamesmyatt/randomatizer/releases).

## Development

See [DEVELOPING.md](DEVELOPING.md) and [CONTRIBUTING.md](CONTRIBUTING.md). To publish a release, see [RELEASING.md](RELEASING.md).

## License

Apache-2.0. See [LICENSE](LICENSE).
