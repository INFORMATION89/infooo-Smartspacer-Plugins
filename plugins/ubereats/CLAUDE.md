# Uber Eats for Smartspacer — release rules

Read the root [CLAUDE.md](../../CLAUDE.md) before versioning or preparing a release.

- Gradle module: `:ubereats`.
- Application ID: `dev.marco.smartspacer.ubereats`.
- `versionName` and `versionCode` live in `app/build.gradle.kts`.
- This plugin uses its own release tag prefix: `ubereats-v<version>`.
- Release notes: root `.github/release-notes-ubereats.md`.
- Release workflow: root `.github/workflows/release-ubereats.yml`.
- Releases use the permanent `smartspacer-plugins` signing identity from repository secrets.

Never bump a version, edit release notes for publication, push a tag, or publish a release without explicit approval. Show the exact version, tag, complete release notes, and APK asset name first.
