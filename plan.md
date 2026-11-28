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

Build plugin suite [#10](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/actions/runs/45717957032) completed successfully: unit tests, lint for all three apps, all APK builds, and all three artifact uploads passed. It produced `uber-rides-plugin`, `uber-eats-plugin`, and `transit-go-plugin`.

The source repositories' wrappers and root Gradle files were not copied. The monorepo owns the root wrapper and settings. The newest code revision adds pure-Kotlin Uber Eats classification/scoring tests; Build suite #12 attempt 2 has passed tests and is still running lint/build. The original `:uber` module has no imported unit-test source set.

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
- The original `:uber` module has no imported unit-test source set. The final pre-merge workflow invoked its test task, ran release lint for all three apps, built all three APKs, and uploaded each artifact. Build plugin suite #21 passed; see the merge-completion record below.

### Remaining audit findings

- Uber Eats still has a large, concentrated `SettingsActivity.kt`; refactor it in a separate behavior-preserving task after migration validation.
- Uber Eats now has pure-Kotlin tests for bounded notification-key storage, food-channel/text classification, English/Spanish wording, mixed food-and-ride rejection, and current score weights. Android notification event integration (ETA-only updates, candidate selection, removals/reconnection) still needs synthetic adapter/state tests.
- **Addressed in the migration branch:** Uber Eats candidates are capped at 16; remembered food-order keys are capped at 32; both expire after 12 hours. Tests cover remembered-key expiry, capacity, refresh, removal and clearing.
- Transit GO relies on private Transit RemoteViews layouts and resource IDs. This remains inherently fragile and requires real-device validation after Transit app updates.
- Both apps require real notification/device checks; successful CI does not validate live order/trip behavior, RemoteViews rendering, screen density, settings gestures, or preference persistence.
- New `plugin.json` download URLs are intended for future monorepo releases. Do not treat the new plugins as published until their respective release tags and signed APK assets exist.

### Migration documentation correction

The root `continue.md` now reflects that Gradle module wiring and the initial CI workflow are already present. Its remaining steps focus on final CI/artifact verification, audit completion, documentation accuracy and device validation.


## Audit update — 2026-11-25

- Before merge, PR #2 was open and mergeable. Its final pre-merge head was exactly up to date with `main` (21 commits ahead, 0 behind).
- No PR comments, submitted reviews, or inline review threads were present at audit time.
- Added `:uber:testDebugUnitTest` to the root test step so CI invokes tests for all three Android apps. Only `:ubereats` and `:transitgo` currently contain unit-test source sets; `:uber` has no tests to execute.
- Added a synthetic Uber Eats classifier regression for mixed food and ride wording from the Uber app.
- Extended workflow path filters to run the suite for root and per-plugin documentation changes, keeping the final handoff/plan update covered by CI.
- Static review confirmed distinct application IDs, provider authorities, notification listener components, and version metadata for all three apps; imported manifests contain only package-visibility queries and no Internet permission. No cross-plugin runtime dependency was found.
- Uber Eats filters by supported source package before parsing and scopes removal callbacks; Transit GO filters by package and `go_mode` channel before parsing. Both clear in-memory state on listener disconnection. Diagnostics inspected do not log notification content or full notification keys.
- Remaining device-only validation is documented above. Do not claim live notification behavior, private Transit RemoteViews compatibility, UI rendering, or settings persistence as validated by CI.
- Final pre-merge head `d9d6004196204eaeced42af759cf4a24485523ac` passed Build plugin suite #21. PR #2 was then merged as `51d0550b0d29bab3376cccee96949f730c76591e`.


## Merge completion — 2026-11-25

- PR #2 was merged into `main` with merge commit `51d0550b0d29bab3376cccee96949f730c76591e`.
- Final pre-merge validation: [Build plugin suite #21](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/actions/runs/45719944978) passed all three test tasks, Android Lint for all three apps, all three APK builds, and uploads for `uber-rides-plugin`, `uber-eats-plugin`, and `transit-go-plugin`.
- No release tags or releases were created. The source repositories were not archived or deleted.
- Remaining follow-up: update the source repositories' remotes/workflows to point to the monorepo locations and decide separately whether to close the original repositories. Physical-device and extended notification-adapter tests remain release-readiness follow-up; CI did not perform those checks.


## Settings unification and Uber ride progress — 2026-11-27

### Shared settings direction

The settings refactor is being developed in draft [PR #6](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/pull/6), branch `refactor/shared-expressive-settings`. It is not merged.

- Introduce `:settings-ui` as the shared presentation layer for plugin settings. Keep plugin-specific preference storage, behavior, notification parsing and product logic inside each plugin.
- Use Material 3 Expressive components and motion for settings. Haze is the sole custom visual treatment and is limited to the translucent/blurred top app bar.
- Preserve the existing preview and its transitions, but render the actual Smartspacer target model through the official `SmartspacerView` renderer. Do not recreate Smartspacer cards with custom Compose layouts.
- Build previews through the same target-building path as production where practical. Preview targets must omit production actions/notification metadata and must never mutate production state or trigger real actions.
- Show live target data when an active notification/order/trip exists; otherwise use clearly labelled synthetic example data. Settings changes should refresh the preview and animate state transitions.
- Preserve existing preference semantics, permission flows and notification behavior. Share presentation components, not product-specific state or parsers.
- Reduce duplicated settings code now that the plugins live in one monorepo, without coupling their runtime behavior.

### Scope and status by plugin

- Uber Eats and Transit GO have been refactored to consume the shared settings UI in the PR branch. Their notification and product behavior is intended to remain unchanged.
- The shared module and those integrations still require full build and visual/device validation. Unit tests and lint were reported passing in the initial PR checks; do not treat that as proof that all APK builds or visual behavior are validated.
- PR #6 remains a draft and must not be merged until the remaining technical/API checks and validation are complete.

### Uber ride plugin — agreed settings and progress scope

The root `:uber` plugin is distinct from Uber Eats. It consumes Uber ride notifications through Smartspacer's `SmartspacerNotificationProvider`; it does not need its own `NotificationListenerService` or a duplicate notification-access permission screen. Smartspacer owns notification access and delivery. Keep the existing notification parsing and trip-state processing intact.

The agreed Uber settings scope is deliberately small:

- Keep the existing per-Target-instance configuration model. Smartspacer supports multiple instances of a Target; each instance's configuration must remain independently addressable and must not be converted into one global plugin preference.
- Preserve the current **Show Dynamic Information** preference and its exact behavior.
- Preserve the current **Hide Notification** preference and its exact behavior.
- Add the settings page using `:settings-ui`, Material 3 Expressive and the shared Smartspacer-rendered preview.
- Do not add redundant notification permission controls or extra functional preferences.
- Show the real trip progress as a numeric percentage on the Uber card whenever a valid progress value is available. No fabricated or time-based progress. If no valid value is supplied, omit the percentage rather than displaying a guessed value.
- Keep the existing visual progress track/vehicle rendering. The percentage should be derived from the same normalized progress value (0.0–1.0), formatted as a percentage, and updated from actual notification data. Any animation may interpolate between received values but must not create progress independently.
- Use representative synthetic trip data for the preview when no live trip exists; preview interactions must not affect a real trip.

### Uber progress implementation findings

Inspection of the Uber ride plugin on the PR branch found that the notification pipeline already extracts a normalized progress value and `TargetRenderer.drawProgress` uses it to draw the progress track and vehicle position. The initial audit found that the card did not expose that value as a numeric percentage. The implementation now formats the existing normalized value in the card renderer; it does not introduce a second progress calculation.

### Next implementation and validation steps

1. Complete a focused audit of Uber's per-instance configuration activity, preference keys/storage and target construction before changing the UI.
2. Trace the existing progress field from `UberNotificationProvider` through `NotificationRepository` and the target data model to `TargetRenderer`; identify the narrowest place to expose a percentage without changing trip parsing.
3. Implemented the Uber settings destination with the two existing preferences, per-instance persistence, and the shared Smartspacer-rendered preview. The screen and preview still require build and visual validation.
4. Implemented the numeric percentage in the existing Uber card renderer using the notification's normalized progress value. Unit tests cover null, non-finite, out-of-range and boundary formatting; execution/build validation remains pending.
5. Add or update unit tests for progress conversion/formatting and state propagation where the existing architecture permits.
6. Run focused local checks while iterating, then one consolidated full CI run for the coherent change batch. Do not reduce required coverage to save CI runs; seek approval before any additional full run beyond one consolidated follow-up.
7. Perform visual and real-notification device validation for per-instance settings persistence, preview fidelity, percentage updates, missing-progress behavior and existing Hide Notification behavior. CI alone does not establish these behaviors.
8. Update this plan and the PR description with verified results; keep the PR in draft until validation is complete.

**Current status at this plan update:** the numeric progress percentage and Compose settings destination have been implemented on the PR branch. The settings preview uses the official Smartspacer renderer, live in-memory target data when available, and synthetic sample data otherwise; preview targets omit real actions and notification metadata. The progress-only revision passed the repository test, lint, and APK-build workflow (#38, commit `7110d7a`). A later settings-screen revision failed during compilation because two rounded icon references were unavailable; the latest run (#40, commit `9830eca`) confirmed only those two unresolved references. The references have been corrected in the current working revision; final consolidated validation and visual/device checks remain pending. PR #6 remains a draft; no merge, release, version bump or publication is authorized.
