# Continue development

Read [AGENTS.md](AGENTS.md), [rules.md](rules.md), [CLAUDE.md](CLAUDE.md), [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md), and [plan.md](plan.md) before changing the repository.

## Baseline

- Repository: `INFORMATION89/infooo-Smartspacer-Plugins`
- Current main baseline before this hardening work: `32f95a9d6672d3569d2027c8298fc9e058f59a3a` (2026-11-25).
- Migration PR [#2](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/pull/2) is merged. Merge commit: `51d0550b0d29bab3376cccee96949f730c76591e`.
- Latest verified main build before this branch: [Build plugin suite #45721585024](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/actions/runs/45721585024) passed unit tests, Android Lint, three APK builds, and three artifact uploads.
- Preserve the original Uber Eats and Transit GO repositories; do not archive or delete them without explicit authorization.
- No plugin version, application ID, signing identity, permission, or release tag should change as part of repository hardening.

## Architecture

- `:uber` is Uber rides and continues to own `:shared` and `:systemstubs`.
- `:ubereats` is the separate food-order app at `plugins/ubereats/app`.
- `:transitgo` is the separate Transit GO app at `plugins/transitgo/app`.
- All three applications keep independent application IDs, namespaces, providers, notification listeners, preferences, version streams, and APKs.
- There are no runtime dependencies between plugins.
- Shared plugin and dependency versions are declared in `gradle/libs.versions.toml`; the three tag workflows now delegate to one reusable release workflow.

## Phase 1 — Repository hardening

- Update root and plugin handoffs to reflect the merged monorepo and actual CI state.
- Point the in-app Source code links to the new plugin folders in this repository.
- Document application ownership, shared-code boundaries, and release flow in `docs/ARCHITECTURE.md`.
- Define the recommended `main` branch protection policy in `.github/BRANCH_PROTECTION.md`. The actual GitHub setting still needs to be applied by a repository administrator.
- Branch review: `add-transit-go` contains six commits not in main and must be preserved; `migration/add-ubereats-transitgo-monorepo` and `transit-go-updates` are fully behind main and are candidates for cleanup after final confirmation.

## Phase 2 — Build and release infrastructure

- Add a shared Gradle version catalog and migrate the duplicate dependencies of Uber Eats and Transit GO to it.
- Centralize root plugin versions in the same catalog.
- Add weekly Dependabot proposals for Gradle and GitHub Actions.
- Replace duplicate release workflow implementations with thin per-plugin callers and one reusable workflow.
- The reusable workflow must validate the tag against module `versionName`, run the module's tests and lint, preserve current signing modes, and publish exactly one plugin-specific APK.

## Remaining validation and follow-up

- Validate this branch with the root Build plugin suite. Do not treat a passing CI run as physical-device validation.
- The original `:uber` module still has no imported unit-test source set.
- Uber Eats still needs Android notification adapter/state tests, Settings UI modularization, and real-order testing.
- Transit GO still needs listener lifecycle and RemoteViews parsing tests plus real Transit device validation.
- Physical device checks for all apps include settings persistence, localized/long text, screen density, and notification-access revocation/reconnection.
- Main protection and stale branch deletion require repository settings/ref changes outside the files in this plan.

## Release boundary

Do not change versions, push tags, publish releases, change signing keys, or modify release assets without explicit approval. See [CLAUDE.md](CLAUDE.md).
