# Repository Rules

These rules apply to human and AI-assisted changes across the Smartspacer plugin monorepo. Plugin-specific rules live beside each plugin.

## Product boundaries

- This repository is exclusively for Smartspacer plugins.
- `:uber` is the Uber ride-tracking plugin. `:ubereats` is the food-order plugin. Keep their application IDs, providers, behavior, and release streams independent.
- `:transitgo` is an independent Transit GO plugin. It must not depend on Uber Eats or the Uber ride plugin.
- Preserve each plugin's Smartspacer target/provider contract, application ID, namespace, authorities, notification-listener component, preferences, and signing identity unless a reviewed change requires otherwise.
- Do not add a dependency between plugins. The legacy `:shared` module remains owned by `:uber` until a deliberate shared-code audit establishes a safe boundary.

## Gradle and build ownership

- The root wrapper, plugin repositories, and plugin versions are authoritative.
- Do not copy standalone `settings.gradle`, root `build.gradle`, `gradle.properties`, wrappers, or workflows into a plugin directory.
- Each new application module lives in `plugins/<id>/app`; include it from root `settings.gradle`.
- Keep application-specific configuration and dependencies in that module's `build.gradle.kts`.
- Avoid broad toolchain/dependency upgrades during feature work.
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
- Report device-only checks separately from automated checks. CI success is not device validation.
- Plan verification before running it: identify affected plugins, acceptance criteria, the smallest relevant checks, and any expensive CI/device checks.
- Use focused module tests, lint, and builds while iterating. Batch related changes and push one coherent update before the full pull-request workflow; avoid triggering the complete suite for every small edit.
- Reuse passing results for the same commit and relevant environment. When one CI job fails, inspect its logs and retry only that job where possible; do not rerun successful jobs without a technical reason.
- For infrastructure/environment failures, allow at most one retry with a materially different setup. If the same requirement still fails, stop and mark it BLOCKED with the evidence and required access.
- Ask before substantially expanding the agreed verification scope or spending another complete CI run after a failure.
- While real-device access is unavailable, the current routine gate is the repository CI suite. Keep live-notification and physical-device checks explicitly NOT TESTED or DEFERRED; never treat CI as proof of real notification behavior.

## Releases

- Keep tag namespaces separate: `uber-v*`, `ubereats-v*`, and `transitgo-v*`.
- Keep release notes and assets scoped to each plugin.
- Never bump a version, push a release tag, publish a GitHub release, change signing secrets, or change signing identity without explicit user approval.
- A debug-signed CI APK is a test artifact, not an official release.

## No hardcoding

Do not hardcode values whose ownership or lifetime belongs outside the implementation that consumes them.

- Put plugin-owned user-facing text in Android string/plural resources; use the existing localization system for every supported locale.
- Put build/environment-specific values in the appropriate Gradle configuration, build variants, manifest placeholders, or generated build configuration. Never commit credentials, tokens, signing secrets, private keys, or other secrets.
- Keep externally owned integration assumptions (package names, channels, notification keys, RemoteViews IDs, protocol fields, provider identifiers) named, centralized at the integration boundary, documented as assumptions, and covered by tests where practical. Do not scatter magic strings or numeric flags through parsing/rendering code.
- Put user- or product-configurable behavior in the existing preference/configuration model. Do not duplicate a setting as a code constant or silently replace persisted values with a hardcoded default.
- Avoid unexplained numeric literals for thresholds, timeouts, limits, dimensions, scoring weights, and policy decisions. Give meaningful values a named constant or typed configuration with units and rationale.
- Keep true language, platform, mathematical, and stable protocol invariants as literals when that is clearer. Do not create configuration files, resources, or abstractions for every harmless constant.
- Do not replace one hardcoded value with another, hide hardcoding behind a helper, or introduce speculative configuration frameworks. Trace the value to its actual owner and choose the narrowest appropriate source of truth.
- Before completion, review changed code for magic numbers/strings, duplicated values, sample data leaking into production, and secrets. Explain any intentional hardcoded exceptions in the change summary.

## CI and verification budget

Avoid excessive CI without weakening required coverage.

- During iteration, prefer the smallest relevant local test, lint, or build tasks. Batch related changes and run the complete PR workflow once for a coherent implementation batch.
- Reuse passing CI results for the same commit and environment. If one job fails, inspect its logs and rerun only the failed job where possible; do not rerun successful jobs without a technical reason.
- Do not trigger a full workflow for documentation-only changes when the workflow can safely exclude them. Do not alter required checks or reduce coverage merely to avoid a run.
- Do not create extra commits or push tiny sequential updates solely to trigger repeated CI runs. Consolidate fixes first.
- For infrastructure failures, allow at most one materially different retry; if the same failure recurs, stop and report the blocker.
- Ask for approval before an additional full CI run beyond the agreed consolidated run/follow-up. This does not waive required checks or the existing release/device acceptance criteria.
