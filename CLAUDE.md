# Repository Release and Version Rules

This repository contains three independently released Smartspacer applications:

| Plugin | Gradle module | Tag format | Release notes |
|---|---|---|---|
| Uber rides | `:uber` | `uber-v<version>` | `.github/release-notes-uber.md` |
| Uber Eats | `:ubereats` | `ubereats-v<version>` | `.github/release-notes-ubereats.md` |
| Transit GO | `:transitgo` | `transitgo-v<version>` | `.github/release-notes-transitgo.md` |

## Versioning

- Uber rides keeps its existing versionCode scheme (for example 1.1.0 → 110).
- Uber Eats and Transit GO keep their own `versionName` and `versionCode` values in their app module.
- Never synchronize version numbers between plugins just because they share a repository.
- Update that plugin's `plugin.json` when a release is explicitly approved.

## Signing

- The maintained apps use the permanent Smartspacer plugin signing identity.
- Release workflows read `SIGNING_KEYSTORE_BASE64` and `SIGNING_PASSWORD` from repository secrets and use alias `smartspacer-plugins`.
- Never expose, download, print, or change signing secrets or keystores.
- CI/test builds may use a debug key when no release signing environment is provided. They are not official releases.

## Release approval

Never publish a release, push a version tag, modify release notes for publication, change signing configuration, or update registry metadata for a release without explicit user approval.

Before asking for approval, show the exact plugin, version, tag, full release notes, asset filename, and the result of tests/lint/build. Wait for explicit approval before creating the tag or publishing.
