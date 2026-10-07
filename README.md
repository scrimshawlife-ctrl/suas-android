# SUAS Android

This repository is the **Android implementation surface** for Shut Up and Serve (SUAS). It is one of three implementation repositories. If you cloned only this repo, start here so you do not treat the tree as a standalone product.

SUAS coordinates consented veteran support. Canonical product rules live in [SUAS-specs](https://github.com/scrimshawlife-ctrl/SUAS-specs), not in this scaffold.

## Start here

1. Name the sibling surfaces: **suas** (web and API), **suas-ios** (iOS), and **SUAS-specs** (canonical contract).
2. Treat this app as a native client of the product API. Call **`/api/v0` only**.
3. Read [AGENTS.md](AGENTS.md) before you change code.
4. Open [MOBILE_SURFACE.md](https://github.com/scrimshawlife-ctrl/SUAS-specs/blob/main/MOBILE_SURFACE.md) in SUAS-specs (decision **D-033**) before you change networking.

## Sibling repositories

| Surface | Repository | Role |
| --- | --- | --- |
| Web and API | [scrimshawlife-ctrl/suas](https://github.com/scrimshawlife-ctrl/suas) | Product API, OpenAPI, and web `/app` HTML. |
| iOS | [scrimshawlife-ctrl/suas-ios](https://github.com/scrimshawlife-ctrl/suas-ios) | Private Swift client over `/api/v0`. |
| Android | [scrimshawlife-ctrl/suas-android](https://github.com/scrimshawlife-ctrl/suas-android) | This repo. Kotlin Compose launcher plus Retrofit `/api/v0` client. |
| Specs | [scrimshawlife-ctrl/SUAS-specs](https://github.com/scrimshawlife-ctrl/SUAS-specs) | Canonical released contract. Native clients follow `MOBILE_SURFACE.md`. |

Keep all three implementation repositories (**suas**, **suas-ios**, and **suas-android**) in future considerations. Do not collapse the product into this Android tree.

## API contract

The Android app is an ordinary authenticated client of the SUAS product API.

- **Path prefix:** `/api/v0`. That prefix is the only version selector.
- **Contract file:** [docs/openapi/v0.json](https://github.com/scrimshawlife-ctrl/suas/blob/main/docs/openapi/v0.json) in **suas**.
- **Auth:** opaque, server-revocable Bearer session (`Authorization: Bearer <credential>`), held in memory (`SessionStore`). Do not write it to disk (D-034).
- **Sign-in:** `POST /api/v0/auth/challenges` then `POST /api/v0/auth/challenges/commands/verify`.
- **Open a Case:** `POST /api/v0/cases` with `Idempotency-Key`. Never `POST /app/qrf/deploy`.
- **Do not** add `/api/mobile`, a client-type header, `/api/v0/dev/*`, or a second version selector.
- **Do not** drive HTML `/app/*` form commands from Android. Those routes belong to the web surface in **suas**.
- **Do not** introduce a mobile test harness in this repository.

Default Retrofit host is staging `https://suasqrf.com` (`Backend.STAGING_BASE`). Emulator LOCAL is `http://10.0.2.2:3000`. Clients may still send build-pinned `Backend.SYNTHETIC_TENANT_ID`; the Worker resolves enrolled email → tenant and treats `tenant_id` as optional. The person must not pick an organization.

## Staging host

Synthetic staging is [https://suasqrf.com](https://suasqrf.com). Use it only for non-production builds. Staging must not use real veteran data or real external support effects.

## Current code (observed)

This tree is a Kotlin Jetpack Compose client. It is a fork of [RuntimeSquad/Suas](https://github.com/RuntimeSquad/Suas). Observed today:

- Launcher activity is `RootActivity` (email sign-in, then ride / food / shelter / peer support).
- `com.example.suas.api` holds Retrofit `SuasApi`, `SessionStore` (memory only), and submit helpers for transportation, food, shelter, and peer support after a signed-in Case open.
- Package and application ID remain `com.example.suas`. That identifier is a placeholder. Specs do not name a production application ID, so this tree does not invent one.
- All four MVP categories are on the launcher home cards.
- `MainActivity` is a debug-only test harness. It is not in the release manifest and it is not exported. The product launcher is `RootActivity`.
- Chat and dashboard-style totals stay unavailable / not computable. Do not print “dispatched now” or lives-saved numbers.
- JVM tests cover the API contract baseline. Instrumented Compose tests still exercise the dummy home.

MVP request categories are FOOD, TRANSPORTATION, temporary SHELTER, and PEER_SUPPORT. Do not add medical or VA-treatment claims in the Android UI.

Do not treat on-screen copy as released crisis copy. D-012 approved wording lives in SUAS-specs.

## Release and production boundary

| Item | Status | What it means here |
| --- | --- | --- |
| **D-033** | Decided. Native client surface released in `MOBILE_SURFACE.md`. | You may implement an Android client of `/api/v0`. |
| **D-034** | Pending. On-device protection of veteran data. | Persist no veteran domain data locally until this decision closes. Hold the bearer in memory only. |
| **SPEC-018** | Blocked. Pilot and production go/no-go. | Do not ship to real veterans, claim a live pilot, or submit to an application store. |
| Production | `NOT_READY` | Implementation authority is not production authority. |

Device push, social login, contact-list access, continuous location, and long-lived unrevocable credentials remain forbidden on a native client.

## Secrets and compliance

- Never commit secrets, session credentials, `.env` files, provider keys, or real contact details.
- Do not claim HIPAA, SOC 2, ISO, or any other compliance certification from this repository or the app UI.
- Do not put provider credentials in the application bundle.

## Build

You need Android Studio (or the Android SDK) with JDK 11 or later.

```bash
./gradlew :app:test
```

Open the project in Android Studio and run the `app` configuration (`RootActivity`) on an emulator or device. Point builds at staging or a local Worker; do not invent a production host.

## Demo modes (debug builds only)

Debug builds add two extra launcher icons next to the normal **SUAS** launcher
(`RootActivity`, pinned STAGING `https://suasqrf.com`). Both live only in
`app/src/debug`, so release builds are unchanged. All data is synthetic:
`@example.invalid` emails and 555-0100 to 555-0199 phone numbers.

| Launcher | Activity | Backend | Environment class |
| --- | --- | --- | --- |
| SUAS | `RootActivity` | STAGING `https://suasqrf.com` | STAGING |
| SUAS Demo (no server) | `DemoRootActivity` | in-memory `DemoSuasApi`, no network | LOCAL |
| SUAS Local Worker | `LocalRootActivity` | `http://10.0.2.2:3000` (host `npm run dev:demo`) | LOCAL |

All three use the same product shell (`SuasProductShell`): SOS-first home cards,
sign-in, and the four request screens. `ShellHooks` adds the demo banner,
sign-in hint, and the "Demo only: advance status" button for the demo launcher;
`RootActivity` always passes `ShellHooks.NONE`.

### No-server demo

```bash
export ANDROID_HOME=/path/to/android-sdk
./gradlew :app:installDebug
adb shell am start -n com.example.suas/.DemoRootActivity
```

Sign in with `demo@example.invalid` and code `123456` (shown on the sign-in
screen). Each request screen opens with the seeded request for that category
(Transportation MATCHING, Food FULFILLED and ready to confirm, Shelter
CANCELLED, Peer Support CREATED); a new request can be submitted, advanced with
the demo button, confirmed, or cancelled. `newvet@example.invalid` (same code)
has no case yet. State lives in memory only and resets when the process ends (D-034).

`DemoSuasApi` loads `app/src/debug/resources/demo/demo-fixtures.json`, a copy of
`contract/demo-fixtures.json`. That file is captured from the real LOCAL Worker
in **suas** (`npm run dev:demo`, then
`npm run demo:fixtures -- --out contract/demo-fixtures.json`), so the shapes
match `/api/v0`. A unit test fails if the two copies drift.

The Worker repo (**suas**) owns this fixture. Both files here are copies of the
Worker export: do not hand-edit them. To change demo data, change the seed in
**suas**, re-run `npm run demo:fixtures` there, and copy the output over both
`contract/demo-fixtures.json` and `app/src/debug/resources/demo/demo-fixtures.json`.
Demo mode lives only in the `debug` source set (no product flavor), so release
builds contain neither the fixture nor the demo launchers.

### Real client against a local Worker

In a **suas** checkout run `npm run dev:demo` (Worker on
`http://127.0.0.1:3000`), then start `LocalRootActivity`:

```bash
adb shell am start -n com.example.suas/.LocalRootActivity
```

The emulator reaches the host at `http://10.0.2.2:3000` (cleartext is allowed
only for `localhost`, `127.0.0.1`, and `10.0.2.2` by
`network_security_config.xml`). Sign in with `demo@example.invalid` (prefilled)
and code `123456`: the LOCAL demo Worker issues that fixed code to this one
account only (`SUAS_DEMO_FIXED_CODE=enabled`, set by `npm run dev:demo`; the
Worker refuses it outside LOCAL). For any other account, read the one-time code
on the host, because the app never calls `/api/v0/dev/*`:

```bash
curl "http://127.0.0.1:3000/api/v0/dev/last-challenge?destination=newvet@example.invalid"
```

### Checks

```bash
bash scripts/forbidden-capabilities.sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease
```

The build needs JDK 25 for the Gradle daemon toolchain. If Gradle cannot find
it, pass `-Porg.gradle.java.installations.paths=$JAVA_HOME`.

## What SUAS is not

SUAS is not an EHR, a diagnosis system, a suicide-prediction product, or an automated emergency dispatcher. The client must not auto-dial 911 or 988. Present crisis destinations only after an explicit person-initiated action, using released copy from SUAS-specs.
