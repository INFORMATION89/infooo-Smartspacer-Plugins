# Continue development

Read the root [AGENTS.md](../../AGENTS.md), [rules.md](../../rules.md), [CLAUDE.md](../../CLAUDE.md), and [docs/ARCHITECTURE.md](../../docs/ARCHITECTURE.md), then this module's [AGENTS.md](AGENTS.md), [rules.md](rules.md), [plan.md](plan.md), and [CLAUDE.md](CLAUDE.md).

## Current state

- Repository: `INFORMATION89/infooo-Smartspacer-Plugins`
- Module: `:transitgo` at `plugins/transitgo/app`
- Application ID: `dev.marco.smartspacer.transitgo`
- Version: `1.3.1` (`versionCode = 10301`).
- Migration PR [#2](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/pull/2) is merged into main.
- Latest verified main suite before repository hardening: [Build plugin suite #45721585024](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/actions/runs/45721585024) passed tests, lint, all three APK builds, and all three artifact uploads.
- Shared imported-app dependencies are declared through the root `gradle/libs.versions.toml`; keep Transit-only behavior and dependencies owned by this module.
- The in-app Source code action now points to this module's monorepo directory.
- Preserve the current version and permanent signing identity. No release tag or new release was created by the migration.

## Implemented safeguards and structure

- The listener filters to the Transit GO package and `go_mode` channel before reading notification content.
- Live-trip classification and selected state comparisons are separated into `TransitGoLogic.kt` and covered by unit tests.
- `TransitNotificationContract.kt` centralizes the package/channel, observed private RemoteViews IDs, inset, and bitmap edge limit.
- `ChipRenderer` handles missing/incorrect views defensively, caps rendered bitmaps at 512 px, and keeps diagnostics structural.
- Automatic notification layout dumps were removed; raw trip content is not written to normal Logcat diagnostics.
- Settings responsibilities are already split across Activity, screen, preview, components, motion, theme, and token files. Do not perform another broad UI refactor.
- The monorepo contains no runtime dependency on Uber Eats or Uber Rides.

## Remaining work

1. Extend tests for listener connection/disconnection, notification removal, state clearing, and updates that refresh only the tap action.
2. Introduce a testable parsing boundary for RemoteViews and cover missing, renamed, hidden, or unexpected view types where feasible.
3. Validate active trips, route chip/step icon variants, trip completion, and lock-screen behavior on a real Transit installation.
4. Verify card rendering on multiple screen widths/densities and test settings persistence, localization, long labels, and notification-access revocation/reconnection.
5. Re-check the RemoteViews assumptions after Transit app updates; its private view IDs are not a stable public API.

## Validation

From the repository root:

```bash
./gradlew :transitgo:testDebugUnitTest :transitgo:lintRelease :transitgo:assembleRelease
```

CI success does not validate private Transit RemoteViews, live trip updates, rendering, or settings interactions on physical devices. Record the Transit app version, Android version, device, and density for device checks.

## Release boundary

Do not bump the version, push a tag, change signing configuration, update release metadata for publication, or publish a GitHub release without explicit approval.
