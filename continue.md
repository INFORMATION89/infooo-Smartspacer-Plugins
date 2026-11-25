# Continue development

Read [AGENTS.md](AGENTS.md), [rules.md](rules.md), [plan.md](plan.md), and [CLAUDE.md](CLAUDE.md) before changing code. For plugin-specific work, also read that plugin's local instructions and handoff.

## Current migration state

- Repository: `INFORMATION89/infooo-Smartspacer-Plugins`
- PR: [#2 — Move Uber Eats and Transit GO into the Smartspacer plugin monorepo](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/pull/2)
- Working branch: `migration/add-ubereats-transitgo-monorepo`
- Base: `main`
- PR head at last audit: `00a36b86127c9d0c9f0345971c46e979289d7616`
- The PR is open, mergeable, and Draft. Do not merge or mark ready without explicit approval.
- Original standalone repositories remain intact. Do not archive or delete them.

## Architecture

- `:uber` is the existing Uber ride-tracking plugin. Preserve it.
- `:ubereats` is the separate food-order plugin at `plugins/ubereats/app`.
- `:transitgo` is the separate Transit GO plugin at `plugins/transitgo/app`.
- `:shared` and `:systemstubs` remain part of the existing Uber rides implementation.
- Root `settings.gradle` already includes all five Gradle projects and maps the two new app modules to their nested directories.
- Each app keeps its own application ID, namespace, provider authority, notification listener, preferences, resources, version and release workflow.
- No runtime dependency between the three applications has been introduced.

## Work completed in the migration branch

- Imported current Transit GO from `INFORMATION89/Transit-GO-Smartspacer`, branch `fix/privacy-safe-diagnostics`, including its privacy changes, tests and extracted settings UI.
- Imported Uber Eats from `INFORMATION89/UberEats-Smartspacer`, branch `main`.
- Sanitized Uber Eats debug diagnostics: raw extras values and full notification keys are not logged; parser failures log only the exception type; notification removal filters to supported source packages.
- Centralized root Gradle plugin versions and registered the Kotlin Compose compiler plugin.
- Added a root CI workflow for unit tests, Android Lint and independent APK builds.
- Added separate Uber Eats and Transit GO tag-triggered release workflows. The existing Uber rides release workflow remains separate.
- Added root and plugin-specific READMEs, AI instructions, rules, plans, handoffs, plugin metadata and migration provenance.

## CI validation

PR workflow run: [Build plugin suite #6](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/actions/runs/45716970751)

Last observed state:
- `:ubereats:testDebugUnitTest` and `:transitgo:testDebugUnitTest`: passed.
- `:ubereats:lintRelease` and `:transitgo:lintRelease`: passed.
- Build step for `:uber:assembleDebug`, `:ubereats:assembleRelease`, and `:transitgo:assembleRelease`: in progress at last check.
- Artifact uploads and final workflow conclusion: not yet confirmed.

Do not describe the monorepo CI as fully green until the build step, all three artifact uploads, and the workflow conclusion succeed.

## Next audit steps

1. Confirm the current CI run reaches success and verify all three separate APK artifacts exist.
2. Review the complete Gradle configuration and confirm the existing `:uber` build output path remains valid.
3. Run/consider lint coverage for the original `:uber` module; current root CI runs lint only for `:ubereats` and `:transitgo`.
4. Audit manifests, application IDs, namespaces, provider authorities, listener components, package visibility queries and permissions for all three apps.
5. Audit notification parsing, lifecycle, stale-state handling, logging and in-memory data handling.
6. Review release workflow signing and tag isolation without accessing or exposing signing secrets and without creating tags/releases.
7. Verify README, plugin metadata, version labels, download URLs and source provenance.
8. Keep the remaining technical debt visible: Uber Eats parser tests and settings modularization; Transit GO listener/RemoteViews edge-case tests; physical-device validation for both plugins.
9. Update this file and [plan.md](plan.md) with exact run IDs, results and any fixes.

## Device validation still required

- Uber Eats: real food-order notifications from both Uber Eats and the Uber app; ETA-only updates; ride/promotion rejection; notification removal/reconnection; courier photo behavior; settings persistence.
- Transit GO: active GO trip updates; route chip and step-icon variants; trip completion; listener disconnection; settings appearance/interactions.
- Both: verify layout and localization on physical devices, including long text and different screen densities.

## Safety boundaries

- Do not change app versions, application IDs, namespaces, signing identity, release secrets or release assets.
- Do not push tags or publish releases.
- Do not merge PR #2 or delete/archive the source repositories without explicit approval.
- Treat CI as automated validation only; it does not replace real-device testing.
