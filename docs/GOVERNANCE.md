# Repository governance — suas-android

This file records observed settings. It does not apply them.

```text
SPEC_018 = KEEP_BLOCKED
STORE_DISTRIBUTION = PROHIBITED
APPLICATION_ID = PLACEHOLDER_NOT_RELEASED
APPLICATION_ID_OBSERVED = com.example.suas
BRANCH_PROTECTION = OPERATOR_ACTION_REQUIRED
DEVICE_ACCEPTANCE_REQUIRED = true
```

## Observed on 2026-09-30

The branch started unprotected (HTTP 404). These settings were then applied:

```text
pull_request_required: true
conversation_resolution_required: true
force_push_blocked: true
deletion_blocked: true
required_approving_review_count: 0
enforce_admins: false
required_status_checks: not set
```

`enforce_admins` is false, so an admin can bypass. Required status checks are not set yet, because the `contract` job did not exist on `main` when protection was applied. Adding the wrong check name would block every pull request.

SUAS-specs does not name a production `applicationId`. `com.example.suas` stays in place and is labeled a placeholder. Inventing a package name would be a product decision.

`MainActivity` is in `src/debug` and is not exported. It is absent from the release manifest.

`android:allowBackup="false"`, with cloud-backup and device-transfer excludes. The session bearer stays in `SessionStore` memory. This is the D-034 `ACCEPT_MEMORY_ONLY_DEFAULT` rule, not a claim that on-device cryptography is decided.

Release optimization is enabled. Store signing keys are not in this repository. `assembleRelease` is not a Play upload.

## Required settings once CI has run on main

```text
main:
  pull_request_required: true
  required_status_checks: true
  contexts:
    - contract
  conversation_resolution_required: true
  force_push_blocked: true
  deletion_blocked: true
```

After `contract` has completed on `main`, add it as a required check. Until that update, a pull request can merge without CI. That remaining step is `OPERATOR_ACTION_REQUIRED`.

Instrumented emulator acceptance is not in CI. Record `DEVICE_ACCEPTANCE_REQUIRED`.
