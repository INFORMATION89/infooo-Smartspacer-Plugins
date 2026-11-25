# Continue development

This file is the handoff for the next coding session. Follow [rules.md](rules.md) throughout; use [plan.md](plan.md) for the full audit findings and acceptance criteria. Release/versioning instructions are in [CLAUDE.md](CLAUDE.md).

## Current state

- Repository: `INFORMATION89/infooo-Smartspacer-Plugins`
- Module: `:transitgo` (`plugins/transitgo/app`)
- Working branch: `migration/add-ubereats-transitgo-monorepo`
- Imported source: `INFORMATION89/Transit-GO-Smartspacer` branch `fix/privacy-safe-diagnostics`, commit `23cdbd42ce508f93e759bc7202c45837ff8a2e2b`
- Code baseline originally audited: `6b0cf834385c9da76e177d6089cd702d7a9a7bf7` (2026-11-17)
- Application version at audit: `1.3.1` (`versionCode = 10301`)
- Latest published stable release at audit: `v1.3.1`.
- **Phase 1 — privacy:** implemented. Automatic notification layout dumps and `LayoutDumper.kt` are gone; trip-specific Logcat output was removed or sanitized. Static review found no remaining references to the dumper or legacy raw-content logging helpers.
- **Phases 3–4 — integration and rendering hardening:** code changes are implemented. The package/channel/RemoteViews assumptions are centralized, the fixed 1080 px width was replaced with a display-resource-based width, rendered bitmaps are capped at 512 px, and the ID cache is synchronized. Device-level behavior is not yet verified.
- **Phase 6 — settings architecture:** extraction is complete. The Activity, Compose coordinator, preview, reusable components, motion/gesture behavior, theme/typography, and visual tokens now live in focused files; no second settings destination was added.
- **Standalone verification:** unit tests, Android Lint, and release APK assembly passed in the source repository's Build workflow #48 after the refactor.
- **Monorepo verification:** passed in Build plugin suite [#3](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/actions/runs/45716316251): `:transitgo:testDebugUnitTest`, `:transitgo:lintRelease`, `:transitgo:assembleRelease`, and the `transit-go-plugin` artifact upload all succeeded.
- **Still open:** extend parser/lifecycle coverage, verify localized bike wording only against real Transit strings, and perform device checks in Phase 8. Standalone CI success does not replace monorepo or device validation.

## How to resume

1. Read root [AGENTS.md](../../AGENTS.md), [rules.md](../../rules.md), [plan.md](../../plan.md), and [continue.md](../../continue.md), then the module's [AGENTS.md](AGENTS.md), [rules.md](rules.md), [plan.md](plan.md), and [CLAUDE.md](CLAUDE.md).
2. Continue on a focused branch based on the monorepo; never edit `main` directly.
3. Run `./gradlew :transitgo:testDebugUnitTest :transitgo:lintRelease :transitgo:assembleRelease` from the repository root.
4. Capture settings and GO card screenshots during device validation, review the full diff, and update this handoff with evidence.

## Implementation order

### Phase 1 — Stop automatic trip-data diagnostics <sub>Implemented; runtime confirmation remains part of Phase 8.</sub>

- `LayoutDumper.kt` and automatic full-layout captures have been removed.
- Trip titles, route names, directions, times, notification keys, and images are not written to normal Logcat diagnostics.
- Static review found no remaining references to the deleted layout dumper or legacy raw-content logging helpers.
- The manifest declares no Internet permission.

**Status:** implementation complete. Confirm runtime behavior once on a debug installation during Phase 8; do not reintroduce automatic notification captures.

### Phase 2 — Add focused tests before changing the integration

Implemented partially in `TransitGoLogicTest.kt`: package/channel filtering; foreground-service versus group-summary classification; English/Spanish bike count formatting; unknown-format fallback; current-notification removal matching; and visible-content comparison. The suite does **not** yet exercise listener disconnection/rebinding or RemoteViews parsing. Add a small parsing boundary before attempting to test private Transit view layouts; keep fixtures synthetic and data-free.

Cover with sanitized fixtures/tests:

- Package and `go_mode` channel filtering.
- Live-trip detection versus group summaries and one-off notifications.
- Notification removal and listener disconnection.
- Bike-station values: zero, one, few, and ordinary counts.
- English and Spanish wording currently recognized by `withBikesLeft()`.
- Missing, renamed, hidden, or wrong-type RemoteViews elements.
- Card/state comparison when notification data changes and when only the tap action refreshes.

Prefer pure Kotlin unit tests for parsing and formatting. Isolate Android `RemoteViews` access behind a small boundary so the rest can be tested. Do not commit real notification payloads, location names, route history, or images.

**Gate:** core decision logic is covered. Listener lifecycle and RemoteViews parsing remain open and must not be marked complete until dedicated tests exist.

### Phase 3 — Harden the Transit notification integration

Implemented in code:
- `TransitNotificationContract.kt` owns the Transit package, GO channel, observed RemoteViews IDs, measurement inset, and bitmap limit.
- Notification extras supply the ordinary title and text; RemoteViews inspection is used for the extra route, step, and timing details that are not available in those fields.
- Extracted views and text are optional. Missing IDs, unexpected view types, and rendering exceptions degrade the card instead of escaping the renderer.
- `bigContentView` and the observed IDs are documented as private Transit implementation details, not a stable API.

**Remaining validation:** exercise missing/renamed layout elements and normal GO states against real Transit builds. Record the Transit version, Android version, device, and density; do not close this phase based only on CI.

### Phase 4 — Make bitmap rendering deliberate

- Implemented: removed the fixed 1080-pixel measure width and use available display-resource width with the configured horizontal inset.
- Implemented: cap rendered bitmaps at 512 px and preserve the square icon/chip drawing behavior.
- Still required: verify text wrapping, clipping, route chips, and step icons on at least two screen widths/densities. Notification RemoteViews may have a different target width from the app window, so visual behavior cannot be inferred from the code alone.

**Gate:** rendered chips and icons remain sharp, correctly proportioned, and useful at the tested densities and widths.

### Phase 5 — Review localized bike-station wording

- Verify the actual bike-count forms used by Transit in the languages the plugin advertises.
- Add tests for each verified form and correct singular/plural behavior.
- Keep the source notification's original wording when an unsupported language or unknown format is encountered.
- Do not guess translations or silently rewrite Transit-provided text.
- Keep all plugin-owned strings and sample data in sync across the ten supported locale resource sets.

**Gate:** verified forms are correct, and unknown forms safely fall back to the original text.

### Phase 6 — Split and simplify the settings UI

The settings extraction is implemented. Keep the existing plugin-owned settings destination; do not add another Activity. Current responsibilities are split across `SettingsActivity.kt` (Android lifecycle and notification-access navigation), `TransitGoSettingsScreen.kt` (Compose screen coordination), `SettingsPreview.kt` (preview), `SettingsComponents.kt` (reusable UI), `SettingsMotion.kt` (pull gesture/animation), `SettingsTheme.kt` (dynamic colors and typography), and `TransitGoSettingsTokens.kt` (visual tokens).

The remaining work in this phase is validation, not another broad refactor: compare screenshots and interactions before/after, and verify light, dark, dynamic-color themes, long localized labels, preference persistence, and notification-access navigation.

The extraction checklist is complete: Activity lifecycle, Compose screen coordination, preview, shared rows/components, pull gesture and animation, theme/typography, and named UI tokens are separated. Continue using Material 3 for ordinary controls and surfaces. Preserve dynamic colors, Google Sans Text, all existing translations, preview behavior, setting persistence, and the intentional Source code interaction. Do not replace the UI or extract a shared library with Uber Eats.

**Remaining validation:** compare the current screen against the pre-refactor behavior; verify light, dark, and dynamic-color themes, long localized labels, preference persistence, notification-access navigation, and the Source code gesture on a device.

### Phase 7 — Improve automated verification

- Implemented: CI runs `testDebugUnitTest`, `lintRelease`, and `assembleRelease` for pull requests and pushes to `main`.
- Verified in the monorepo: unit tests, Android Lint, release APK assembly, and the `transit-go-plugin` artifact upload passed in [Build plugin suite #3](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/actions/runs/45716316251).
- Add tests for listener lifecycle, parsing boundaries, and RemoteViews edge cases as those seams are introduced.

**Gate:** the module's CI tasks pass in the root workflow. Device-only behavior must remain explicitly listed as unverified until tested.

### Phase 8 — Validate with Transit GO on a device

- Test active GO trips that include waiting, riding, walking, route-chip, and bike-station states where available.
- Verify the tap action opens the correct trip.
- Verify the card updates on each step and disappears when GO ends or notification access is revoked.
- Check that no stale trip remains after Transit stops or the listener disconnects.
- Re-test after Transit app updates, because the renderer depends on its private notification layout.
- Record Transit app version, Android version, and device/density for each validation.

## Final review

Before declaring the work complete:

- Confirm the plugin still requests no Internet permission.
- Confirm only the Transit GO channel is read.
- Confirm source notifications are never cancelled or modified.
- Confirm no raw route/trip content is logged or automatically persisted.
- Confirm all ten plugin locales and sample resources remain consistent.
- Confirm the README no longer advises users to disable Play Protect.
- Review the full diff and report unverified device-only behavior.
- Update this file with completed phases and remaining work.

## Release boundary

Do not bump versions, push tags, edit release notes for publication, change signing configuration, or publish a GitHub release without explicit approval. Follow [CLAUDE.md](CLAUDE.md) and ask before any release action.
