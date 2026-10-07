# Releasing the SUAS Android client

The app uses [Semantic Versioning](https://semver.org/) and stays below 1.0.0
while SPEC-018 (launch and store distribution) is blocked. A version or tag is
not permission to publish to a store or serve real Veterans.

The app version is separate from the SUAS-specs stack version. Every release
states the stack it implements, for example "Implements SUAS-specs 0.6.0"
(SUAS-specs `VERSIONING.md` sections 3 and 8). The spec pin lives in
`ClientPins.SPEC_VERSION`.

## Version rules (pre-1.0)

- MINOR (`0.x.0`): new screens or behavior, or a change in how the app uses `/api/v0`.
- PATCH (`0.x.y`): fixes and docs only.
- `versionCode` goes up by one for every build handed to anyone.

## Steps

1. On the branch, set the new version in both places (a unit test checks they match):
   - `app/build.gradle.kts`: `versionName = "0.2.0"` and `versionCode = 2`
   - `app/src/main/java/com/example/suas/api/ClientConfiguration.kt`: `APPLICATION_VERSION = "0.2.0"`
2. In `CHANGELOG.md`, move `[Unreleased]` entries under `## [0.2.0] - YYYY-MM-DD`,
   keep the "Implements SUAS-specs x.y.z" line, and update the links at the bottom.
3. Run the checks in README "Checks", open the PR, and get CI green. Merging needs Danny's yes.
4. After the merge, tag the merge commit on `main` and push the tag:

   ```bash
   git checkout main && git pull
   git tag -a v0.2.0 -m "suas-android v0.2.0 (implements SUAS-specs 0.6.0)"
   git push origin v0.2.0
   ```

5. The `release` workflow (`.github/workflows/release.yml`) creates the GitHub
   Release from the tag using that version's `CHANGELOG.md` section. No APK or
   bundle is uploaded and nothing is published to a store.

Before tagging, change the `0.2.0` heading date from "Unreleased" to the real date.
