# SUAS Android

This repository is the **Android implementation surface** for Shut Up and Serve (SUAS). It is one of three implementation repositories. If you cloned only this repo, start here so you do not treat the tree as a standalone product.

SUAS coordinates consented veteran support. Canonical product rules live in [SUAS-specs](https://github.com/scrimshawlife-ctrl/SUAS-specs), not in this scaffold.

## Start here

1. Name the sibling surfaces: **suas** (web and API), **suas-ios** (iOS), and **SUAS-specs** (canonical contract).
2. Treat this app as a native client of the product API. Call **`/api/v0` only**.
3. Read [AGENTS.md](AGENTS.md) before you change code.
4. Open [MOBILE_SURFACE.md](https://github.com/scrimshawlife-ctrl/SUAS-specs/blob/main/MOBILE_SURFACE.md) in SUAS-specs (decision **D-033**) before you add networking.

## Sibling repositories

| Surface | Repository | Role |
| --- | --- | --- |
| Web and API | [scrimshawlife-ctrl/suas](https://github.com/scrimshawlife-ctrl/suas) | Product API, OpenAPI, and web `/app` HTML. |
| iOS | [scrimshawlife-ctrl/suas-ios](https://github.com/scrimshawlife-ctrl/suas-ios) | Private Swift client. Already has a handwritten `APIClient` over `/api/v0`. |
| Android | [scrimshawlife-ctrl/suas-android](https://github.com/scrimshawlife-ctrl/suas-android) | This repo. Kotlin Compose scaffold. No API client yet. |
| Specs | [scrimshawlife-ctrl/SUAS-specs](https://github.com/scrimshawlife-ctrl/SUAS-specs) | Canonical released contract. Native clients follow `MOBILE_SURFACE.md`. |

Keep all three implementation repositories (**suas**, **suas-ios**, and **suas-android**) in future considerations. Do not collapse the product into this Android tree.

## API contract

The Android app is an ordinary authenticated client of the SUAS product API.

- **Path prefix:** `/api/v0`. That prefix is the only version selector.
- **Contract file:** [docs/openapi/v0.json](https://github.com/scrimshawlife-ctrl/suas/blob/main/docs/openapi/v0.json) in **suas**.
- **Auth:** opaque, server-revocable Bearer session (`Authorization: Bearer <credential>`).
- **Do not** add `/api/mobile`, a client-type header, or a second version selector.
- **Do not** drive HTML `/app/*` form commands from Android. Those routes belong to the web surface in **suas**.
- **Do not** introduce a mobile test harness in this repository.

iOS already wraps `/api/v0` in `APIClient.swift`. Use that client and the OpenAPI file as the observed sibling pattern. Copy neither secrets nor provider credentials from any sibling.

## Staging host

Synthetic staging is [https://suasqrf.com](https://suasqrf.com). Point a future Android API base URL at that host only for non-production builds. Staging must not use real veteran data or real external support effects.

## Current scaffold status

This tree is a Kotlin Jetpack Compose UI scaffold. It is a fork of [RuntimeSquad/Suas](https://github.com/RuntimeSquad/Suas). Observed today:

- Package and application ID remain `com.example.suas`.
- Home and ride-request pages scroll. Accessibility semantics are present and do not change the visual layout.
- Home shows ride, food, and shelter cards. The former "Tap what you need" section is gone.
- The ride-request form can prefill and edit pickup, destination, ZIP, and timing fields. Submission is explicitly disabled until a real backend contract is connected.
- Food and shelter cards intentionally have no handlers, so they do not imply unavailable fulfillment.
- There is no Retrofit client, session store, or OpenAPI binding. The manifest still declares `INTERNET`, but the scaffold performs no support-request network call.
- JVM tests cover the package baseline. Instrumented Compose tests cover support options, form editing, disabled submission, cancellation/back navigation, and package context on an emulator.

MVP request categories are FOOD, TRANSPORTATION, temporary SHELTER, and PEER_SUPPORT. Do not add medical or VA-treatment claims in the Android UI.

Do not treat the on-screen copy in this scaffold as released crisis copy. D-012 approved wording lives in SUAS-specs.

## Release and production boundary

| Item | Status | What it means here |
| --- | --- | --- |
| **D-033** | Decided. Native client surface released in `MOBILE_SURFACE.md`. | You may implement an Android client of `/api/v0`. |
| **D-034** | Pending. On-device protection of veteran data. | Persist no veteran domain data locally until this decision closes. Hold only the minimum required for a session. |
| **SPEC-018** | Blocked. Pilot and production go/no-go. | Do not ship to real veterans, claim a live pilot, or submit to an application store. |
| Production | `NOT_READY` | Implementation authority is not production authority. |

Device push, social login, contact-list access, continuous location, and long-lived unrevocable credentials remain forbidden on a native client.

## Secrets and compliance

- Never commit secrets, session credentials, `.env` files, provider keys, or real contact details.
- Do not claim HIPAA, SOC 2, ISO, or any other compliance certification from this repository or the app UI.
- Do not put provider credentials in the application bundle.

## Build the scaffold

You need Android Studio (or the Android SDK) with JDK 11 or later.

```bash
./gradlew :app:test
```

Open the project in Android Studio and run the `app` configuration on an emulator or device. The scaffold has no Worker `/api/v0` client and performs no support-request network call.

## What SUAS is not

SUAS is not an EHR, a diagnosis system, a suicide-prediction product, or an automated emergency dispatcher. The client must not auto-dial 911 or 988. Present crisis destinations only after an explicit person-initiated action, using released copy from SUAS-specs.
