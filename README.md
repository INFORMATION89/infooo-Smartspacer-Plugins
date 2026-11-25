# infooo Smartspacer Plugins

A collection of independently installable Android plugins for [Smartspacer](https://github.com/KieronQuinn/Smartspacer), maintained in one Gradle repository.

This repository keeps the original Uber ride-tracking plugin and adds separate plugins for Uber Eats orders and Transit GO trips. It is an independent community project, not an official Smartspacer release and not affiliated with the source apps.

## Plugins

| Plugin | Gradle module | Package / application ID | Version | Status |
|---|---|---|---|---|
| Uber (rides) | `:uber` | `com.kieronquinn.app.smartspacer.plugin.uber` | 1.1.0 | Existing plugin based on the upstream Smartspacer plugin |
| Uber Eats (food orders) | `:ubereats` | `dev.marco.smartspacer.ubereats` | 0.1.1 (alpha) | Migrated from the standalone repository; real-order validation still required |
| Transit GO | `:transitgo` | `dev.marco.smartspacer.transitgo` | 1.3.1 | Migrated from the standalone repository; device validation still required |

**Uber and Uber Eats are different plugins.** `:uber` tracks rides and drivers. `:ubereats` tracks food orders from Uber Eats or the food-order section of the Uber app. They must remain separate Android applications.

## Repository layout

```text
.
├── uber/                       # Existing upstream-derived Uber ride plugin
├── plugins/
│   ├── ubereats/app/            # Uber Eats Android application module
│   └── transitgo/app/           # Transit GO Android application module
├── shared/                      # Existing shared code used by :uber
├── systemstubs/                 # Existing hidden Android API stubs
├── .github/workflows/           # Central build and per-plugin release workflows
├── AGENTS.md                    # Codex / AI contributor entry point
├── rules.md                     # Repository-wide engineering rules
├── plan.md                      # Audit findings and roadmap
└── continue.md                  # Current development handoff
```

Each plugin has its own module documentation, rules, and handoff under `plugins/<plugin>/`. The three Android applications retain independent application IDs, notification listeners, settings, resources, versions, and release assets.

## Build

Requirements: JDK 21 and Android SDK platform 37. Run commands from the repository root.

```bash
# Existing Uber ride plugin
./gradlew :uber:assembleDebug

# Uber Eats
./gradlew :ubereats:testDebugUnitTest :ubereats:lintRelease :ubereats:assembleRelease

# Transit GO
./gradlew :transitgo:testDebugUnitTest :transitgo:lintRelease :transitgo:assembleRelease

# All maintained applications
./gradlew :uber:assembleDebug :ubereats:assembleRelease :transitgo:assembleRelease
```

APK output locations:

- Uber Eats: `plugins/ubereats/app/build/outputs/apk/`
- Transit GO: `plugins/transitgo/app/build/outputs/apk/`
- Uber rides: `apks/` (as configured by the existing module)

GitHub Actions builds the modules on pull requests and relevant pushes. CI artifacts are test builds; they are not official releases and may use a debug signing key.

## Installation and releases

Each plugin has an independent tag namespace and APK:

- Uber rides: `uber-v<version>`
- Uber Eats: `ubereats-v<version>`
- Transit GO: `transitgo-v<version>`

The matching release workflow builds and attaches only that plugin's APK. No release is published merely by merging code. Existing release history in the former standalone repositories remains available; this migration does not delete or archive those repositories.

Install only APKs from the relevant GitHub release. Do not disable Play Protect to install a plugin. A CI-signed APK may not install over a release signed with the permanent release key.

## Privacy and integration boundaries

- Each plugin reads only the source-app notifications it needs.
- Preserve the absence of Internet permission unless a separately reviewed product requirement justifies a change.
- Never log raw order or trip text, notification keys, courier/driver details, location data, or images.
- Never dismiss, modify, or cancel source-app notifications.
- Notification layouts, extras, package names, and channel identifiers owned by third-party apps are integration assumptions, not stable APIs.

See [rules.md](rules.md), [plan.md](plan.md), and [continue.md](continue.md) before contributing.

## Licenses and attribution

The repository is licensed under GPL-3.0 unless a component's own license says otherwise. The existing `:uber`, `:shared`, and `:systemstubs` modules retain their upstream attribution. See [LICENSE](LICENSE) and module-level notices.
