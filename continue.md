# Continue development

Read [AGENTS.md](AGENTS.md), [rules.md](rules.md), [plan.md](plan.md), and [CLAUDE.md](CLAUDE.md) before changing code. Then read the module-specific handoff for the plugin being changed.

## Current migration

- Repository: `INFORMATION89/infooo-Smartspacer-Plugins`
- Working branch: `migration/add-ubereats-transitgo-monorepo`
- Base: `main`
- Source for Transit GO: `INFORMATION89/Transit-GO-Smartspacer`, branch `fix/privacy-safe-diagnostics` (includes the privacy, settings extraction, and CI work from its draft PR #1).
- Source for Uber Eats: `INFORMATION89/UberEats-Smartspacer`, branch `main`.
- Original repositories remain intact. Do not archive or delete them during this migration.
- The existing `:uber`, `:shared`, and `:systemstubs` modules are retained unchanged.
- The source repositories' Gradle wrappers, root build files, settings files, and per-repository workflows are intentionally not copied. The monorepo owns those concerns.

## Work completed in this branch

- Imported the current Transit GO application source, resources, tests, and module documentation under `plugins/transitgo/`.
- Imported the current Uber Eats application source, resources, and module documentation under `plugins/ubereats/`.
- Sanitized Uber Eats debug notification diagnostics during import: no raw extras values or full notification keys are logged; parser failures log only the exception type; removal callbacks filter to the two supported Uber packages.
- The module source has not yet been built from the monorepo root. Do not mark the migration complete until root Gradle wiring, CI, lint, release builds, and APK outputs are validated.

## Next steps

1. Wire `:ubereats` and `:transitgo` into root `settings.gradle` with project directories under `plugins/`.
2. Register the Kotlin Compose compiler plugin centrally in root `build.gradle`; keep the current root Gradle wrapper and Android/Kotlin versions.
3. Add plugin-specific registry metadata and concise module READMEs.
4. Consolidate build/test/lint CI. The workflow must build `:uber`, `:ubereats`, and `:transitgo` independently and upload separate artifacts.
5. Add independent signed release workflows using `ubereats-v*` and `transitgo-v*`. Do not publish tags or releases.
6. Run Gradle sync, unit tests, lint, and all APK builds from a clean clone. Fix root-cause failures.
7. Review package IDs, manifests, permissions, signing, resources, version metadata, and release assets.
8. Complete the remaining technical debt in [plan.md](plan.md), especially Uber Eats parser tests and settings modularization.
9. Update this file with exact CI run IDs and device validation evidence.
10. Once the monorepo migration is accepted, add a short moved-project notice to each standalone repository. Do not delete or archive either source repo without separate approval.

## Verification commands

```bash
./gradlew :uber:assembleDebug
./gradlew :ubereats:testDebugUnitTest :ubereats:lintRelease :ubereats:assembleRelease
./gradlew :transitgo:testDebugUnitTest :transitgo:lintRelease :transitgo:assembleRelease
```

## Device validation still required

- Uber Eats: real food order notifications from both Uber Eats and the Uber app, ETA-only updates, ride/promotion rejection, notification removal, and settings persistence.
- Transit GO: active GO trip updates, route chip/step icon variants, trip completion, listener disconnection, and settings appearance/interactions.
- Test at more than one screen size/density where rendering depends on notification RemoteViews.
