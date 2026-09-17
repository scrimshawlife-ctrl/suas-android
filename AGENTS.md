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

JSON auth still requires `tenant_id` on the wire. Use `Backend.SYNTHETIC_TENANT_ID`. That is a build pin, not a person-facing picker.

Chat and dashboard numbers stay unavailable / not computable. Do not print “dispatched now” or lives-saved totals.
