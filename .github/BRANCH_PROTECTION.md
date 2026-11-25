# Protecting `main`

The repository's `main` branch should accept changes through pull requests and successful CI, not direct pushes.

## Recommended GitHub settings

Open **Settings → Rules → Rulesets** (or **Branches → Branch protection rules**) and create a rule targeting the `main` branch.

Enable:

- Require a pull request before merging.
- Require status checks to pass before merging; select the required check produced by **Build plugin suite** (the job is named `build`). Select the check from GitHub's list after a recent workflow run rather than typing an assumed context.
- Require branches to be up to date before merging.
- Block force pushes.
- Block branch deletion.
- Apply the rule to administrators as well.

For this single-maintainer repository, do not require an approving review from another person unless a second reviewer is available. Requiring the CI check and a pull request still prevents accidental direct pushes without creating a permanent self-review deadlock.

## Current state

This file documents the intended policy; it does not itself configure GitHub repository settings. An administrator must enable the rule in GitHub.

Last checked: 2026-11-25. The branch was reported as unprotected at the time of the repository audit.

## Legacy branch review

The 2026-11-25 comparison against `main` found:

| Branch | Difference from `main` | Recommendation |
|---|---|---|
| `add-transit-go` | 6 commits ahead and 34 behind | Preserve. It contains unique commits that have not been integrated into main; inspect them separately before considering any cherry-pick. |
| `migration/add-ubereats-transitgo-monorepo` | 0 ahead and 6 behind | Candidate for deletion after confirming the merged PR #2 is the only intended use. |
| `transit-go-updates` | 0 ahead and 32 behind | Candidate for deletion after confirming no external work depends on the ref. |

Do not delete a branch that is ahead of main. A ref being fully behind main is evidence that its commits are reachable from main, but confirm that it is no longer needed before deleting it.
