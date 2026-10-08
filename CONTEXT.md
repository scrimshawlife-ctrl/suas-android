# CONTEXT.md: SUAS Android client context

Read this before working in this repository. Rules for agents: [AGENTS.md](AGENTS.md). Setup and detail: [README.md](README.md).

## What SUAS is

Shut Up and Serve is a consent-governed veteran support coordination platform.

Mission: coordinate the shortest safe and consented path between a veteran's current need and an available human or material support resource.

Canonical loop:

`SIGNAL → NEED → CONSENT → COORDINATION → FULFILLMENT → FOLLOW-UP → SETTLEMENT`

MVP categories:

- `FOOD`
- `TRANSPORTATION`
- `SHELTER`: temporary shelter/accommodation, not permanent housing
- `PEER_SUPPORT`

## What SUAS is not

- EHR
- diagnosis system
- suicide-prediction product
- automated emergency-dispatch system
- clinical efficacy measurement product
- production billing/Medi-Cal system

The full non-goal list is SUAS-specs [PRODUCT.md](https://github.com/scrimshawlife-ctrl/SUAS-specs/blob/main/PRODUCT.md) section 8.

## The four repositories

| Repository | Role |
| --- | --- |
| [`SUAS-specs`](https://github.com/scrimshawlife-ctrl/SUAS-specs) | Canonical released contract. Specs are authority; implementation gaps return here. |
| [`suas`](https://github.com/scrimshawlife-ctrl/suas) | TypeScript Cloudflare Worker: JSON API `/api/v0`, web `/app`, OpenAPI `docs/openapi/v0.json`. Synthetic STAGING `https://suasqrf.com`. |
| [`suas-ios`](https://github.com/scrimshawlife-ctrl/suas-ios) | Native iOS client (Swift). Consumes `/api/v0`. |
| [`suas-android`](https://github.com/scrimshawlife-ctrl/suas-android) | Native Android client (Kotlin Compose). Consumes `/api/v0`. |

A change to the product API, Veteran journey, auth, or environment class must be considered against all three implementation repositories. Work across all four is tracked on the [SUAS Product Board](https://github.com/users/scrimshawlife-ctrl/projects/6).

## This repository's role

`suas-android` is the native Android Veteran client, a fork of [`RuntimeSquad/Suas`](https://github.com/RuntimeSquad/Suas): Kotlin Jetpack Compose UI plus the Retrofit `/api/v0` client in `com.example.suas.api`. The product launcher is `RootActivity`. It implements the released native client surface in SUAS-specs [MOBILE_SURFACE.md](https://github.com/scrimshawlife-ctrl/SUAS-specs/blob/main/MOBILE_SURFACE.md) (D-033). It does not redefine the product.

## API contract

- Call `/api/v0` only, from the `suas` Worker. No `/api/mobile`, client-type header, or second version selector. Do not drive HTML `/app/*` or wrap `/app` in a WebView.
- Auth: `Authorization: Bearer <credential>`, held in memory by `SessionStore` (D-034). Never written to disk.
- Pinned contract: `suas` [`docs/openapi/v0.json`](https://github.com/scrimshawlife-ctrl/suas/blob/main/docs/openapi/v0.json). Integration detail: SUAS-specs `D033_NATIVE_CLIENT_INTEGRATION.md`, `D033_SIGN_IN_PARITY.md`, `D033_CASE_OPEN.md`, `D033_CHAT_PARITY.md`.
- `ClientConfiguration.validate` pins STAGING `https://suasqrf.com` and spec `0.6.0`, and rejects every PRODUCTION configuration. `applicationId` `com.example.suas` is `PLACEHOLDER_NOT_RELEASED`.

## Demo and local modes (debug builds only)

Debug builds add two launchers next to **Veteran's Passport** (`RootActivity`, STAGING). Both live only in `app/src/debug`, so release builds are unchanged.

| Launcher | Activity | Backend |
| --- | --- | --- |
| Veteran's Passport | `RootActivity` | STAGING `https://suasqrf.com` |
| SUAS Demo (no server) | `DemoRootActivity` | in-memory `DemoSuasApi`, no network |
| SUAS Local Worker | `LocalRootActivity` | `http://10.0.2.2:3000` (`npm run dev:demo` in `suas`) |

- Sign in as `demo@example.invalid` with code `123456`; `newvet@example.invalid` has no case. The synthetic STAGING Worker accepts that account, and its web sign-in page shows the email and code. The Veteran's Passport launcher does not prefill it. TEST and PRODUCTION reject the flag.
- `contract/demo-fixtures.json` and `app/src/debug/resources/demo/demo-fixtures.json` are copies of the `suas` export (`npm run demo:fixtures`). Do not hand-edit them.
- Checks: `bash scripts/forbidden-capabilities.sh` and `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease`.

## Versioning and release

- Version `0.1.0` (`versionName`, `versionCode` 1 in `app/build.gradle.kts`), tagged `v0.1.0`. Implements SUAS-specs `0.6.0`. `ClientPins.APPLICATION_VERSION` must match `versionName` (a unit test checks).
- Stays below 1.0.0 while SPEC-018 is blocked (SUAS-specs `VERSIONING.md` section 8).
- Record changes in [CHANGELOG.md](CHANGELOG.md) and follow [RELEASING.md](RELEASING.md): tag `vX.Y.Z` on `main` only after an approved merge; `.github/workflows/release.yml` creates the GitHub Release. No APK or bundle is published.

## Hard walls

- `/api/v0/dev/*` exists only on a LOCAL Worker (`SUAS_ENV=LOCAL`) and returns 404 on staging `https://suasqrf.com`.
- Do not invent a HIPAA class, percentages, or a production host. There is no production host.
- Blocked until the owner decides: D-034 (on-device protection of Veteran data; clients persist none), D-006 (health-information classification, counsel-owned), SPEC-018 (store distribution and launch, `KEEP_BLOCKED`), production VA verification (D-035 authorizes LOCAL fixture and VA SANDBOX only), and live operations, pilot, or real Veteran data.
- All demo and test data is synthetic: `@example.invalid` emails and 555-0100 to 555-0199 phone numbers.
- iOS keeps Louis's look: service blue `#1C529E`, grouped light home, need cards. Android keeps its SOS-first light home cards.
- Never commit secrets, `.env`, provider credentials, or real contact details.

## Links

- [AGENTS.md](AGENTS.md), [README.md](README.md), [CHANGELOG.md](CHANGELOG.md), [RELEASING.md](RELEASING.md)
- SUAS-specs: [AGENTS.md](https://github.com/scrimshawlife-ctrl/SUAS-specs/blob/main/AGENTS.md), [MOBILE_SURFACE.md](https://github.com/scrimshawlife-ctrl/SUAS-specs/blob/main/MOBILE_SURFACE.md), [STATUS.md](https://github.com/scrimshawlife-ctrl/SUAS-specs/blob/main/STATUS.md), [REPOS.md](https://github.com/scrimshawlife-ctrl/SUAS-specs/blob/main/REPOS.md)
- Board: [SUAS Product Board](https://github.com/users/scrimshawlife-ctrl/projects/6)
- Mac device work (emulator, screenshots, local-runner CI): SUAS-specs [docs/handoffs/MAC_DEVICE_WORK.md](https://github.com/scrimshawlife-ctrl/SUAS-specs/blob/main/docs/handoffs/MAC_DEVICE_WORK.md)
