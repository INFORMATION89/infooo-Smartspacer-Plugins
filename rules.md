# Repository Rules

These rules apply to human and AI-assisted changes across the Smartspacer plugin monorepo. Plugin-specific rules live beside each plugin.

## Product boundaries

- This repository is exclusively for Smartspacer plugins.
- `:uber` is the Uber ride-tracking plugin. `:ubereats` is the food-order plugin. Keep their application IDs, providers, behavior, and release streams independent.
- `:transitgo` is an independent Transit GO plugin. It must not depend on Uber Eats or the Uber ride plugin.
- Preserve each plugin's Smartspacer target/provider contract, application ID, namespace, authorities, notification-listener component, preferences, and signing identity unless a reviewed change requires otherwise.
- Do not add a dependency between plugins. The legacy `:shared` module remains owned by `:uber` until a deliberate shared-code audit establishes a safe boundary.

## Gradle and build ownership

- The root wrapper, plugin repositories, and plugin versions are authoritative.
- Do not copy standalone `settings.gradle`, root `build.gradle`, `gradle.properties`, wrappers, or workflows into a plugin directory.
- Each new application module lives in `plugins/<id>/app`; include it from root `settings.gradle`.
- Keep application-specific configuration and dependencies in that module's `build.gradle.kts`.
- Avoid broad toolchain/dependency upgrades during feature work.
- Keep release tasks and tag filters scoped to the intended plugin. A plugin release must never build or upload another plugin's APK.

## Notification integration and privacy

- Filter the source package and, where applicable, notification channel before reading notification content.
- Handle notification removal and listener disconnection without leaving stale state.
- Never log raw notification titles, bodies, extras values, full notification keys, courier/driver names, order or trip details, addresses, locations, or images.
- Diagnostics may report only minimal structural metadata (for example flags, category, presence of known fields, and extra key/type names).
- Never persist notification layouts, screenshots, order data, or trip data automatically.
- Never dismiss, modify, or cancel source-app notifications.
- Keep the plugins local-first. Do not add Internet permission, networking, analytics, telemetry, remote logging, or a backend without explicit approval.

## UI and settings

- Keep every plugin's settings destination and preferences independent.
- Use Material 3 / Jetpack Compose for Compose plugins and accessible platform components for standard controls.
- Preserve established UI behavior, localization, dynamic colors, and preference defaults when refactoring.
- Separate lifecycle/navigation from screen composition, previews, reusable components, theme/tokens, and motion behavior.
- Do not turn duplicated UI into a shared library until its API, theming, lifecycle, and release implications have been reviewed.

## Strings and localization

- Plugin-owned text belongs in Android resources.
- Preserve every locale already shipped by a plugin.
- Do not translate or rewrite text supplied by third-party notification content.
- Test localized and long labels; preserve original source-app wording when parsing cannot confidently interpret it.

## Tests and CI

- Add sanitized unit tests for notification filtering, parser decisions, state changes, formatting, and edge cases.
- CI invokes the unit-test task for `:uber`, `:ubereats`, and `:transitgo`, runs release lint for all three, and builds each maintained APK independently. The current `:uber` module has no imported unit-test source set; its task is still invoked to validate Gradle task configuration. Uber Eats and Transit GO have separate unit tests.
- Do not commit real notifications, names, routes, addresses, orders, courier/driver data, or images as fixtures.
- Report device-only checks separately from automated checks. CI success is not device validation.
- Plan verification before running it: identify affected plugins, acceptance criteria, the smallest relevant checks, and any expensive CI/device checks.
- Use focused module tests, lint, and builds while iterating. Batch related changes and push one coherent update before the full pull-request workflow; avoid triggering the complete suite for every small edit.
- Reuse passing results for the same commit and relevant environment. When one CI job fails, inspect its logs and retry only that job where possible; do not rerun successful jobs without a technical reason.
- For infrastructure/environment failures, allow at most one retry with a materially different setup. If the same requirement still fails, stop and mark it BLOCKED with the evidence and required access.
- Ask before substantially expanding the agreed verification scope or spending another complete CI run after a failure.
- While real-device access is unavailable, the current routine gate is the repository CI suite. Keep live-notification and physical-device checks explicitly NOT TESTED or DEFERRED; never treat CI as proof of real notification behavior.

## Releases

- Keep tag namespaces separate: `uber-v*`, `ubereats-v*`, and `transitgo-v*`.
- Keep release notes and assets scoped to each plugin.
- Never bump a version, push a release tag, publish a GitHub release, change signing secrets, or change signing identity without explicit user approval.
- A debug-signed CI APK is a test artifact, not an official release.


## AI-assisted development and model selection

Use the smallest model that can reliably complete the task. Model choice is a routing decision, not a status symbol.

- **Sonnet or ChatGPT (balanced coding model) — default:** everyday plugin work, clear feature implementations, routine bug fixes, scoped refactors, tests, standard documentation, Gradle configuration, and fast iteration. Treat them as interchangeable execution choices for this tier; choose one primary agent based on repository access, available capability, and usage allowance. Do not duplicate the same implementation in both.
- **Opus — escalation:** complex architecture or trade-offs, reasoning across multiple plugins and shared Gradle/CI configuration, long-context work, difficult notification/lifecycle bugs, repeated failures where the current model is looping, complex third-party notification formats, or an independent high-risk review.
- **Plan with Opus, implement with Sonnet/ChatGPT:** when architecture or a difficult multi-module task warrants it, use Opus to challenge the approach and define acceptance criteria. Record the decisions in `plan.md` or the relevant plugin plan, then use Sonnet or ChatGPT for routine implementation and iteration.
- **Do not wait for Opus to begin routine work.** When an advanced review is justified, mark it **`Opus review pending`**, prepare a concise brief with the diff, relevant tests, constraints, and open questions, and leave the review outstanding until Opus actually performs it. Never imply approval before then.
- Escalate when the primary model repeats the same failed approach, cannot explain the root cause, produces contradictory changes, or the cost of another incorrect attempt is higher than the specialist review.
- Use one primary owner agent per coherent task. A second model should provide independent review or a specialist contribution, not repeat repository exploration, implementation, or full CI by default.
- Do not enable paid API usage, buy credits, upgrade plans, or incur additional charges for a model without explicit owner approval. Existing subscription allowances are finite; never silently switch to API billing.
- Model names, versions, limits, and pricing change. Check current availability rather than hardcoding a version into repository rules.

For the wider shared workflow and cost-aware verification guidance, see [Motion's AI Usage and Cost Policy](https://github.com/INFORMATION89/Motion-UI/blob/main/docs/AI_USAGE_POLICY.md).
