# Uber Eats plugin instructions

Read this file together with the root [AGENTS.md](../../AGENTS.md), [rules.md](rules.md), [plan.md](plan.md), and [continue.md](continue.md).

- This is the food-order plugin, not the Uber ride plugin. Preserve application ID `dev.marco.smartspacer.ubereats`.
- Keep support for `com.ubercab.eats` and food orders posted by `com.ubercab`; ordinary Uber rides and promotions must be ignored.
- Filter package before reading notification content. On removal, ignore unrelated packages before forwarding a notification key.
- Never log notification text, extras values, full keys, order details, courier names, or images.
- Keep parsing/scoring assumptions centralized and covered by synthetic tests. Do not use live user notifications as fixtures.
- Keep settings behavior and stored preferences stable while refactoring the large settings screen.
- Keep this module buildable as `:ubereats` from the monorepo root.
- Never change versioning, signing, or releases without approval.
