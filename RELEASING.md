# Releasing

Releases are published to GitHub Releases by `.github/workflows/release.yml` when a `v<MAJOR>.<PATCH>` tag is pushed.

## One-time setup: signing key

Generate a keystore once and keep it safe. Losing it means users cannot update to future releases.

Work in a private folder outside any Git checkout, e.g. `~/keys/randomatizer`. You don't need the code.

### 1. Create the keystore

`keytool` is in Android Studio's bundled JDK. It prompts for a keystore password and your name.

Linux (adjust the Android Studio path):

```sh
~/android-studio/jbr/bin/keytool -genkeypair -v -keystore randomatizer-release.jks -alias randomatizer -keyalg RSA -keysize 4096 -validity 10000
```

macOS:

```sh
"/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/keytool" -genkeypair -v -keystore randomatizer-release.jks -alias randomatizer -keyalg RSA -keysize 4096 -validity 10000
```

Windows (PowerShell):

```powershell
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -genkeypair -v -keystore randomatizer-release.jks -alias randomatizer -keyalg RSA -keysize 4096 -validity 10000
```

### 2. Base64-encode it

Linux:

```sh
base64 -w0 randomatizer-release.jks > randomatizer-release.jks.b64
```

macOS:

```sh
base64 -i randomatizer-release.jks -o randomatizer-release.jks.b64
```

Windows (PowerShell):

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("$PWD\randomatizer-release.jks")) | Set-Content -NoNewline randomatizer-release.jks.b64
```

### 3. Add repository secrets

Settings → Secrets and variables → Actions:

| Secret | Value |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | Contents of `randomatizer-release.jks.b64` |
| `RELEASE_KEYSTORE_PASSWORD` | Keystore password |
| `RELEASE_KEY_ALIAS` | `randomatizer` |
| `RELEASE_KEY_PASSWORD` | Optional. Only for a key with its own password; keytool's default keystores don't have one. |

### 4. Clean up and back up

Delete the `.b64` file. Back up the `.jks` file and both passwords, e.g. in a password manager.

## Each release

1. Make sure `gradle.properties` has the right version and `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt` exists. The changelog becomes the release notes.
2. Tag and push: `git tag v1.0 && git push origin v1.0`.

The workflow fails if the tag doesn't match the version, the changelog is missing, a secret is missing, or the build, tests or lint fail. It attaches `randomatizer-<version>.apk` and its SHA-256 checksum to the release.

To sign a release locally, set `RANDOMATIZER_KEYSTORE_FILE`, `RANDOMATIZER_KEYSTORE_PASSWORD` and `RANDOMATIZER_KEY_ALIAS` (and `RANDOMATIZER_KEY_PASSWORD` if the key has its own password), then run `./gradlew assembleRelease`. Without them the release APK is unsigned.
