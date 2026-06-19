#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if command -v mise >/dev/null 2>&1; then
  JAVA21_HOME="$(mise where java@21.0.2 2>/dev/null || true)"
  if [[ -n "${JAVA21_HOME}" && -x "${JAVA21_HOME}/bin/java" ]]; then
    export JAVA_HOME="$JAVA21_HOME"
    export PATH="$JAVA_HOME/bin:$PATH"
  fi
fi

rm -rf dist/android dist/web dist/desktop dist/manifest.txt
mkdir -p dist/android dist/web dist/desktop

./gradlew \
  :composeApp:assembleDebug \
  :composeApp:jsBrowserDistribution \
  :composeApp:packageDistributionForCurrentOS

APK_PATH="$(find composeApp/build/outputs/apk/debug -name '*.apk' | head -1)"
if [[ -z "$APK_PATH" ]]; then
  echo "Android APK not found" >&2
  exit 1
fi
cp "$APK_PATH" dist/android/dayplan-android-debug.apk

WEB_SRC="composeApp/build/dist/js/productionExecutable"
if [[ ! -d "$WEB_SRC" ]]; then
  echo "Web distribution not found at $WEB_SRC" >&2
  exit 1
fi
cp -R "$WEB_SRC" dist/web/dayplan-web
(
  cd dist/web
  python3 -m zipfile -c dayplan-web.zip dayplan-web
)

mapfile -t DESKTOP_PACKAGES < <(find composeApp/build/compose/binaries -type f \( -name '*.deb' -o -name '*.dmg' -o -name '*.msi' \) 2>/dev/null | sort)
if [[ "${#DESKTOP_PACKAGES[@]}" -eq 0 ]]; then
  DESKTOP_APP_DIR="$(find composeApp/build/compose/binaries -type d -name dayplan 2>/dev/null | head -1)"
  if [[ -n "$DESKTOP_APP_DIR" ]]; then
    cp -R "$DESKTOP_APP_DIR" dist/desktop/dayplan
    (
      cd dist/desktop
      tar -czf dayplan-desktop.tar.gz dayplan
    )
  else
    echo "Desktop package not found" >&2
    exit 1
  fi
else
  for package in "${DESKTOP_PACKAGES[@]}"; do
    cp "$package" "dist/desktop/$(basename "$package")"
  done
fi

cat > dist/manifest.txt <<MANIFEST
DayPlan local distribution
Built: $(date -u +%Y-%m-%dT%H:%M:%SZ)
Git: $(git rev-parse --short HEAD)

Android:
$(find dist/android -type f -maxdepth 1 -printf '  %f (%s bytes)\n' | sort)

Web:
$(find dist/web -type f -maxdepth 2 -printf '  %P (%s bytes)\n' | sort)

Desktop:
$(find dist/desktop -type f -maxdepth 2 -printf '  %P (%s bytes)\n' | sort)
MANIFEST

cat dist/manifest.txt
