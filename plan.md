# Monorepo Migration and Maintenance Plan

## Purpose

Consolidate the maintained Smartspacer plugins into one Gradle repository without turning them into one application or coupling their product logic.

## Architecture decisions

- Preserve `:uber` as the existing Uber ride-tracking plugin.
- Add `:ubereats` for Uber Eats food orders and `:transitgo` for Transit GO.
- Place the new standalone app modules under `plugins/ubereats/app` and `plugins/transitgo/app`.
- Keep independent application IDs, namespaces, version codes, settings, resources, notification listeners, release tags, and APKs.
- Keep the existing `:shared` module scoped to `:uber`. Do not share code with the new plugins until there is a tested, stable API.
- Use one root Gradle wrapper, one plugin version catalog/configuration surface, one CI entry point, and per-plugin release workflows.
- Preserve the old repositories and their Git histories as source archives. The migration commit records the imported tree; it does not rewrite the source repositories' commit histories into this repository.

## Audit summary

### Existing monorepo

- The repository already contains the original `:uber`, `:shared`, and `:systemstubs` modules.
- Its Gradle wrapper is already Gradle 9.4.1 and the root Android/Kotlin plugins are already at the versions used by the two standalone apps.
- The existing `:uber` is the upstream-derived ride plugin; it must not be replaced by Uber Eats.

### Transit GO

- Source: `INFORMATION89/Transit-GO-Smartspacer`, branch `fix/privacy-safe-diagnostics`.
- Privacy diagnostics, notification contract, unit test foundation, bitmap bounds, settings extraction, and build/test/lint workflow are already implemented in that branch.
- The parser still depends on Transit-owned `bigContentView` and private RemoteViews IDs. This is inherently fragile and needs device validation after Transit updates.
- Existing tests cover package/channel filtering, live-trip classification, bike wording, notification removal matching, and visible-state comparison. Dedicated listener lifecycle and RemoteViews parsing tests are still missing.
- Bike wording currently recognizes only the tested English and Spanish forms; unknown text should remain unchanged.
- Device validation is required for the actual card dimensions, text clipping, route chip, step icon, live trip updates, and the settings screen.

### Uber Eats

- Source: `INFORMATION89/UberEats-Smartspacer`, branch `main`, version `0.1.1` / versionCode `101`.
- It is a native Kotlin/Compose plugin that reads notifications from `com.ubercab.eats` and `com.ubercab`, and has no Internet permission in its manifest.
- During migration, raw debug logging was removed: notification extra values and the full notification key are no longer logged; parse failures log only the exception type; removal callbacks filter to supported Uber packages.
- Order detection still uses notification categories, flags, channel names, and English/Spanish words to distinguish food orders from rides and promotions. This needs a parser boundary and sanitized fixtures before expanding supported-language claims.
- The settings implementation remains concentrated in a large `SettingsActivity.kt`. Modularize it without redesigning the existing screen.
- No automated test source set was present in the standalone repository. Add tests for parser scoring, source-package filtering, food-versus-ride classification, ETA-only updates, removal/reconnection, and candidate selection.
- The standalone README explicitly states that version 0.1.1 has not yet been validated with a real order. Keep that limitation visible until a real order is tested.

## Findings and priorities

### P1 — Complete the monorepo Gradle migration <sub>Implemented; CI passed</sub>

The root Gradle build now includes the existing `:uber` module plus independent `:ubereats` and `:transitgo` applications under `plugins/`. The root Kotlin Compose compiler plugin is registered centrally. The root workflow runs available tests and lint, builds all three applications, and uploads a separate artifact for each.

