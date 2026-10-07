# Changelog

All notable changes to the SUAS Android client are recorded here. The format
follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the app
uses [Semantic Versioning](https://semver.org/) while it stays pre-1.0.

The app version (`versionName` / `versionCode` in `app/build.gradle.kts`, and
`ClientPins.APPLICATION_VERSION`) is not the SUAS-specs stack version. Each
release states the stack it implements. Versions below 1.0.0 mean no store
listing or launch: SPEC-018 is blocked. Release steps: [RELEASING.md](RELEASING.md).

## [Unreleased]

## [0.1.0] - Unreleased

Implements SUAS-specs `0.6.0`. First versioned release of this fork. The
previous `versionName` was `1.0`, a template default that wrongly implied a
launch; no `1.0` was ever released.

### Added

- No-server demo mode in the debug source set: launcher "SUAS Demo (no server)"
  (`DemoRootActivity`) backed by the in-memory `DemoSuasApi` and the demo
  fixture copied from the **suas** export
  ([#13](https://github.com/scrimshawlife-ctrl/suas-android/pull/13)).
- Debug launcher "SUAS Local Worker" (`LocalRootActivity`) for the real client
  against `npm run dev:demo` on `http://10.0.2.2:3000` (#13).
- Demo sign-in `demo@example.invalid` with code `123456`; `newvet@example.invalid`
  has no case (#13).
- Unit tests for the demo repository and demo journey (#13).
- This changelog, [RELEASING.md](RELEASING.md), and a tag-triggered release
  workflow.

### Changed

- `versionName` `1.0` to `0.1.0` (`versionCode` stays 1).
- README points at SUAS-specs for the `/api/v0` contract instead of restating it.

[Unreleased]: https://github.com/scrimshawlife-ctrl/suas-android/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/scrimshawlife-ctrl/suas-android/releases/tag/v0.1.0
