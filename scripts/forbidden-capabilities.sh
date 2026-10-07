#!/bin/bash
# Fail if the Android client grows a capability MOBILE_SURFACE.md §5 forbids.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
src="$root/app/src"
main_manifest="$src/main/AndroidManifest.xml"
fail=0
note() { echo "FORBIDDEN: $*"; fail=1; }

if grep -q 'MainActivity' "$main_manifest"; then
  note "MainActivity is in the main manifest"
fi
if ! grep -q 'text = TEST_HARNESS_NOTICE' "$src/debug/java/com/example/suas/MainActivity.kt"; then
  note "MainActivity no longer shows the TEST HARNESS ONLY banner"
fi
if grep -q 'allowBackup="true"' "$main_manifest"; then
  note "allowBackup is still the template default"
fi
if ! grep -q 'allowBackup="false"' "$main_manifest"; then
  note "allowBackup is not explicitly false"
fi
if ! grep -q 'networkSecurityConfig' "$main_manifest"; then
  note "network security config is missing"
fi

if grep -R -n -E 'ACCESS_BACKGROUND_LOCATION|ACCESS_COARSE_LOCATION|ACCESS_FINE_LOCATION|READ_CONTACTS|CALL_PHONE|POST_NOTIFICATIONS|FirebaseMessaging|GoogleSignIn|androidx.security.crypto|SharedPreferences|EncryptedSharedPreferences|androidx.room|ACTION_CALL|/api/mobile|/api/v0/dev/|Log\.(d|e|i|w|v)\(|println\(' \
  "$src/main" --include='*.kt' --include='*.xml'; then
  note "main source matches a forbidden capability or logging pattern"
fi

if grep -R -n 'newIdempotencyKey()' "$src/main" --include='*.kt'; then
  note "a screen is minting an idempotency key outside SubmissionAttempt"
fi

python3 - "$src/main/java/com/example/suas/api/SuasApi.kt" <<'PY'
import pathlib, re, sys
text = pathlib.Path(sys.argv[1]).read_text()
paths = re.findall(r'@(?:GET|POST)\("([^"]+)"\)', text)
bad = [p for p in paths if not p.startswith("/api/v0/") or "/api/mobile" in p or "/app/" in p or "/dev/" in p]
if bad:
    print("CONTRACT: " + ", ".join(bad))
    sys.exit(1)
if "com.example.suas" not in pathlib.Path(sys.argv[1]).read_text() and False:
    sys.exit(1)
PY

if [[ "$fail" -ne 0 ]]; then
  exit 1
fi
echo "forbidden-capability scan passed"
