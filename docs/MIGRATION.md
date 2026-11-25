# Migration Notes

## Source repositories and imported revisions

| Plugin | Source | Imported revision |
|---|---|---|
| Uber Eats | [INFORMATION89/UberEats-Smartspacer](https://github.com/INFORMATION89/UberEats-Smartspacer) | `3d1e6fea2e86945e9276086a2310e0565112b9c0` (`main`) |
| Transit GO | [INFORMATION89/Transit-GO-Smartspacer](https://github.com/INFORMATION89/Transit-GO-Smartspacer) | `23cdbd42ce508f93e759bc7202c45837ff8a2e2b` (`fix/privacy-safe-diagnostics`) |

The base monorepo already contained `:uber`, `:shared`, and `:systemstubs`. The imported Uber Eats plugin is not the same product as the original Uber ride-tracking module.

## Layout mapping

- Standalone `app/` becomes `plugins/<plugin>/app/`.
- Plugin `README.md`, `AGENTS.md`, `rules.md`, `plan.md`, `continue.md`, and `CLAUDE.md` live beside the app module.
- Registry metadata is stored in `plugins/<plugin>/plugin.json`.
- Release notes are namespaced under `.github/release-notes-<plugin>.md`.
- Root Gradle configuration and GitHub Actions are owned by this repository.

## History and provenance

The source working trees are imported as new files in the monorepo. Their original Git histories remain available in the source repositories, but their commit graphs are not rewritten into this repository by this file-level migration. Source commits and branch names are recorded above so that the origin of each imported tree can be audited.

The original standalone repositories are intentionally retained. This migration does not archive, delete, or rewrite them.

## Deliberate changes during import

- Uber Eats notification diagnostics were sanitized. The logger no longer prints raw extras values or the full notification key. Parser failures log only the exception type, and removal callbacks filter to supported Uber packages before forwarding a key.
- Transit GO is imported from the branch that already contains privacy hardening, the settings extraction, initial regression tests, and a standalone CI pipeline.
- The standalone wrappers, root build files, settings files, and per-repository workflows were not copied. They are replaced by one monorepo root build and per-plugin release workflows.
