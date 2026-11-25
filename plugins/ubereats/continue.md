# Continue development

Read the root [AGENTS.md](../../AGENTS.md), [rules.md](../../rules.md), [CLAUDE.md](../../CLAUDE.md), and [docs/ARCHITECTURE.md](../../docs/ARCHITECTURE.md), then this module's [AGENTS.md](AGENTS.md), [rules.md](rules.md), [plan.md](plan.md), and [CLAUDE.md](CLAUDE.md).

## Current state

- Repository: `INFORMATION89/infooo-Smartspacer-Plugins`
- Module: `:ubereats` at `plugins/ubereats/app`
- Application ID: `dev.marco.smartspacer.ubereats`
- Version: `0.1.1` (`versionCode = 101`, alpha).
- Migration PR [#2](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/pull/2) is merged into main.
- Latest verified main suite before repository hardening: [Build plugin suite #45721585024](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/actions/runs/45721585024) passed tests, lint, all three APK builds, and all three artifact uploads.
- Shared imported-app dependencies are declared through the root `gradle/libs.versions.toml`; keep product-specific dependencies and application settings owned by this module.
- The in-app Source code link now points to this module's monorepo directory.
- Preserve the current version and permanent signing identity. No release tag or new release was created by the migration.

## Implemented safeguards

- Notification ingestion is filtered to `com.ubercab.eats` and `com.ubercab` before reading notification content.
- Uber app food-order classification is isolated in a pure Kotlin classifier and covered by synthetic tests for food wording/channel signals and ride rejection.
- Candidate state is capped at 16 entries and expires after 12 hours.
- Remembered Uber food notification keys are capped at 32 and expire after 12 hours.
- Debug diagnostics do not log raw notification text, extra values, full notification keys, courier details, or exception stack traces.
- Listener disconnection clears in-memory order state.

## Remaining work

1. Add Android notification adapter/state tests for ETA-only updates, candidate selection, source package filtering, state comparison, removal, and listener reconnection.
2. Refactor the large `SettingsActivity.kt` into focused Activity, screen, preview, components, motion, theme, and token files. Preserve the current visual behavior, dynamic colors, Google Sans typography, localized resources, preference keys/defaults, and Source code gesture.
3. Add explicit bitmap size/resource limits for the courier photo and verify Binder/card behavior on device.
4. Validate real food orders from both Uber Eats and the Uber app. Confirm ordinary rides/promotions are ignored, ETA updates work, tapping opens the correct order, and revoking notification access clears the card.

## Validation

From the repository root:

```bash
./gradlew :ubereats:testDebugUnitTest :ubereats:lintRelease :ubereats:assembleRelease
```

CI success does not validate real Uber notifications, courier image rendering, settings gestures, or preference persistence on physical devices. Record those checks separately.

## Release boundary

Do not bump the version, push a tag, change signing configuration, update release metadata for publication, or publish a GitHub release without explicit approval.
