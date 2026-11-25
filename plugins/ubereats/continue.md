# Continue development

This file is the handoff for the next coding session. Follow [rules.md](rules.md) throughout; use [plan.md](plan.md) for the full audit findings and acceptance criteria. Release/versioning instructions are in [CLAUDE.md](CLAUDE.md).

## Current state

- Repository: `INFORMATION89/infooo-Smartspacer-Plugins`
- Module: `:ubereats` (`plugins/ubereats/app`)
- Working branch: `migration/add-ubereats-transitgo-monorepo`
- Imported source: `INFORMATION89/UberEats-Smartspacer` at `3d1e6fea2e86945e9276086a2310e0565112b9c0`
- Application version: `0.1.1` (`versionCode = 101`); retain the alpha status until a real order has been tested.
- Uber Eats debug diagnostics were sanitized during migration: raw extra values and full notification keys are no longer logged; parser failures log only the exception type; removal events are filtered to supported Uber packages.
- Monorepo Build plugin suite [#3](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/actions/runs/45716316251) passed `:ubereats:testDebugUnitTest`, `:ubereats:lintRelease`, `:ubereats:assembleRelease`, and the `uber-eats-plugin` artifact upload.
- A pure-Kotlin `BoundedNotificationKeyStoreTest` suite now tests remembered-key expiry, capacity limits, refresh, removal and clearing. Full parser/scoring regression tests are still required; Build suite #10 is validating the added tests.

## How to resume

1. Read the root [AGENTS.md](../../AGENTS.md), [rules.md](../../rules.md), [plan.md](../../plan.md), and [continue.md](../../continue.md), then the module's [AGENTS.md](AGENTS.md), [rules.md](rules.md), [plan.md](plan.md), and [CLAUDE.md](CLAUDE.md).
2. Work on a focused branch based on the current monorepo. Never edit `main` directly.
3. Run `./gradlew :ubereats:testDebugUnitTest :ubereats:lintRelease :ubereats:assembleRelease` from the repository root.
4. Keep changes focused and update this handoff with exact verification results.

## Implementation order

### Phase 1 — Remove sensitive debug output <sub>Implemented; device Logcat check remains</sub>

- `OrderReader.logNotification()` now logs only package, flags, category, channel, presence of title/body fields, and extra key/type names.
- Raw notification extra values and the full notification key are no longer logged.
- Parser failures log the exception type only; no exception message or stack trace is forwarded to Logcat.
- Notification removal callbacks filter package before forwarding the key.

**Remaining validation:** inspect Logcat in a debug installation using synthetic notifications. Do not share logs containing real order data.

### Phase 2 — Add parser and state tests

The remembered Uber food-order key store is bounded to 32 keys with a 12-hour TTL and has tests for expiry, capacity, refresh, and clearing. The candidate map is capped at 16 entries and drops entries older than 12 hours when a new notification event is processed.

Still add a testable parser boundary and sanitized fixtures covering:

- A live order from the Uber Eats app.
- A food order placed through the Uber app.
- An ordinary Uber ride (must not become a food-order card).
- A promotion or unrelated notification (must be ignored).
- An order update containing only an ETA / short critical text.
- Multiple candidate notifications and candidate scoring.
- Notification removal, listener disconnection, and state clearing.
- Repeated notifications that have no visible change but have a refreshed tap action.

Prefer pure Kotlin tests for scoring, filtering, parsing decisions, and state comparison. Avoid committing real notification payloads, names, addresses, order IDs, or photos.

**Gate:** every expected classification has a regression test before changing the current heuristics.

### Phase 3 — Make notification parsing more resilient

- Centralize package identifiers, notification extra keys, category values, flags, and scoring assumptions.
- Prefer structured notification metadata; retain text matching only where necessary.
- Test the English and Spanish forms that the plugin claims to support, plus source-app text in other languages where metadata is sufficient.
- Handle missing or malformed extras without crashing or leaving stale order state.
- Do not add network access or attempt to reverse-engineer private APIs beyond the notification information already available.

**Gate:** parser tests pass and the behavior for Uber rides, promotions, and food orders remains distinct.

### Phase 4 — Split and simplify the settings UI

Refactor `SettingsActivity.kt` (1,121 lines at audit) without redesigning the screen:

1. Keep Activity lifecycle and Android navigation in `SettingsActivity`.
2. Move screen composition and list structure into a focused settings screen file.
3. Separate the preview card and its presentation helpers.
4. Separate settings rows, icon badges, and section headers.
5. Separate pull-to-open gesture/animation code.
6. Move theme and typography into their own file.
7. Centralize repeated dimensions, shapes, colors, and animation values in a small Compose token layer.

Continue using Material 3 for ordinary controls and surfaces. Preserve dynamic colors, Google Sans Text, localization, live preview, setting persistence, and the intentional Source code interaction. Do not replace the entire UI or extract a shared library with Transit GO.

**Gate:** compare before/after screenshots in light, dark, and dynamic-color themes; settings and preview behave as before.

### Phase 5 — Improve automated verification

- Add a testable parser boundary and sanitized unit tests for food-vs-ride classification, candidate scoring, ETA-only updates, removal, and listener reconnection.
- The monorepo workflow already runs `:ubereats:testDebugUnitTest`, `:ubereats:lintRelease`, and `:ubereats:assembleRelease`.
- Keep the APK build step scoped to this module.
- Run the three commands locally and in CI:
  - `./gradlew :ubereats:testDebugUnitTest`
  - `./gradlew :ubereats:lintRelease`
  - `./gradlew :ubereats:assembleRelease`

**Gate:** all available checks pass on the pull request. If an Android-dependent test cannot run in the CI environment, document the limitation instead of silently skipping it.

### Phase 6 — Validate with a real order

The existing alpha release notes explicitly say real-order validation has not been completed.

- Install a test build on a device with Uber Eats and/or Uber installed.
- Verify a real food order appears and updates through preparing, pickup, en route, ETA, and completion states as available.
- Verify a regular Uber ride and promotional notifications do not appear as food orders.
- Verify tapping the card opens the correct source app/order.
- Verify notification access can be revoked and restored without leaving a stale card.
- Compare courier-photo and fallback-icon behavior.
- Do not publish a new release until this validation is recorded.

## Final review

Before declaring the work complete:

- Confirm no Internet permission, network client, analytics, or telemetry was added.
- Confirm only the expected source-app notifications are read.
- Confirm source notifications are never cancelled or modified.
- Confirm all plugin-owned UI strings remain in Android resources and English/Spanish translations stay in sync.
- Confirm the README no longer advises users to disable Play Protect.
- Review the full diff and report unverified device-only behavior.
- Update this file with completed phases and remaining work.

## Release boundary

Do not bump versions, push tags, edit release notes for publication, change signing configuration, or publish a GitHub release without explicit approval. Follow [CLAUDE.md](CLAUDE.md) and ask before any release action.
