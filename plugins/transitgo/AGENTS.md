# Transit GO plugin instructions

Read this file together with the root [AGENTS.md](../../AGENTS.md), [rules.md](rules.md), [plan.md](plan.md), and [continue.md](continue.md).

- Preserve application ID `dev.marco.smartspacer.transitgo`, the Smartspacer target contract, listener component, settings, and local preferences.
- Read only Transit package `com.thetransitapp.droid` notifications from channel `go_mode`.
- Treat `bigContentView` and Transit RemoteViews resource IDs as private, unstable integration assumptions. Keep IDs centralized and all fields optional.
- Never log trip text, routes, directions, times, notification keys, or images. Never persist notification layouts automatically.
- Keep fixtures synthetic. Expand tests for listener lifecycle, parsing boundaries, missing RemoteViews IDs, and state transitions.
- Preserve all shipped plugin locales and do not guess translations for Transit-owned notification text.
- Keep this module buildable as `:transitgo` from the monorepo root.
- Require physical-device validation for layout, density, and live trip behavior.
- Never change versioning, signing, or releases without approval.
