# Repository Rules

These rules apply to changes in the `:transitgo` module. Follow the root [AGENTS.md](../../AGENTS.md) and [rules.md](../../rules.md), then read the local `AGENTS.md`, `CLAUDE.md`, and `plan.md` before making changes.

## Product boundaries

- This is an independent Android plugin for Smartspacer. Keep it independently buildable and installable.
- Preserve the current Smartspacer target/provider contract and the existing application ID, namespace, provider authority, and notification-listener component unless a change is explicitly required and reviewed.
- The plugin must remain local-first and offline. Do not add Internet permission, network calls, analytics, telemetry, remote logging, or a backend.
- Read only notifications from Transit and only when the expected GO channel matches. Filter package and channel before reading notification content.
- Never dismiss, modify, or cancel Transit notifications. Smartspacer may hide the relevant notification on the lock screen through its own Native Smartspace behavior; the source notification must remain in the notification shade.

## UI and Compose

- Use Jetpack Compose and Material 3 as the default UI foundation.
- Prefer Material 3 components for standard controls and surfaces. Do not hand-draw a control when an appropriate accessible Material component already exists.
- Preserve the existing dynamic-color behavior on Android 12+ and Google Sans Text typography unless a reviewed change requires otherwise.
- Keep bespoke animation and gesture behavior only when it has a clear user benefit, is understandable, and has been tested. Avoid custom gesture machinery for ordinary navigation or controls.
- Keep Activity lifecycle/navigation separate from screen composition, preview rendering, reusable rows, theme, and animation logic. Avoid recreating a monolithic `SettingsActivity.kt`.
- Put repeated spacing, shape, color, render, and motion decisions in small, named token/configuration objects. Do not turn every numeric literal into an XML resource.
- Use `MaterialTheme` colors and typography where possible. Check contrast in light, dark, and dynamic-color themes.

## Settings screen architecture

- Keep one plugin-owned settings destination. The existing `SettingsActivity` is opened both from Smartspacer target configuration and the launcher; do not add a duplicate settings Activity or parallel preferences screen.
- Keep `SettingsActivity` focused on Android lifecycle and system permission navigation.
- Keep Compose composition in `TransitGoSettingsScreen`; keep theme and typography in `SettingsTheme`.
- Continue extracting the preview card, reusable setting rows, and custom motion/gesture code into focused files. Preserve the current preview, preferences, dynamic colors, typography, localization, and Smartspacer target refresh behavior.
- Any future settings screen additions must use the existing `Settings` persistence model unless a migration is explicitly planned and tested.

## Strings and localization

- All plugin-owned user-facing text belongs in Android string/plural resources. Do not add visible English or Spanish literals directly to composables or Kotlin UI logic.
- Preserve all existing supported locale resources when adding or changing strings.
- Do not translate or rewrite text supplied by Transit notifications; the source app owns that wording and language.
- Brand names and technical identifiers may remain literal where they are genuinely invariant.
- When adding derived text (for example bike counts), use verified localized terms and plural rules. For an unknown source-app language, preserve the original text instead of guessing.

## Transit notification integration

- Treat the Transit package name, GO channel, notification flags/extras, `bigContentView`, and internal RemoteViews IDs as external integration assumptions, not stable public API.
- Keep package/channel/ID assumptions named, centralized, and documented. Do not scatter magic strings or numeric flags through the parser.
- Prefer notification metadata and structured fields over scraping rendered views. Use RemoteViews only where the required information is not available through notification metadata.
- Every optional RemoteViews view must be handled safely when missing, renamed, hidden, or of an unexpected type. The card must still render the information that is available.
- Keep the bitmap-rendering width/scale strategy explicit and test it on more than one screen density/width.
- Parser/renderer changes must include sanitized fixtures or focused tests for expected and missing layout elements, live/non-live notifications, and notification lifecycle events.
- Do not use real trip notifications, route/location history, or personal travel information as committed test fixtures.

## Privacy and diagnostics

- Never log raw trip titles, bodies, route names, directions, arrival times, previous steps, notification keys, or images.
- Do not persist full notification layouts or screenshots automatically.
- Any exceptional layout capture must be explicitly opt-in, debug-only, documented, bounded, and easy to delete. Never enable it in release builds.
- Keep the absence of Internet permission and explain the notification-access behavior accurately in the README.

## Architecture and code quality

- Keep notification ingestion/state, Transit parsing/rendering, Smartspacer target construction, settings persistence, and UI as separate responsibilities.
- Favor small, named functions and focused files. Avoid speculative abstractions and unnecessary dependencies.
- Keep existing settings compatible. Handle missing notification content, unexpected layout variants, and enum parsing safely.
- Do not introduce a cross-module dependency on Uber Eats or the original Uber ride plugin. Transit GO must remain independently buildable and installable.
- Do not perform broad formatting or unrelated cleanup in the same change as a functional fix.

## Verification

Before finishing a change:

1. Run `./gradlew :transitgo:testDebugUnitTest` from the repository root.
2. Run `./gradlew :transitgo:lintRelease`.
3. Run `./gradlew :transitgo:assembleRelease`.
4. Review the diff for accidental permission, package, resource, or release changes.
5. State which commands passed and which could not be run. Never claim tests passed if they were not executed.

Add automated tests for notification filtering, live-trip detection, bike-count formatting, state transitions, and safe RemoteViews parsing. CI must run tests and lint on pull requests as well as pushes to `main`.

## Dependencies and releases

- Avoid dependency upgrades unrelated to the task. Check compatibility with the Android Gradle Plugin, Kotlin, Compose BOM, and Material 3 before changing versions.
- Keep versionName/versionCode and semantic-versioning behavior consistent with `CLAUDE.md`.
- Never create/push a release tag, publish a GitHub release, alter release notes for a release, or change signing secrets/identity without explicit approval.
- A locally or CI-generated debug-signed release APK is a test artifact, not an official distributable release.

## No hardcoding and rule enforcement

The root `rules.md` No hardcoding policy is mandatory for this module. Keep user-facing text in localized Android resources; build/environment values in the correct build configuration; configurable behavior in the existing preference model; and third-party integration assumptions in named, centralized constants at the integration boundary. Do not scatter magic strings, unexplained numeric thresholds, flags, IDs, or duplicated defaults. Secrets must never be committed. Keep legitimate platform/protocol invariants literal when appropriate, without needless abstraction.

Before implementation, read the root and local instructions and make a concise acceptance checklist. Before completion, inspect the complete diff against it and report any unverified requirement. Root CI/verification-budget rules also apply: focused local checks while iterating, one consolidated full CI run per coherent batch, reuse passing results, retry only failed jobs where possible, and obtain approval before extra full CI runs beyond the agreed follow-up. Never reduce required test coverage to save CI.
