# Uber ride plugin instructions

Read the root [AGENTS.md](../AGENTS.md), [rules.md](../rules.md), and this directory's [rules.md](rules.md) before changing this module. Also read the root `plan.md` and `continue.md`; they are the planning and handoff documents for this plugin unless a local replacement is added.

- This is the Uber ride-tracking plugin, not Uber Eats. Keep it independent from `:ubereats` and `:transitgo`.
- Smartspacer owns notification access and delivery through `UberNotificationProvider`. Do not add a separate notification listener service or duplicate notification permission UI.
- Preserve the `SmartspacerNotificationProvider` and `UberTarget` contracts and the existing application ID, provider authority, package filtering, and per-Target-instance configuration.
- Configuration is keyed by `smartspacerId`. Never convert `showExpandedInfo` or `hideNotification` into global preferences.
- Keep notification parsing and trip-state handling in the existing provider/repository boundary. Use the already extracted normalized progress value; never invent or time-advance trip progress.
- Settings presentation uses the shared `:settings-ui` module and Material 3 Expressive. Keep preference semantics and the official Smartspacer target renderer for previews.
- A preview must omit source notification keys, notification metadata, pending intents, and all real actions. Use live target data when available and clearly labelled synthetic data only when no live trip exists.
- Never log notification text, extras values, full notification keys, trip identifiers, names, locations, or images. Keep fixtures synthetic.
- Keep this module independently buildable as `:uber`. Do not change versions, signing, tags, or releases without explicit approval.
