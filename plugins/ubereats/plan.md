# Maintenance and Repair Plan

## Baseline

- Source repository: `INFORMATION89/UberEats-Smartspacer` (imported into `plugins/ubereats/`)
- Audited branch: `main`
- Audited commit: `b73c4353c17faea83146584a823d32cc3527382c` (2026-11-17)
- Scope: static review of the repository tree, Kotlin/Compose implementation, manifest, resources, Gradle configuration, README, and GitHub Actions.
- This is a repair plan, not permission to publish a release or change the app's version.

## Summary

The plugin has a sound basic shape: it is a native Kotlin Android app, uses Jetpack Compose and Material 3 for its settings UI, reads notifications from the two expected Uber packages, and does not request Internet access. The latest Build and Release workflow runs for the audited commit succeeded.

The UI is not entirely hand-drawn. It uses Material 3 components, dynamic system colors on Android 12+, and Google Sans Text. The bespoke parts are concentrated in the animated preview, blur/header treatment, custom row/icon treatments, and the pull-to-open Source code interaction. Those are Compose implementations, not evidence that the whole screen should be replaced.

The main maintenance risks are the oversized settings file, notification parsing based on external-app heuristics, and the lack of parser regression tests. Debug logging of notification contents was identified in the standalone audit and sanitized during migration. Monorepo Build plugin suite [#3](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/actions/runs/45716316251) passed this module's available test, lint, release-build, and artifact-upload stages. A bounded notification-key store and candidate cache have since been added; Build suite #10 is validating the new unit tests and extended lint.

## Findings

### P1 — Debug logs can contain order information <sub>Implemented during monorepo migration</sub>

The original `OrderReader.logNotification()` wrote notification extras values and the full notification key to Logcat in debuggable builds. During import to `:ubereats`, the logger was changed to report only source package, flags, category, channel, title/body presence, and extra key/type names. Parser failures now log the exception type only, without a throwable or stack trace. Removal callbacks filter to the supported Uber packages before forwarding a key.

**Remaining validation:** inspect Logcat on a debug device using synthetic notifications and verify no raw order content is emitted.

### P1 — Order detection depends on language-sensitive heuristics

`EatsNotifications.kt`, especially `FOOD_CHANNEL`, `FOOD_WORDS`, `RIDE_WORDS`, `MIN_SCORE`, and `isUberFood()` (around lines 120–132 and 240–247), uses notification categories, flags, channel names, and English/Spanish words to distinguish Uber food orders from rides and promotions.

This is a reasonable fallback because the apps do not expose the order through a public API, but it can miss notifications whose channel or wording changes, especially in other app languages.

**Repair:** isolate the parsing rules into a clearly named notification-contract/parser area. Add sanitized fixtures for real observed variants: a food order from Uber Eats, a food order from the Uber app, a ride, a promotion, an update that only contains an ETA, and notification removal/reconnection. Add cases for the app languages actually claimed or supported. Change scoring only when a fixture demonstrates the expected behavior.

### P2 — Settings screen and visual system are concentrated in one file

`app/src/main/java/dev/marco/smartspacer/ubereats/SettingsActivity.kt` is 1,121 lines. It currently owns Activity lifecycle, settings state, list composition, notification-access navigation, preview UI, nested-scroll gesture logic, animations, rows, icon badges, theme selection, and typography.

The file already has named constants for many gesture values, but UI spacing, shapes, animation values, and accent colors are still spread through composables.

**Repair:** refactor without redesigning the screen:
- Keep `SettingsActivity` focused on lifecycle and Android navigation.
- Move the screen composition, preview card, reusable settings rows, gesture interaction, and theme/typography into focused files.
- Create a small Compose token object for repeated dimensions, corner radii, and intentional animation values.
- Continue to use Material 3 components for switches, segmented buttons, surfaces, text, and app bars. Retain custom animation only when it adds clear value.
- Do not extract a cross-repository library or make this plugin depend on Transit GO. Keep this repository independently buildable.

### P2 — UI code is duplicated in the Transit GO repository

The settings screens in this repository and `INFORMATION89/Transit-GO-Smartspacer` have almost the same structure and reusable components, with product-specific preview and setting differences.

**Repair:** extract and organize reusable UI components locally in this repository, and keep the equivalent structure understandable in the other repository. Do not introduce a shared remote dependency or a third repository as part of this cleanup. Any synchronized UI change must preserve each plugin's distinct settings and preview behavior.

### P2 — Parser tests remain incomplete

The standalone audit found no `src/test` or `src/androidTest` source set. The monorepo now has four unit tests for the bounded remembered-key store and root CI runs tests and lint for this module. The parser heuristics themselves are not yet covered.

**Remaining work:** add sanitized tests for notification parsing, candidate scoring, Uber-food filtering, state comparison, ETA-only updates, and notification lifecycle. Never use real order notifications or personal data as fixtures.

### P2 — Installation instructions recommend disabling Play Protect

The README explains how to install the APK with ADB, but also tells users to turn off Play Protect scanning temporarily.

**Repair:** remove the recommendation to disable device protection. Keep the ADB installation route and explain that users should only install an APK obtained from this repository's trusted release page. Do not advise users to weaken device security to install the plugin.

### P3 — Hardcoded values need classification, not indiscriminate removal

Some values are legitimate integration constants: Uber package names, Android notification extra keys, source URL, and the notification scoring rules. Other values are UI design decisions: the green access state, accent palette, corner radii, dimensions, and animation timings.

**Repair:** centralize integration identifiers and UI tokens in named, documented locations. Keep plugin-owned user-facing strings in Android string resources. Do not move every number into XML or remove useful constants merely to eliminate literals. Keep Uber's own notification text intact; it belongs to the source app's language.

## Recommended implementation order

1. **Establish a baseline.** Build the current code, record the current settings screen and live-card behavior, and preserve the version and release configuration.
2. **Reduce diagnostic data exposure.** Sanitize or remove notification-content logging before adding more diagnostics.
3. **Add parser tests.** Capture expected behavior with sanitized fixtures before changing heuristics.
4. **Refactor the settings UI.** Split the 1,121-line file into focused files and introduce a small token/theme layer. Preserve current behavior and appearance unless a specific issue is demonstrated.
5. **Harden notification parsing.** Centralize external-app assumptions, add graceful fallbacks, and verify Uber app food orders are not confused with rides or promotions.
6. **Improve CI.** Run unit tests and lint on pull requests and pushes, in addition to building the release variant.
7. **Fix README installation guidance.** Remove instructions to turn off Play Protect and keep the safe ADB path.
8. **Review the result.** Compare screenshots and behavior, inspect the final diff, and report anything that cannot be validated without a real Uber Eats/Uber order.

## Acceptance criteria

- The settings screen behaves and looks the same unless a deliberate, documented improvement is made.
- The UI remains based on Material 3 and the existing dynamic-color/Google Sans theme.
- No raw order or notification text is written to Logcat.
- Food orders, rides, promotions, ETA-only updates, removal, and listener reconnection have automated parser/state coverage.
- Plugin-owned settings strings remain resource-based and localized.
- The manifest still has no Internet permission and the listener still ignores unrelated packages before reading their content.
- `assembleRelease`, unit tests, and lint pass in CI.
- No release, tag, version bump, signing-key change, or release-note publication is made without explicit approval.

## Non-goals

- Do not rewrite the plugin from scratch.
- Do not replace every custom Compose component with a stock component just to reduce line count.
- Do not add networking, analytics, telemetry, or a backend.
- Do not change the Smartspacer target contract, package/application ID, notification-access model, or release signing identity without a demonstrated need and explicit review.
- Do not publish a release. Follow `CLAUDE.md` for versioning and release approval.
