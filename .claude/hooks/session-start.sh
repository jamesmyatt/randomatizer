#!/bin/bash
# Installs the Android SDK for Claude Code on the web sessions, so
# ./gradlew assembleDebug testDebugUnitTest works. Needs dl.google.com
# allowed in the environment's network settings.
# Keep PACKAGES in step with compileSdk / build tools in app/build.gradle.kts.
set -euo pipefail

if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

SDK="${ANDROID_HOME:-$HOME/android-sdk}"
CMDLINE_TOOLS="commandlinetools-linux-16111833_latest.zip"
PACKAGES=("platforms;android-37.2" "build-tools;37.0.0" "platform-tools")
SDKMANAGER="$SDK/cmdline-tools/latest/bin/sdkmanager"

if [ ! -x "$SDKMANAGER" ]; then
  echo "Installing Android command-line tools into $SDK"
  tmp="$(mktemp -d)"
  curl -fsSL -o "$tmp/clt.zip" "https://dl.google.com/android/repository/$CMDLINE_TOOLS"
  mkdir -p "$SDK/cmdline-tools"
  unzip -q "$tmp/clt.zip" -d "$tmp"
  rm -rf "$SDK/cmdline-tools/latest"
  mv "$tmp/cmdline-tools" "$SDK/cmdline-tools/latest"
  rm -rf "$tmp"
fi

missing=()
for pkg in "${PACKAGES[@]}"; do
  [ -d "$SDK/${pkg//;//}" ] || missing+=("$pkg")
done
if [ ${#missing[@]} -gt 0 ]; then
  echo "Installing SDK packages: ${missing[*]}"
  yes | "$SDKMANAGER" --sdk_root="$SDK" --licenses > /dev/null 2>&1 || true
  "$SDKMANAGER" --sdk_root="$SDK" --install "${missing[@]}" > /dev/null 2>&1
fi

echo "sdk.dir=$SDK" > "$CLAUDE_PROJECT_DIR/local.properties"
if [ -n "${CLAUDE_ENV_FILE:-}" ]; then
  echo "export ANDROID_HOME=\"$SDK\"" >> "$CLAUDE_ENV_FILE"
fi

# Warm the Gradle wrapper and plugin cache so the container snapshot includes them.
# Not fatal: Maven Central sometimes rate-limits.
(cd "$CLAUDE_PROJECT_DIR" && ./gradlew --quiet help > /dev/null) || echo "Gradle warm-up failed; it will retry on first build."
