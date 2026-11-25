# Repository Rules

These rules apply to human and AI-assisted changes across the Smartspacer plugin monorepo. Plugin-specific rules live beside each plugin.

## Product boundaries

- This repository is exclusively for Smartspacer plugins.
- `:uber` is the Uber ride-tracking plugin. `:ubereats` is the food-order plugin. Keep their application IDs, providers, behavior, and release streams independent.
- `:transitgo` is an independent Transit GO plugin. It must not depend on Uber Eats or the Uber ride plugin.
- Preserve each plugin's Smartspacer target/provider contract, application ID, namespace, authorities, notification-listener component, preferences, and signing identity unless a reviewed change requires otherwise.
- Do not add a dependency between plugins. The legacy `:shared` module remains owned by `:uber` until a deliberate shared-code audit establishes a safe boundary.

## Gradle and build ownership

- The root wrapper and plugin repositories are authoritative. Shared imported-app plugin/dependency versions live in `gradle/libs.versions.toml`.
- Use the version-catalog aliases in Kotlin DSL modules. Let the Compose BOM select BOM-managed versions; do not pin a second version beside it.
- Preserve the legacy `:uber` dependency setup in the root build until it is migrated deliberately; do not create a second version source while doing unrelated work.
- Do not copy standalone `settings.gradle`, root `build.gradle`, `gradle.properties`, wrappers, or workflows into a plugin directory.
- Each new application module lives in `plugins/<id>/app`; include it from root `settings.gradle`.
- Keep application-specific configuration and dependencies in that module's `build.gradle.kts`.
- Avoid broad toolchain/dependency upgrades during feature work.
- Keep per-plugin release workflows as thin tag/configuration entry points into `.github/workflows/_release-android-plugin.yml`.
- A release tag must exactly match that module's `versionName`; release validation runs the module's tests and lint before signing/building.
- Keep release tasks and tag filters scoped to the intended plugin. A plugin release must never build or upload another plugin's APK.

## Notification integration and privacy

- Filter the source package and, where applicable, notification channel before reading notification content.
- Handle notification removal and listener disconnection without leaving stale state.
- Never log raw notification titles, bodies, extras values, full notification keys, courier/driver names, order or trip details, addresses, locations, or images.
- Diagnostics may report only minimal structural metadata (for example flags, category, presence of known fields, and extra key/type names).
- Never persist notification layouts, screenshots, order data, or trip data automatically.
- Never dismiss, modify, or cancel source-app notifications.
- Keep the plugins local-first. Do not add Internet permission, networking, analytics, telemetry, remote logging, or a backend without explicit approval.

## UI and settings

- Keep every plugin's settings destination and preferences independent.
- Use Material 3 / Jetpack Compose for Compose plugins and accessible platform components for standard controls.
- Preserve established UI behavior, localization, dynamic colors, and preference defaults when refactoring.
- Separate lifecycle/navigation from screen composition, previews, reusable components, theme/tokens, and motion behavior.
- Do not turn duplicated UI into a shared library until its API, theming, lifecycle, and release implications have been reviewed.

## Strings and localization

- Plugin-owned text belongs in Android resources.
- Preserve every locale already shipped by a plugin.
- Do not translate or rewrite text supplied by third-party notification content.
- Test localized and long labels; preserve original source-app wording when parsing cannot confidently interpret it.

## Tests and CI

- Add sanitized unit tests for notification filtering, parser decisions, state changes, formatting, and edge cases.
- CI invokes the unit-test task for `:uber`, `:ubereats`, and `:transitgo`, runs release lint for all three, and builds each maintained APK independently. The current `:uber` module has no imported unit-test source set; its task is still invoked to validate Gradle task configuration. Uber Eats and Transit GO have separate unit tests.
- Do not commit real notifications, names, routes, addresses, orders, courier/driver data, or images as fixtures.
- Report device-only checks separately from automated checks.
- Keep `main` protected by a pull-request rule and the successful Build plugin suite check; see `.github/BRANCH_PROTECTION.md`.

## Releases

- Keep tag namespaces separate: `uber-v*`, `ubereats-v*`, and `transitgo-v*`.
- Keep release notes and assets scoped to each plugin.
- Never bump a version, push a release tag, publish a GitHub release, change signing secrets, or change signing identity without explicit user approval.
- A debug-signed CI APK is a test artifact, not an official release.