Build plugin suite [#3](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/actions/runs/45716316251) completed successfully: tests, lint, all three APK builds, and all three artifact uploads passed. It produced `uber-rides-plugin`, `uber-eats-plugin`, and `transit-go-plugin`.

The source repositories' wrappers and root Gradle files were not copied. The monorepo owns the root wrapper and settings. Follow-up workflow runs check later metadata and release-workflow changes. Central CI is being extended with `:uber:lintRelease`; the original `:uber` module has no imported unit-test source set.

### P1 — Uber Eats notification diagnostics

**Implemented during migration.** Do not restore raw extras values, order text, full notification keys, courier information, or exception stack traces in Logcat.

### P1 — Validate Uber Eats order classification

The Uber app hosts both rides and food orders. Current heuristics may fail as Uber changes channel IDs, notification categories, or wording. Add sanitized fixtures before modifying scoring; avoid treating a ride as a food order.

### P1 — Validate Transit GO private RemoteViews integration

Transit does not provide a public GO trip API, so the plugin reads private notification layout IDs. Missing or renamed views must degrade gracefully. Keep the IDs centralized and verify at least one real Transit build/device after updates.

### P2 — Automated test coverage

- Add Uber Eats parser/state unit tests; currently none exist.
- Extend Transit GO tests to listener lifecycle and parsing boundaries.
- Keep fixtures synthetic and free of real names, routes, locations, orders, photos, or notification keys.

### P2 — Uber Eats settings maintainability

The standalone Uber Eats settings screen is a large Compose file combining lifecycle, screen composition, preview, motion, rows, and theme. Split it into focused files while preserving current behavior and preferences. Do not extract a shared settings library just to avoid duplication before both screens have stabilized.

### P2 — Device and visual validation

Verify settings themes, dynamic colors, long translations, preference persistence, notification-access navigation, gesture interactions, and RemoteViews rendering on physical devices.

### P3 — Shared code and dependency alignment

Only after behavioral parity and ownership are clear, consider sharing Compose UI tokens/components or test utilities. Do not share notification parsers or product-specific settings state. Review dependency version drift as a separate change, not as a migration side effect.

## Acceptance criteria

- `:uber`, `:ubereats`, and `:transitgo` build as separate Android applications from the root project.
- Each plugin retains its original application ID, namespace, version, provider authority, notification listener, preferences, and release identity.
- CI runs tests, lint, and a build for all maintained modules with separate APK artifacts.
- Release workflows are isolated by tag prefix and never upload the wrong plugin.
- No new Internet permission, analytics, telemetry, or shared product-data service is introduced.
- Uber Eats diagnostics no longer reveal raw notification/order content.
- Module READMEs and root README explain installation, build commands, privacy, and version state accurately.
- The original standalone repositories remain available and are not deleted or archived.
- No release tags, version bumps, signing changes, or publications occur without approval.

## Out of scope

- Rewriting the plugins.
- Replacing the original Uber ride plugin with Uber Eats.
- Combining plugin applications into one APK.
- Introducing a cross-plugin runtime dependency or backend.
- Publishing releases as part of the move.


## Audit progress — 2026-11-25

### Confirmed in the migration branch

- Root Gradle settings include `:uber`, `:ubereats`, and `:transitgo`; the two imported apps are mapped to `plugins/<plugin>/app`.
- Root repositories are centralized through `dependencyResolutionManagement`; inspected existing `:uber`, `:shared`, and `:systemstubs` build files do not declare their own repositories.
- The original `:uber` application ID, namespace, version, provider and notification-provider components remain present.
- Uber Eats and Transit GO retain separate application IDs, namespaces, Smartspacer target authorities, notification-listener services, local settings and version metadata.
- The two imported manifests declare package-visibility `<queries>` only; no `<uses-permission>` declaration was found in either imported manifest.
- Notification listeners filter source packages before parsing. Transit GO additionally checks the `go_mode` channel using short-circuit package filtering.
- Uber Eats and Transit GO clear in-memory state on listener disconnection; removal callbacks are scoped to supported source notifications.
- Transit GO diagnostics log structural booleans/flags only. Uber Eats diagnostics log package/channel/category/flags and extra key/type names, not extra values, notification content, or the full StatusBarNotification key.
- Release workflows use distinct tag prefixes and only copy the intended plugin APK. No tag or release has been created as part of this migration.

### CI evidence

- PR workflow run [#3](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/actions/runs/45716316251) completed successfully. Unit tests, lint, the three plugin builds, and all three artifact uploads passed.
- The artifacts were `uber-rides-plugin`, `uber-eats-plugin`, and `transit-go-plugin`.
- The original `:uber` module has no imported unit-test source set. It was built as a debug APK; `:uber:lintRelease` is being added to central CI for consistent static-analysis coverage.

### Remaining audit findings

- Uber Eats still has a large, concentrated `SettingsActivity.kt`; refactor it in a separate behavior-preserving task after migration validation.
- Uber Eats has no imported unit-test source set. Its notification scoring, Uber-food-vs-ride classification, ETA-only updates, candidate selection and listener lifecycle need synthetic tests.
- Uber Eats' in-memory candidate map and remembered Uber food notification keys should be reviewed for bounded growth and stale-candidate behavior under repeated notification updates.
- Transit GO relies on private Transit RemoteViews layouts and resource IDs. This remains inherently fragile and requires real-device validation after Transit app updates.
- Both apps require real notification/device checks; successful CI does not validate live order/trip behavior, RemoteViews rendering, screen density, settings gestures, or preference persistence.
- New `plugin.json` download URLs are intended for future monorepo releases. Do not treat the new plugins as published until their respective release tags and signed APK assets exist.

### Migration documentation correction

The root `continue.md` now reflects that Gradle module wiring and the initial CI workflow are already present. Its remaining steps focus on final CI/artifact verification, audit completion, documentation accuracy and device validation.
