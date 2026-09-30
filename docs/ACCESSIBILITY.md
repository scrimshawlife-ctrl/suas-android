# Accessibility residual — suas-android

`MOBILE_SURFACE.md` §7 maps the client to WCAG 2.2 AA. This file does not claim that conformance.

## Automated in this change

- The launcher heading uses a heading semantic.
- Crisis actions use a 48dp minimum height.
- Build info is one merged accessibility node and contains no credential or veteran free text.

## Manual device checks still required

```text
MOB-011 = DEVICE_ACCEPTANCE_REQUIRED
```

TalkBack, largest font scale, and contrast still need a device pass on sign-in, the launcher, request forms, and the crisis actions. Platform defaults are not a WCAG 2.2 AA result.
