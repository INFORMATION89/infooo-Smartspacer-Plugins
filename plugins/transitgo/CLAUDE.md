# Transit GO for Smartspacer — release rules

Read the root [CLAUDE.md](../../CLAUDE.md) before versioning or preparing a release.

- Gradle module: `:transitgo`.
- Application ID: `dev.marco.smartspacer.transitgo`.
- `versionName` and `versionCode` live in `app/build.gradle.kts`.
- This plugin uses its own release tag prefix: `transitgo-v<version>`.
- Release notes: root `.github/release-notes-transitgo.md`.
- Release workflow: root `.github/workflows/release-transitgo.yml`.
- Releases use the permanent `smartspacer-plugins` signing identity from repository secrets.

Never bump a version, edit release notes for publication, push a tag, or publish a release without explicit approval. Show the exact version, tag, complete release notes, and APK asset name first.
