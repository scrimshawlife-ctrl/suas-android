# Agent rules for SUAS Android

You are in **one of three** SUAS implementation surfaces. This clone is Android only.

## Sibling inventory

| Repo | What it is |
| --- | --- |
| [scrimshawlife-ctrl/suas](https://github.com/scrimshawlife-ctrl/suas) | Web and product API. OpenAPI at `docs/openapi/v0.json`. |
| [scrimshawlife-ctrl/suas-ios](https://github.com/scrimshawlife-ctrl/suas-ios) | Swift client. Same `/api/v0` paths. |
| [scrimshawlife-ctrl/SUAS-specs](https://github.com/scrimshawlife-ctrl/SUAS-specs) | Canonical specs. Start with `MOBILE_SURFACE.md` and `D033_SIGN_IN_PARITY.md`. |
| This repo | Kotlin Compose UI plus `com.example.suas.api`. |

## API rule

Speak `/api/v0` only.

- Sign-in: `POST /api/v0/auth/challenges` then `POST /api/v0/auth/challenges/commands/verify`.
- Session: `Authorization: Bearer …` held in memory (`SessionStore`). Do not write it to disk (D-034).
- Open a Case: `POST /api/v0/cases` with `Idempotency-Key`. Never `POST /app/qrf/deploy`.
- Logout: `POST /api/v0/auth/sessions/commands/logout`.
- Staging host: `https://suasqrf.com`.
- Do not add `/api/mobile` or `/api/v0/dev/*`.
- Do not wrap `/app` in a WebView.

JSON auth still sends `tenant_id` on the wire. Use `Backend.SYNTHETIC_TENANT_ID`. That is a build pin, not a person-facing picker.

Chat and dashboard numbers stay unavailable / not computable. Do not print “dispatched now” or lives-saved totals.

## Preflight pins

SPEC-018 stays `KEEP_BLOCKED`. Do not flip a readiness gate.

- Environment class is explicit (`LOCAL`, `TEST`, `STAGING`, `PRODUCTION`). Do not infer it from the URL or from debug/release. `ClientConfiguration.validate` rejects an unknown class, a spec or manifest mismatch, real external effects, `STAGING` on any host other than `suasqrf.com`, and every `PRODUCTION` configuration. There is no production host.
- `applicationId` `com.example.suas` is `PLACEHOLDER_NOT_RELEASED`. Do not invent a store id.
- `MainActivity` is a debug-only harness. It is not in the main manifest and is not exported. The product launcher is `RootActivity`.
- Backup and device transfer are explicitly off. Do not persist a bearer, request history, or veteran free text. Do not add Keystore storage for the session.
- A logical submission owns its idempotency keys in `SubmissionAttempt`. A retry reuses them. Do not mint a new key inside an API call.
- Crisis copy stays the released 911 / 988 sentences. Person-initiated dialer or SMS only.
- Release assembly may use the builder’s debug keystore. That is not a store credential. Do not commit signing secrets.
