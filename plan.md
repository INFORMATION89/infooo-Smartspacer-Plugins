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

### P1 — Complete the monorepo Gradle migration

Root Gradle configuration and CI must build each independent app. Ensure project directories, Kotlin Compose plugin registration, release task paths, artifact names, and tag filters are correct. A CI success in a standalone repository does not prove the new monorepo build works.

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
