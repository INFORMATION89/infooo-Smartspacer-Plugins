# Repository architecture

This repository is a collection of independent Smartspacer Android plugins. Sharing a Git repository and Gradle wrapper does not make them one application.

## Application ownership

| Gradle project | Location | Responsibility | Shared runtime code |
|---|---|---|---|
| `:uber` | `uber/` | Uber rides and drivers | `:shared`, `:systemstubs` |
| `:ubereats` | `plugins/ubereats/app/` | Uber Eats food orders, including orders surfaced by the Uber app | None |
| `:transitgo` | `plugins/transitgo/app/` | Active Transit GO trips | None |

Each application owns its application ID, namespace, version, target/provider components, notification listener, preferences, resources, signing configuration, release notes, tag prefix, and APK artifact.

## Gradle ownership

- The root `settings.gradle` includes all maintained projects and maps imported applications into `plugins/<plugin>/app`.
- The root Gradle wrapper is the only wrapper.
- Root plugin versions are declared through `gradle/libs.versions.toml`.
- Dependencies shared by Uber Eats and Transit GO are declared once in the version catalog; each app still opts into only the dependencies it uses.
- The catalog also owns the Smartspacer SDK version used by `:shared` and both imported apps, plus the Room and Rikka Refine versions consumed by `:shared` and `:systemstubs`.
- Remaining legacy dependency versions (including navigation Safe Args and protobuf build tooling) stay in the root Gradle build until migrated and validated separately.
- Android application settings and product versions remain in each app module.

## Runtime boundaries

There are no runtime dependencies between the three plugins.

- `:shared` is legacy infrastructure owned by `:uber`; it is not a general-purpose plugin SDK.
- Uber Eats classifies notifications from `com.ubercab.eats` and `com.ubercab`. The latter requires distinguishing food orders from rides and promotions.
- Transit GO reads only Transit GO notifications and uses private Transit `RemoteViews` implementation details to obtain optional route and step visuals. Those identifiers are an unstable integration boundary.
- Each plugin keeps its notification state local and in memory. Do not add cross-plugin storage, networking, analytics, telemetry, or a backend without explicit review.

## UI boundaries

Uber Eats and Transit GO use Compose and Material 3, but their settings and previews are product-specific. Similar components may be extracted only after behavior, theme, lifecycle, localization, and ownership have been verified. Do not create a shared UI module solely to reduce visual duplication.

## Build and release flow

- `.github/workflows/build-plugins.yml` tests, lints, and builds every maintained application, then uploads one artifact per app.
- The three tag namespaces remain independent: `uber-v*`, `ubereats-v*`, and `transitgo-v*`.
- The per-plugin release entry points call the reusable `_release-android-plugin.yml` workflow.
- Release validation must match the tag version to that plugin's declared `versionName`, pass tests and lint, use the permanent signing identity, and publish only that plugin's APK.
- A build artifact is not an official GitHub release.

## Future shared code

The next appropriate shared boundaries are build metadata, version catalog entries, release automation, and possibly small UI primitives after a separate audit. Notification parsing, app state, settings, Smartspacer providers, and product-specific resources must remain owned by their respective plugins.
