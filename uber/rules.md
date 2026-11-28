# Uber ride plugin rules

These module rules supplement the root [rules.md](../rules.md); they never weaken it. The root no-hardcoding, privacy, mandatory instruction-compliance, and CI-budget policies apply here in full.

## Product and integration boundaries

- `:uber` is for Uber rides. Do not mix in Uber Eats order parsing or state.
- Smartspacer delivers notifications through `UberNotificationProvider`. Do not add a second `NotificationListenerService`, notification-access permission request, or duplicate permission screen.
- Preserve package filtering for `com.ubercab`, provider authority, target contract, application ID, and the existing notification lifecycle.
- Treat Uber's custom RemoteViews layout names, view IDs, hidden Android ProgressStyle extras, flags, and category heuristics as unstable external integration assumptions. Keep them named, centralized, documented, and optional where possible.
- Prefer public notification fields and structured metadata. Keep RemoteViews parsing as a safe fallback and tolerate missing or changed fields.

## Per-instance settings

- Read and write configuration through the existing Smartspacer `smartspacerId` and `DataRepository` model. Multiple Target instances must remain independently configurable.
- Preserve the exact behavior and defaults of **Show Dynamic Information** and **Hide Notification**.
- Do not introduce plugin-global substitutes, redundant permission controls, or extra preferences without product approval.
- Use `:settings-ui`, Material 3 Expressive components, and the shared theme/scaffold for settings presentation. Haze is reserved for the shared translucent app bar.
- Keep the official `SmartspacerView` renderer for previews. Do not hand-draw a separate Compose imitation of a Smartspacer target.
- A live preview may use current target content but must omit real pending intents, source notification keys, and notification metadata. When no trip exists, show clearly labelled synthetic sample data. Preview must never dismiss, open, or mutate a real trip.
- Reflect the **Show Dynamic Information** setting in the preview. Preserve the production fallback behavior when the real target image cannot be rendered.

## Progress correctness

- Reuse the normalized progress value already extracted by `UberNotificationProvider`; do not create another source of truth.
- Display a numeric percentage only for a finite, valid value. Missing or invalid progress must be omitted.
- Clamp values only when formatting/drawing for presentation. Never write a clamped display value back into notification state.
- Progress updates must come from notifications. Animation may interpolate between received values but must never advance autonomously or based on elapsed time.
- Keep the existing progress track and vehicle renderer intact unless a reviewed visual change is required.

## No hardcoding and privacy

- Follow the root No hardcoding policy. Put plugin-owned visible strings in Android resources, build/environment values in Gradle configuration, and existing user preferences in the per-instance configuration model.
- Keep package names, notification keys, flags, RemoteViews IDs, and scoring assumptions in named integration constants; do not scatter magic strings or unexplained numbers.
- Never commit secrets or real notifications as fixtures.
- Never log raw titles, bodies, extras values, full notification keys, trip identifiers, driver names, addresses, route details, or images. Log only minimal structural diagnostics and exception types.

## Architecture and verification

- Keep notification ingestion, parsing, state, target construction, image rendering, settings persistence, and Compose presentation as separate responsibilities.
- Add focused unit tests for progress formatting, parser assumptions, missing/malformed fields, and state changes where those areas change.
- Before finishing, run the relevant `:uber` unit tests, release lint, and release build. Review the full diff for manifest, package, permission, resource, preference, and release changes.
- Follow the root CI budget: focused local checks while iterating, one consolidated full CI run for a coherent batch, reuse passing results, and rerun only failed jobs where possible. Ask before additional full CI runs beyond the agreed consolidated follow-up. Never reduce required coverage to save CI.
- Report physical-device and real-notification checks separately; CI does not prove live Uber behavior or visual fidelity.
- Never bump versions, change signing, create tags, or publish releases without explicit approval.
