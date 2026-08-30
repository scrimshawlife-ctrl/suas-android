# Agent rules for SUAS Android

You are in **one of three** SUAS implementation surfaces. This clone is Android only.

## Sibling inventory

Name these repositories before you invent architecture:

| Repo | What it is |
| --- | --- |
| [scrimshawlife-ctrl/suas](https://github.com/scrimshawlife-ctrl/suas) | Web and product API. OpenAPI at `docs/openapi/v0.json`. |
| [scrimshawlife-ctrl/suas-ios](https://github.com/scrimshawlife-ctrl/suas-ios) | Private Swift client with a handwritten `/api/v0` `APIClient`. |
| [scrimshawlife-ctrl/SUAS-specs](https://github.com/scrimshawlife-ctrl/SUAS-specs) | Canonical specs. Native contract: `MOBILE_SURFACE.md` (**D-033**). |
| This repo (`suas-android`) | Kotlin Compose scaffold. **No API client yet.** |

Keep **suas**, **suas-ios**, and **suas-android** in future considerations. Do not drop a sibling because it is not in this working tree.

## API rule

The Android app **must speak `/api/v0`**.

- Auth is an opaque Bearer session.
- Staging host: `https://suasqrf.com`.
- Do not add `/api/mobile`.
- Do not call HTML `/app/*` commands.
- Do not add a mobile harness.

Read the start-here guide in [README.md](README.md). Semantic gaps return to **SUAS-specs**. Do not invent product behavior from this scaffold.

## Blocked and pending

- **D-033:** native client surface is released for implementation, not for production.
- **D-034:** pending. Persist no veteran domain data on device.
- **SPEC-018:** blocked. No real-veteran use, no store distribution, no production claim.

## Hard stops

- Never commit secrets or real veteran data.
- Do not claim HIPAA or any other compliance certification.
- No automated emergency dispatch, diagnosis, or suicidality determination.
- No provider credentials on the device.
- No device push, social login, contact-list access, or continuous location.
