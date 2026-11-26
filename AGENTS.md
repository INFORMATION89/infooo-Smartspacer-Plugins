# Repository instructions for AI agents

Read this file before making changes. Then read [rules.md](rules.md), [CLAUDE.md](CLAUDE.md), [plan.md](plan.md), and [continue.md](continue.md). For a plugin, also read its nearest `AGENTS.md`, `rules.md`, `plan.md`, and `continue.md`.

## Project boundaries

- This repository contains only Smartspacer plugins. Do not add Solstice, Meridian-Engine, or unrelated apps here.
- Keep `:uber` (ride tracking) separate from `:ubereats` (food orders). They are not interchangeable.
- Keep `:ubereats` and `:transitgo` independently buildable Android applications with their existing application IDs, namespaces, notification-listener components, Smartspacer provider contracts, settings, preferences, and release identities.
- Do not couple one plugin to another. The current `:shared` module belongs to the existing upstream-derived `:uber` implementation; do not make new plugins depend on it without a concrete review.
- Do not extract common UI code simply because two screens look similar. First verify behavior, dependencies, resource ownership, and the costs of a shared module.

## Gradle and module layout

- The root Gradle wrapper and root plugin versions are authoritative.
- New plugin modules are included from `settings.gradle`; their Android application project directories are `plugins/<plugin>/app`.
- Do not add nested Gradle wrappers, nested settings files, or independent root builds under `plugins/`.
- Keep per-application values (namespace, applicationId, SDK levels, versionCode/versionName, signing config) in that plugin's app module.
- Do not upgrade AGP, Kotlin, Gradle, Compose, or Android SDK versions as part of an unrelated plugin change.

## Privacy and notifications

- Filter package/channel before reading notification contents.
- Never log raw notification titles, bodies, extras values, full notification keys, order/trip identifiers, names, locations, or images.
- Keep notification state in memory unless persistence is explicitly required and reviewed.
- Never cancel, dismiss, or modify source-app notifications.
- Do not add Internet permission, telemetry, analytics, remote logging, or a backend without explicit approval.

## Validation and verification budget

Before running checks, identify the affected plugin(s), acceptance criteria, smallest relevant tests/lint/build tasks, full CI work, and any device-only requirements.

- During implementation, run only the focused tasks that cover the current change. Batch related changes before pushing so the full pull-request workflow runs once for a coherent batch.
- The complete CI suite invokes `:uber:testDebugUnitTest`, `:ubereats:testDebugUnitTest`, and `:transitgo:testDebugUnitTest`, release lint for all three apps, and builds all three APKs. Only Uber Eats and Transit GO currently have unit-test source sets; the Uber test task is still invoked but has no imported tests. See the root README for commands.
- Reuse successful results for the same commit and environment. If a single CI job fails, inspect its logs and retry only the failed job where possible; do not rerun passing jobs without a technical reason.
- For an infrastructure/environment failure, make at most one materially different retry. If it fails for the same reason, stop and report the blocker instead of repeating attempts.
- Ask before adding a substantially larger test matrix or requesting another full CI run beyond the agreed plan.
- Inspect the manifest, package IDs, permissions, signing configuration, resource paths, and complete diff as part of the relevant review; do not repeat unrelated checks.
- While real-device access is unavailable, CI is the current routine acceptance gate. Keep real notification and physical-device behavior explicitly `NOT TESTED` or `DEFERRED`; CI, static review, and emulator compilation do not prove live notification behavior. Run device-only checks only when the task or release gate explicitly requires them and the hardware/source app is available.

## Git and releases

- Work on a focused branch; do not work directly on `main`.
- Keep changes small enough to review, but fix root causes rather than layering workarounds.
- Never bump versions, push tags, publish releases, change release signing identity, or alter GitHub release assets without explicit user approval.
- Preserve source repositories and their history unless the user explicitly asks to archive or delete them.
