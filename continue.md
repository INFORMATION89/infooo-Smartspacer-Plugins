# Continue development

Read [AGENTS.md](AGENTS.md), [rules.md](rules.md), [plan.md](plan.md), and [CLAUDE.md](CLAUDE.md) before changing code. For plugin-specific work, also read that plugin's local instructions and handoff.

## Current migration state

- Repository: `INFORMATION89/infooo-Smartspacer-Plugins`
- PR: [#2 — Move Uber Eats and Transit GO into the Smartspacer plugin monorepo](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/pull/2)
- Working branch: `migration/add-ubereats-transitgo-monorepo`
- Base: `main`
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

Verified monorepo workflow: [Build plugin suite #3](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/actions/runs/45716316251).

- Unit tests for `:ubereats` and `:transitgo`: passed.
- Android Lint for `:ubereats` and `:transitgo`: passed.
- Build step for `:uber:assembleDebug`, `:ubereats:assembleRelease`, and `:transitgo:assembleRelease`: passed.
- All three artifact uploads passed: `uber-rides-plugin`, `uber-eats-plugin`, and `transit-go-plugin`.
- Workflow conclusion: `success`.

The run validated all three app builds and the root Gradle layout. Later commits mainly adjusted registry metadata, release workflow behavior, and documentation. A final follow-up run is checking those changes. The original `:uber` module has no unit-test source set; `:uber:lintRelease` is being added to central CI for consistent lint coverage.

## Remaining audit work

1. Confirm the follow-up CI run, including `:uber:lintRelease`, succeeds.
2. Add sanitized parser/state tests for Uber Eats: food orders from both Uber apps, ride/promotion rejection, ETA-only updates, candidate selection and stale-state cleanup.
3. Extend Transit GO tests for listener disconnection/rebinding and RemoteViews parsing fallbacks.
4. Consider tests for the original `:uber` module; no `src/test` or `src/androidTest` source set was imported for it.
5. Complete physical-device checks for both imported plugins: real order/trip lifecycle, settings persistence, localization, and display densities.
6. Review Uber Eats' in-memory candidate map and remembered notification keys for bounded growth and stale-candidate behavior.

## Device validation still required

- Uber Eats: real food-order notifications from both Uber Eats and the Uber app; ETA-only updates; ride/promotion rejection; notification removal/reconnection; courier photo behavior; settings persistence.
- Transit GO: active GO trip updates; route chip and step-icon variants; trip completion; listener disconnection; settings appearance/interactions.
- Both: verify layout and localization on physical devices, including long text and different screen densities.

## Safety boundaries

- Do not change app versions, application IDs, namespaces, signing identity, release secrets or release assets.
- Do not push tags or publish releases.
- Do not merge PR #2 or delete/archive the source repositories without explicit approval.
- Treat CI as automated validation only; it does not replace real-device testing.
