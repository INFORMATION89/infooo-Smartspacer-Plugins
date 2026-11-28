# Repository Rules

These rules apply to changes in the `:ubereats` module. Follow the root [AGENTS.md](../../AGENTS.md) and [rules.md](../../rules.md), then read the local `AGENTS.md`, `CLAUDE.md`, and `plan.md` before making changes.

## Product boundaries

- This is an independent Android plugin for Smartspacer. Keep it independently buildable and installable.
- Preserve the current Smartspacer target/provider contract and the existing application ID, namespace, provider authority, and notification-listener component unless a change is explicitly required and reviewed.
- The plugin must remain local-first and offline. Do not add Internet permission, network calls, analytics, telemetry, remote logging, or a backend.
- Read only notifications from the expected Uber Eats and Uber packages. Filter by package before reading notification content. Never inspect or retain unrelated notifications.
- Never dismiss, modify, or cancel source-app notifications. Smartspacer may hide the relevant notification on the lock screen through its own Native Smartspace behavior; the notification must remain in the notification shade.

## UI and Compose

- Use Jetpack Compose and Material 3 as the default UI foundation.
- Prefer Material 3 components for standard controls and surfaces. Do not hand-draw a control when an appropriate accessible Material component already exists.
- Preserve the existing dynamic-color behavior on Android 12+ and Google Sans Text typography unless a reviewed change requires otherwise.
- Keep bespoke animation and gesture behavior only when it has a clear user benefit, is understandable, and has been tested. Avoid custom gesture machinery for ordinary navigation or controls.
- Keep Activity lifecycle/navigation separate from screen composition, preview rendering, reusable rows, theme, and animation logic. Avoid recreating a monolithic `SettingsActivity.kt`.
- Put repeated spacing, shape, color, and motion decisions in a small, named Compose token/theme layer. Do not turn every numeric literal into an XML resource.
- Use `MaterialTheme` colors and typography where possible. Check contrast in light, dark, and dynamic-color themes.

## Strings and localization

- All plugin-owned user-facing text belongs in Android string/plural resources. Do not add visible English or Spanish literals directly to composables or Kotlin UI logic.
- Preserve existing English and Spanish translations. Update every supported locale when adding or changing a resource.
- Do not translate or rewrite text supplied by Uber's notifications; the source app owns that wording and language.
- Brand names and technical identifiers may remain literal where they are genuinely invariant.

## Notification parsing and integration

- Treat Uber package names, notification extra keys, category values, channel heuristics, and flags as external integration assumptions.
- Keep those assumptions named, centralized, and documented. Do not scatter magic strings or numeric flags through the parser.
- Prefer structured notification metadata over text matching. Use text heuristics only where metadata is insufficient, and document the languages/variants they cover.
- Parser changes must include sanitized regression fixtures for food orders, rides, promotions, ETA-only updates, and notification lifecycle events.
- Handle missing, malformed, or changed notification fields safely. A parsing failure must not crash the listener or leave stale order state visible.
- Do not use real user notifications, names, addresses, order IDs, or photos as committed test fixtures.

## Privacy and diagnostics

- Never log raw notification titles, bodies, extras, courier names, order details, full notification keys, or photos.
- Debug diagnostics must be sanitized and must not write personal order data to persistent files.
- Do not add persistent dumps or screenshots of live notifications. Any exceptional diagnostic capture must be explicitly opt-in, debug-only, documented, bounded, and easy to delete.
- Keep the absence of Internet permission and explain the notification-access behavior accurately in the README.

## Architecture and code quality

- Keep notification ingestion/state, parsing, Smartspacer target construction, settings persistence, and UI as separate responsibilities.
- Favor small, named functions and focused files. Avoid speculative abstractions and unnecessary dependencies.
- Keep existing public behavior and settings compatible. Handle SharedPreferences defaults and enum parsing safely.
- Do not introduce a cross-module dependency on Transit GO or the original Uber ride plugin. Uber Eats must remain independently buildable and installable.
- Do not perform broad formatting or unrelated cleanup in the same change as a functional fix.

## Verification

Before finishing a change:

1. Run `./gradlew :ubereats:testDebugUnitTest`.
2. Run `./gradlew :ubereats:lintRelease`.
3. Run `./gradlew :ubereats:assembleRelease`.
4. Review the diff for accidental permission, package, resource, or release changes.
5. State which commands passed and which could not be run. Never claim tests passed if they were not executed.

Add automated tests for parser decisions, scoring, state transitions, and formatting when changing those areas. CI must run tests and lint on pull requests as well as pushes to `main`.

## Dependencies and releases

- Avoid dependency upgrades unrelated to the task. Check compatibility with the Android Gradle Plugin, Kotlin, Compose BOM, and Material 3 before changing versions.
- Keep versionName/versionCode and semantic-versioning behavior consistent with `CLAUDE.md`.
- Never create/push a release tag, publish a GitHub release, alter release notes for a release, or change signing secrets/identity without explicit approval.
- A locally or CI-generated debug-signed release APK is a test artifact, not an official distributable release.

## No hardcoding and rule enforcement

The root `rules.md` No hardcoding policy is mandatory for this module. Keep user-facing text in localized Android resources; build/environment values in the correct build configuration; configurable behavior in the existing preference model; and third-party integration assumptions in named, centralized constants at the integration boundary. Do not scatter magic strings, unexplained numeric thresholds, flags, IDs, or duplicated defaults. Secrets must never be committed. Keep legitimate platform/protocol invariants literal when appropriate, without needless abstraction.

Before implementation, read the root and local instructions and make a concise acceptance checklist. Before completion, inspect the complete diff against it and report any unverified requirement. Root CI/verification-budget rules also apply: focused local checks while iterating, one consolidated full CI run per coherent batch, reuse passing results, retry only failed jobs where possible, and obtain approval before extra full CI runs beyond the agreed follow-up. Never reduce required test coverage to save CI.
