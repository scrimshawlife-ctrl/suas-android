# SUAS Android

This repository is the **Android implementation surface** for Shut Up and Serve (SUAS). It is one of three implementation repositories. If you cloned only this repo, start here so you do not treat the tree as a standalone product.

SUAS coordinates consented veteran support. Canonical product rules live in [SUAS-specs](https://github.com/scrimshawlife-ctrl/SUAS-specs), not in this scaffold.

## Start here

1. Name the sibling surfaces: **suas** (web and API), **suas-ios** (iOS), and **SUAS-specs** (canonical contract).
2. Treat this app as a native client of the product API. Call **`/api/v0` only**.
3. Read [AGENTS.md](AGENTS.md) before you change code.
4. Open [MOBILE_SURFACE.md](https://github.com/scrimshawlife-ctrl/SUAS-specs/blob/main/MOBILE_SURFACE.md) in SUAS-specs (decision **D-033**) before you change networking.

## Version

`0.1.0` (`versionName`, `versionCode` 1). Implements SUAS-specs `0.6.0`.
Pre-1.0 on purpose: SPEC-018 (store and launch) is blocked. See
[CHANGELOG.md](CHANGELOG.md) and [RELEASING.md](RELEASING.md); the scheme is in
SUAS-specs [VERSIONING.md](https://github.com/scrimshawlife-ctrl/SUAS-specs/blob/main/VERSIONING.md) section 8.
Work is tracked on the [SUAS Product Board](https://github.com/users/scrimshawlife-ctrl/projects/6).

## Sibling repositories

| Surface | Repository | Role |
| --- | --- | --- |
| Web and API | [scrimshawlife-ctrl/suas](https://github.com/scrimshawlife-ctrl/suas) | Product API, OpenAPI, and web `/app` HTML. |
| iOS | [scrimshawlife-ctrl/suas-ios](https://github.com/scrimshawlife-ctrl/suas-ios) | Private Swift client over `/api/v0`. |
| Android | [scrimshawlife-ctrl/suas-android](https://github.com/scrimshawlife-ctrl/suas-android) | This repo. Kotlin Compose launcher plus Retrofit `/api/v0` client. |
| Specs | [scrimshawlife-ctrl/SUAS-specs](https://github.com/scrimshawlife-ctrl/SUAS-specs) | Canonical released contract. Native clients follow `MOBILE_SURFACE.md`. |

Keep all three implementation repositories (**suas**, **suas-ios**, and **suas-android**) in future considerations. Do not collapse the product into this Android tree.

## API contract

The Android app is an ordinary authenticated client of the SUAS product API,
`/api/v0` only. This README does not restate the contract; read it in
SUAS-specs and the Worker:

| Topic | Source |
| --- | --- |
| Native client rules, environment classes, forbidden capabilities | [MOBILE_SURFACE.md](https://github.com/scrimshawlife-ctrl/SUAS-specs/blob/main/MOBILE_SURFACE.md) (D-033) |
| How the native clients use `/api/v0` | [D033_NATIVE_CLIENT_INTEGRATION.md](https://github.com/scrimshawlife-ctrl/SUAS-specs/blob/main/D033_NATIVE_CLIENT_INTEGRATION.md) |
| Sign-in, case open, chat parity | [D033_SIGN_IN_PARITY.md](https://github.com/scrimshawlife-ctrl/SUAS-specs/blob/main/D033_SIGN_IN_PARITY.md), [D033_CASE_OPEN.md](https://github.com/scrimshawlife-ctrl/SUAS-specs/blob/main/D033_CASE_OPEN.md), [D033_CHAT_PARITY.md](https://github.com/scrimshawlife-ctrl/SUAS-specs/blob/main/D033_CHAT_PARITY.md) |
| Three-client inventory and current status | [REPOS.md](https://github.com/scrimshawlife-ctrl/SUAS-specs/blob/main/REPOS.md), [STATUS.md](https://github.com/scrimshawlife-ctrl/SUAS-specs/blob/main/STATUS.md) |
| Machine-readable contract (pinned) | [docs/openapi/v0.json](https://github.com/scrimshawlife-ctrl/suas/blob/main/docs/openapi/v0.json) in **suas** |

Rules this code relies on: Bearer session held in memory only (`SessionStore`,
D-034); no `/api/mobile`, client-type header, or second version selector; no
HTML `/app/*` form commands; the app never calls the LOCAL-only `/api/v0/dev/*`
routes (the no-server demo only imitates one in memory). Default host is staging `https://suasqrf.com`
(`Backend.STAGING_BASE`); the emulator LOCAL host is `http://10.0.2.2:3000`.

## Staging host

Synthetic staging is [https://suasqrf.com](https://suasqrf.com). Use it only for non-production builds. Staging must not use real veteran data or real external support effects.

As of 2026-10-07 staging runs **suas** `0f7aeae`, which fixed path-parameter
routes such as `GET /api/v0/cases/{id}/service-requests` (they used to answer
`400`). Staging is deployed only when an owner runs the **suas** `worker-deploy`
workflow by hand; `staging-path-param-check` runs after each deploy. Details:
[suas README](https://github.com/scrimshawlife-ctrl/suas#synthetic-staging-deploys).

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
