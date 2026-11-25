# Maintenance and Repair Plan

## Current monorepo status — 2026-11-25

The migration PR [#2](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/pull/2) is merged. The imported application remains `:transitgo` at version `1.3.1` (versionCode 10301). Do not use the former migration branch as the development baseline; branch from the current monorepo `main`.

The final migration validation [Build plugin suite #21](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/actions/runs/45719944978) passed all three unit-test tasks, lint for all three apps, all three builds, and artifact uploads. The subsequent main build [#45721585024](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/actions/runs/45721585024) also passed.

The migration-era suite #10/#12 references below are historical, not running validations. The Settings UI extraction, privacy-diagnostic cleanup, and renderer hardening described below are already in the imported code. The current module-specific handoff is [continue.md](continue.md); remaining work is principally listener/RemoteViews test coverage and physical-device validation. The old standalone README's Play Protect wording is not present in the monorepo README.

## Current implementation progress

The source tree was imported by the merged monorepo migration PR #2; continue from the current monorepo main branch. Source: `INFORMATION89/Transit-GO-Smartspacer`, branch `fix/privacy-safe-diagnostics`, commit `23cdbd42ce508f93e759bc7202c45837ff8a2e2b`. No release version, signing configuration, or official release has been changed.

- **Privacy diagnostics — implemented in source:** automatic layout dumps and `LayoutDumper.kt` were removed; trip content is no longer written to normal Logcat diagnostics.
- **Logic tests — partially complete:** `TransitGoLogicTest.kt` covers package/channel filtering, live-trip versus group-summary flags, English/Spanish bike wording, unknown wording fallback, notification-removal matching, and visible-content comparison. Listener lifecycle and RemoteViews parsing still need dedicated coverage.
- **Integration/rendering code — implemented, device gate open:** `TransitNotificationContract.kt` centralizes package/channel and observed RemoteViews IDs. `ChipRenderer` uses display-resource width, caps output bitmaps at 512 px, synchronizes the ID cache, and avoids stack traces in logs. Validate the actual RemoteViews behavior on devices before closing this work.
- **Settings architecture — extraction complete, visual gate open:** `SettingsActivity.kt`, `TransitGoSettingsScreen.kt`, `SettingsPreview.kt`, `SettingsComponents.kt`, `SettingsMotion.kt`, `SettingsTheme.kt`, and `TransitGoSettingsTokens.kt` have focused responsibilities. The existing single settings destination is preserved.
- **Source CI — passed:** the standalone branch completed unit tests, Android Lint, and release APK assembly in Build run [#48](https://github.com/INFORMATION89/Transit-GO-Smartspacer/actions/runs/45710785597).
- **Monorepo CI — passed:** Build plugin suite [#10](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/actions/runs/45717957032) completed successfully, including tests, lint across all three apps, all APK builds, and the `transit-go-plugin` artifact upload. Final pre-merge suite #21 and the subsequent main run #45721585024 passed. Treat suite #10/#12 as historical migration runs.
- Earlier intermediate compile failures in the source branch were corrected; they are historical and are not evidence of the new monorepo build status.

## Baseline

- Source repository: `INFORMATION89/Transit-GO-Smartspacer` (imported into `plugins/transitgo/`)
- Audited branch: `main`
- Audited commit: `6b0cf834385c9da76e177d6089cd702d7a9a7bf7` (2026-11-17)
- Scope: static review of the repository tree, Kotlin/Compose implementation, manifest, resources, Gradle configuration, README, and GitHub Actions.
- This is a repair plan, not permission to publish a release or change the app's version.

## Summary

The plugin has a reasonable native Android foundation: Kotlin, Jetpack Compose, Material 3 settings, dynamic colors on Android 12+, Google Sans Text, and a narrowly filtered Transit GO notification listener. The manifest does not request Internet access. The latest Build and Release workflow runs for the audited commit succeeded.

The settings UI is not entirely hand-drawn. It uses Material 3 components, with custom Compose animation, blur, icon treatment, preview rendering, and a pull-to-open Source code interaction layered around them. The aim should be to simplify and organize that custom layer, not replace the whole screen.

The largest functional fragility is the dependency on Transit's private notification RemoteViews layout and resource IDs. Privacy-sensitive debug dumps, the monolithic settings file, missing automated tests, and CI that only builds are the other main issues.

## Findings

### P1 — Debug diagnostics persist full trip notification layouts

`app/src/main/java/dev/marco/smartspacer/transitgo/TransitGoNotifications.kt`, `TripReader.update()`, calls `LayoutDumper.dump()` for new layout shapes in debuggable builds. `LayoutDumper.kt` writes text, view metadata, image PNGs, and a full-layout PNG to `context.filesDir/dump` (around lines 18–40 and 46–66). The notification can contain route names, directions, times, and other trip context. The regular debug Logcat messages also include title, text, route, arrival, time-left, and previous-step text.

This is debug-only, not the normal release path, but files are persistent app-private data and there is no cleanup/retention control.

**Repair:** remove automatic full-layout dumping from the normal listener path. Prefer sanitized structural diagnostics. If full captures are still needed for active development, require an explicit developer opt-in, keep it debug-only, show/document where captures are stored, bound the number/age of files, and provide a reliable deletion path. Never include real trip data in committed fixtures.

### P1 — The card renderer depends on private Transit view IDs

`ChipRenderer.kt` inflates `Notification.bigContentView` and looks up IDs such as `floatingServiceName`, `floatingServiceNameContainer`, `text`, `image`, `leftTextView`, `rightTextView`, and `previous_instruction` by name (around lines 47–82 and 86–90). These are implementation details of the Transit app, not a stable public API. `bigContentView` is deprecated for notification producers, but is currently used here to read another app's custom expanded layout.

This approach is understandable because Transit GO has no public trip API, but it can break silently when Transit changes its notification layout.

**Repair:** isolate the expected package, channel, RemoteViews IDs, and rendering assumptions in a documented integration contract. Handle every missing ID/view/type safely and keep the card useful when optional chip, step, or timing views disappear. Add tests for missing/renamed IDs and malformed RemoteViews where feasible. Do not pretend this private layout is a stable API.

### P2 — Renderer and layout dumper use a fixed 1080-pixel measure width

`ChipRenderer.kt` and `LayoutDumper.kt` both measure the notification layout at exactly 1080 pixels. That is a hidden rendering assumption and may affect text layout or clipping on devices with different densities or sizes.

**Repair:** document why a fixed width is needed, then replace it with a deliberate width strategy based on the available display/resources or a tested canonical rendering size. Verify the resulting chip and step bitmaps on more than one density/width. Avoid changing it without visual regression checks.

### P2 — Settings UI is concentrated in one very large file and duplicated in Uber Eats

`app/src/main/java/dev/marco/smartspacer/transitgo/SettingsActivity.kt` is 1,118 lines. It combines Activity lifecycle, screen/list composition, settings state, preview UI, animations, pull gestures, reusable rows, icon rendering, theme selection, and typography.

The Uber Eats repository contains an almost identical settings-screen structure and the same general UI components, with product-specific preview and option differences.

**Repair:** split the screen into focused files: Activity/lifecycle, SettingsScreen, preview, reusable rows, gesture/animation helpers, and theme/tokens. Keep product-specific previews and options separate. Continue to use Material 3 for standard controls. Do not make Transit GO depend on the Uber Eats repository or introduce a third shared library as part of this cleanup.

### P2 — Bike-station wording supports only two detected forms

`TripReader.withBikesLeft()` recognizes English `bike/bikes` and Spanish `bici/bicis` (around lines 179–200). The README advertises ten plugin UI locales, while this derived notification enhancement only handles two word forms.

**Repair:** add sanitized tests for supported Transit notification languages and bike counts (zero, one, few, and normal). Either support the observed localized terms and correct plural forms or explicitly leave unsupported source-app wording unchanged. Do not guess translations or change text in a way that makes the source notification misleading.

### P2 — No automated tests or static analysis are wired into CI

The audited tree contains no `src/test` or `src/androidTest` source set. The Build workflow runs `assembleRelease`; it does not run unit tests or Android lint.

**Repair:** add unit tests for notification filtering, foreground-service/live-trip detection, state comparison, bike-count formatting, and optional RemoteViews parsing. Add test and lint tasks to the Build workflow while retaining the release APK build.

### P2 — Installation instructions recommend disabling Play Protect

The README provides an ADB installation route but also recommends turning off Play Protect scanning temporarily.

**Repair:** remove the recommendation to disable device protection. Keep the ADB installation path and tell users to install only an APK obtained from this repository's trusted release page. Do not ask users to weaken device security to install the plugin.

### P3 — Hardcoded integration/UI values need clear ownership

Some literals are necessary: Transit package name, GO notification channel, Android notification extras, and the source app's internal RemoteViews IDs. Other values are presentation decisions: the 1080-pixel render size, dimensions, shapes, animation timings, and accent palette.

**Repair:** centralize integration assumptions and Compose UI tokens in named, documented places. Keep plugin-owned UI text in Android resources and maintain all supported locale files. Do not indiscriminately move every number into XML or translate the source app's own notification text.

## Recommended implementation order

1. **Establish a baseline.** Build the current code and record the settings screen plus representative GO card states (waiting, riding, walking, and bike station). Preserve the current version/release configuration.
2. **Reduce diagnostic data exposure.** Disable automatic full-layout dumps and sanitize trip-related Logcat output.
3. **Add parser/renderer tests.** Cover notification filtering, live-trip identification, bike counts, missing RemoteViews IDs, and state changes using sanitized fixtures.
4. **Harden the Transit integration boundary.** Centralize the GO package/channel and private layout IDs, add safe fallbacks, and document the assumptions.
5. **Validate rendering dimensions.** Replace or explicitly justify the fixed 1080-pixel width and test multiple densities and screen widths.
6. **Refactor the settings UI.** Split the large file into focused files and add a small token/theme layer. Preserve the current Material 3 look and intentional interactions.
7. **Improve CI.** Run unit tests and lint on pull requests and pushes, in addition to building the release variant.
8. **Fix README installation guidance.** Remove instructions to turn off Play Protect and keep the safe ADB path.
9. **Review the result.** Compare screenshots and card states, inspect the final diff, and report any behavior that requires a real Transit GO session to validate.

## Acceptance criteria

- The settings UI remains Material 3-based and preserves the existing dynamic-color/Google Sans theme.
- No raw trip text or full notification layout is automatically logged or persisted.
- The card degrades gracefully when Transit changes or omits optional RemoteViews elements.
- The renderer is tested at multiple display densities/widths.
- Bike-count wording is correct for verified supported forms and safely unchanged for unknown forms.
- All plugin-owned strings remain resource-based and all supported locale resources are kept in sync.
- The manifest continues to request no Internet permission and the listener reads only Transit GO notifications.
- `assembleRelease`, unit tests, and lint pass in CI.
- No release, tag, version bump, signing-key change, or release-note publication is made without explicit approval.

## Non-goals

- Do not rewrite the plugin from scratch.
- Do not assume private Transit RemoteViews IDs are a supported API.
- Do not add networking, analytics, telemetry, or a backend.
- Do not change the Smartspacer target contract, package/application ID, notification-access model, or release signing identity without a demonstrated need and explicit review.
- Do not publish a release. Follow `CLAUDE.md` for versioning and release approval.
