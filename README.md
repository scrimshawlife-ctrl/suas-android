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

- Launcher activity is `RootActivity` (email sign-in, then ride / food / shelter).
- `com.example.suas.api` holds Retrofit `SuasApi`, `SessionStore` (memory only), and submit helpers for transportation, food, and shelter after a signed-in Case open.
- Package and application ID remain `com.example.suas`.
- Peer support is **not** on the launcher home cards yet (see specs `GAP_ANALYSIS.md`).
- `MainActivity` remains as a dummy form for instrumented tests. It is not the product launcher and must not imply live fulfillment.
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

## What SUAS is not

SUAS is not an EHR, a diagnosis system, a suicide-prediction product, or an automated emergency dispatcher. The client must not auto-dial 911 or 988. Present crisis destinations only after an explicit person-initiated action, using released copy from SUAS-specs.
