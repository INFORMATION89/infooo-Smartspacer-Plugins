# Continue development

Read [AGENTS.md](AGENTS.md), [rules.md](rules.md), [plan.md](plan.md), and [CLAUDE.md](CLAUDE.md) before changing code. For plugin-specific work, also read that plugin's local instructions and handoff.

## Current migration state

- Repository: `INFORMATION89/infooo-Smartspacer-Plugins`
- PR: [#2 — Move Uber Eats and Transit GO into the Smartspacer plugin monorepo](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/pull/2)
- Merged on 2026-11-25 into `main`.
- Merge commit: `51d0550b0d29bab3376cccee96949f730c76591e`.
- CI suite #21 passed tests, lint, all three APK builds, and all three artifact uploads.
- Preserve both original standalone repositories. Do not archive or delete them without explicit authorization.
- Remaining migration follow-up: update the source repositories' remotes/workflows to point to their monorepo paths, then decide separately whether to close those repositories.

## Architecture

- `:uber` remains the Uber rides plugin.
- `:ubereats` is the separate food-order app at `plugins/ubereats/app`.
- `:transitgo` is the separate Transit GO app at `plugins/transitgo/app`.
- `:shared` and `:systemstubs` remain part of the existing Uber rides implementation.
- The root Gradle settings include all five projects. The imported applications keep separate IDs, namespaces, provider authorities, notification listeners, preferences, resources, versions, and release workflows. There are no runtime dependencies between the three plugins.

## Migration and audit work

- Imported Transit GO from `INFORMATION89/Transit-GO-Smartspacer`, branch `fix/privacy-safe-diagnostics`, and Uber Eats from `INFORMATION89/UberEats-Smartspacer`, branch `main`.
- Centralized Gradle configuration and added root CI for tests, lint, three independent app builds, and three separate APK artifacts.
- CI invokes the unit-test task for all three apps. Uber Eats and Transit GO have unit tests; the existing Uber rides module has no imported test source set.
- Added an explicit synthetic regression case that rejects mixed food-and-ride wording from Uber's shared notification feed.
- Reviewed manifests, IDs, authorities, dependency boundaries, notification filtering/removal/disconnection, diagnostics, release tag scoping, APK output paths, and migration provenance.
- No release tags or releases have been created. The imported code is not device-validated: real order/trip notifications, Transit private RemoteViews, rendering, settings persistence, localization, and screen densities still need physical-device checks.

## Validation

The root workflow runs:

- `:uber:testDebugUnitTest`, `:ubereats:testDebugUnitTest`, and `:transitgo:testDebugUnitTest`.
- Release lint for all three modules.
- `:uber:assembleDebug`, `:ubereats:assembleRelease`, and `:transitgo:assembleRelease`.
- A distinct artifact upload for each app.

Final pre-merge validation is recorded in [Build plugin suite #21](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/actions/runs/45719944978), which passed the expanded test tasks, lint, all three builds, and all three artifact uploads. A CI pass does not replace the device checks listed above. Documentation-only changes are excluded from the full Android workflow; source, build configuration, shared settings, and workflow changes continue to trigger it.

## Remaining device checks

- Uber Eats: live food orders from both Uber apps, ETA-only updates, ride/promotion rejection, removal/reconnection, courier photo behavior, and settings persistence.
- Transit GO: active trip updates, route-chip/step-icon variants, trip completion, listener disconnection, and settings appearance/interactions.
- Both apps: localization, long text, and display-density/layout checks on physical devices.

## Release and source-repository boundaries

- Preserve current application IDs, namespaces, signing identity, release secrets, and individual versions.
- Do not push tags or publish releases.
- Do not archive or delete the source repositories.
- The remaining migration follow-up is to update local clones to use the monorepo and its `plugins/ubereats/app` or `plugins/transitgo/app` path, update source repository READMEs/workflows to point to the monorepo, and decide separately how to close the originals.

## Active work — PR #6

- Draft PR: [Unify expressive settings and render real Smartspacer previews](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/pull/6).
- The shared `:settings-ui` implementation for Uber Eats and Transit GO is on the PR branch, not merged to `main`.
- Uber ride settings now use the shared Compose/Material 3 Expressive screen and preserve per-Target-instance configuration.
- Uber's card now displays a percentage derived from the existing notification progress value. Focused tests cover null, non-finite, boundary, rounding, and out-of-range formatting.
- The Uber preview uses the official Smartspacer renderer, live target data when present, and a labelled synthetic image otherwise. Preview targets omit real actions and notification metadata.
- The progress-only revision passed the full plugin suite (#38). Two later settings-screen CI attempts failed on unavailable rounded icon references; those references were corrected. A final consolidated validation run remains pending approval.
- Physical-device, live-notification, and visual preview checks remain unverified. Keep PR #6 in draft; do not merge or publish without explicit authorization.
