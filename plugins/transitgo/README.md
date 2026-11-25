# Transit GO for Smartspacer

An unofficial Smartspacer plugin that brings an active [Transit](https://transitapp.com) GO trip to the home and lock screens.

The card shows the current instruction, route chip or step icon, next step, and arrival time or time left. Tap the card to open the trip in Transit.

**Application ID:** `dev.marco.smartspacer.transitgo`  
**Current version:** `1.3.1` (`versionCode = 10301`)  
**Minimum Android:** 10  
**Gradle module:** `:transitgo`

Not affiliated with or endorsed by Transit or Smartspacer.

## Status

This module is migrated from [INFORMATION89/Transit-GO-Smartspacer](https://github.com/INFORMATION89/Transit-GO-Smartspacer), using the `fix/privacy-safe-diagnostics` branch. The settings extraction, privacy changes, initial regression tests, and CI work are included. Real-device verification is still required before treating the migration as release-ready.

## Build and test

From the monorepo root, with JDK 21 and Android SDK 37:

```bash
./gradlew :transitgo:testDebugUnitTest
./gradlew :transitgo:lintRelease
./gradlew :transitgo:assembleRelease
```

The APK is produced under `plugins/transitgo/app/build/outputs/apk/`.

## Privacy

- Reads only Transit notifications from the `go_mode` channel.
- No Internet permission, analytics, telemetry, or remote service.
- Active trip data is kept in memory; plugin settings are local.
- Transit notifications are never dismissed, modified, or cancelled.
- Debug diagnostics report structural metadata only. No trip text or notification-layout images are logged or persisted automatically.

## Integration limitation

Transit does not expose a public GO trip API. The renderer reads private Transit `bigContentView` RemoteViews and observed resource IDs. Those are not stable APIs and may change with Transit updates. Validate actual GO trip states and card rendering on a device before publishing changes that affect this integration.

## Development notes

- [AI/contributor rules](rules.md)
- [Module AI instructions](AGENTS.md)
- [Audit and roadmap](plan.md)
- [Next steps](continue.md)
- [Release rules](CLAUDE.md)
