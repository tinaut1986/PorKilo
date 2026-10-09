# Versions, branches and releases

Same scheme as `tinaut1986/sm-3ds`.

## Branches

- `main` = stable. It only receives `--no-ff` merges of a finished release branch.
- `release/vX.Y.Z` = the current release line. Work accumulates here; being on it
  means "not stable yet".
- Topic branches (`feat/...`, `fix/...`, `chore/...`) are cut from the active release
  branch and merged back into it with `--no-ff`.
- Version numbers: minor = feature milestone, patch = fix round.

## Version name and code

Nothing is bumped by hand: `app/build.gradle.kts` derives both from git.

| Checkout | `versionName` |
|---|---|
| Commit tagged `vX.Y.Z` | `X.Y.Z` |
| On `release/vX.Y.Z` | `X.Y.Z-dev.<commits since main>+<hash>` |
| On a branch cut from `release/vX.Y.Z` | `X.Y.Z-dev.<since main>.<since release>+<hash>` |
| Anything else | `git describe --tags` |

`versionCode` is the number of commits (`git rev-list --count HEAD`), so a later build
always installs over an earlier one without uninstalling (as long as both are signed
with the same key). Builds need the full history: CI checks out with `fetch-depth: 0`.

## Installed apps

| Build | App | Signed with |
|---|---|---|
| Release and Beta (GitHub Releases) | PorKilo, `com.tinaut1986.porkilo` | release key (CI secrets) |
| Local debug builds | PorKilo Dev, `com.tinaut1986.porkilo.debug`, red icon | local debug key |

Beta and Release are the same app, so a beta updates the stable version and keeps its
data. Debug builds are a separate app and never touch it.

## Releases

`.github/workflows/android-release.yml` builds and signs the APK on any `v*` tag push
and on manual dispatch, and publishes a GitHub Release with the APK and a QR code that
downloads it on the phone. Pull requests to `main` or `release/**` only build and run
the unit tests. The channel depends on whether `main` can reach the built commit:

| Built from | Channel |
|---|---|
| Tag on an unmerged `release/*` branch | `Beta` (pre-release) |
| Tag on `main`, or on a commit merged into `main` | `Release` |
| Manual dispatch on any branch | `Beta` |

**Beta**: tag the release branch, push the branch, then the tag.

```sh
git tag -a v1.3.0 -m "v1.3.0"
git push origin release/v1.3.0
git push origin v1.3.0
```

**Stable**: merge into `main`, tag the merge, push **main before the tag** (the build
checks reachability from `origin/main`).

```sh
git checkout main
git merge --no-ff release/v1.3.0
git tag -a v1.3.0 -m "v1.3.0"
git push origin main
git push origin v1.3.0
```

A tag already shipped as a beta is promoted by re-running the workflow from the Actions
tab **on the tag itself**.

After tagging, rename the release branch to the next patch and delete the old remote
branch:

```sh
git branch -m release/v1.3.0 release/v1.3.1
git branch --unset-upstream
git push -u origin release/v1.3.1
git push origin --delete release/v1.3.0
```

## Database changes

Room schemas are exported to `app/schemas/` and committed. Any change to an entity
(`ProductTemplate`) must bump the database version in `AppDatabase` and add a migration,
preferably an `AutoMigration` (with an `AutoMigrationSpec` for deleted or renamed
columns); otherwise the app fails to open on devices that already have data.
