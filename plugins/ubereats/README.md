# Uber Eats for Smartspacer

An unofficial Smartspacer plugin that brings supported live food orders from the Uber Eats app or the food-order section of the Uber app to the home and lock screens.

The card can show order status, restaurant/courier details, ETA or progress, and the courier photo when available. Tap the card to open the source order.

**Application ID:** `dev.marco.smartspacer.ubereats`  
**Current version:** `0.1.1` (`versionCode = 101`, alpha)  
**Minimum Android:** 10  
**Gradle module:** `:ubereats`

Not affiliated with or endorsed by Uber, Uber Eats, or Smartspacer.

## Status

This module is migrated from [INFORMATION89/UberEats-Smartspacer](https://github.com/INFORMATION89/UberEats-Smartspacer). The standalone `v0.1.1` release notes state that the build has not yet been tested with a real food order. Do not describe live-order behavior as device-validated until both source-app paths have been tested.

## Build and test

From the monorepo root, with JDK 21 and Android SDK 37:

```bash
./gradlew :ubereats:testDebugUnitTest
./gradlew :ubereats:lintRelease
./gradlew :ubereats:assembleRelease
```

The APK is produced under `plugins/ubereats/app/build/outputs/apk/`.

## Privacy

- Reads notifications only from `com.ubercab.eats` and `com.ubercab`.
- The Uber app path must distinguish food orders from rides; do not turn ordinary ride notifications into food-order cards.
- No Internet permission or remote data service.
- Order state stays in memory; plugin preferences are local.
- Source-app notifications are never dismissed, modified, or cancelled.
- Debug diagnostics contain structural metadata only. They must never print notification text, extras values, full notification keys, courier details, or images.

## Development notes

- [AI/contributor rules](rules.md)
- [Module AI instructions](AGENTS.md)
- [Audit and roadmap](plan.md)
- [Next steps](continue.md)
- [Release rules](CLAUDE.md)
