# Releasing

Releases are published to GitHub Releases by `.github/workflows/release.yml` when a `v<MAJOR>.<PATCH>` tag is pushed.

## One-time setup: signing key

Generate a keystore once and keep it safe. Losing it means users cannot update to future releases.

`keytool` comes with the JDK (in its `bin/` directory). If you don't have JDK 21, install [Eclipse Temurin 21](https://adoptium.net/temurin/releases/?version=21).

```sh
keytool -genkeypair -v -keystore randomatizer-release.jks -alias randomatizer \
  -keyalg RSA -keysize 4096 -validity 10000
base64 -w0 randomatizer-release.jks > randomatizer-release.jks.b64
```

Add these repository secrets (Settings → Secrets and variables → Actions):

| Secret | Value |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | Contents of `randomatizer-release.jks.b64` |
| `RELEASE_KEYSTORE_PASSWORD` | Keystore password |
| `RELEASE_KEY_ALIAS` | `randomatizer` |
| `RELEASE_KEY_PASSWORD` | Key password |

Then delete the `.b64` file and store the `.jks` and passwords somewhere safe outside the repository.

## Each release

1. Make sure `gradle.properties` has the right version and `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt` exists. The changelog becomes the release notes.
2. Tag and push: `git tag v1.0 && git push origin v1.0`.

The workflow fails if the tag doesn't match the version, the changelog is missing, a secret is missing, or the build, tests or lint fail. It attaches `randomatizer-<version>.apk` and its SHA-256 checksum to the release.

To sign a release locally, set `RANDOMATIZER_KEYSTORE_FILE`, `RANDOMATIZER_KEYSTORE_PASSWORD`, `RANDOMATIZER_KEY_ALIAS` and `RANDOMATIZER_KEY_PASSWORD`, then run `./gradlew assembleRelease`. Without them the release APK is unsigned.
