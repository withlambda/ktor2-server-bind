# Publishing to Maven Central

This document explains how to publish the two `ktor2-server-bind` artifacts to Maven Central,
covering how to obtain credentials, how to set up GPG signing, and how to
trigger a release — both locally and via the provided GitHub Actions workflow.

## Published artifacts

Both modules must always be published together and share the same version:

| Artifact                                    | Gradle configuration in consumer project |
|---------------------------------------------|------------------------------------------|
| `io.withlambda:ktor2-server-bind`           | `implementation(...)`                    |
| `io.withlambda:ktor2-server-bind-processor` | `ksp(...)`                               |

A consuming project's `build.gradle.kts` declares both like this:

```kotlin
dependencies {
    implementation("io.withlambda:ktor2-server-bind:<version>")
    ksp("io.withlambda:ktor2-server-bind-processor:<version>")
}
```

The processor artifact is a pure compile-time KSP tool and must never appear on the
runtime classpath; using `ksp(...)` guarantees this.

---

## What is the Sonatype Central Portal?

The **Sonatype Central Portal** (<https://central.sonatype.com>) is the current gateway to Maven Central.
You cannot push artifacts directly to Maven Central; instead, you publish to the Central Portal and
Sonatype promotes them.

The portal exposes two endpoints:

| Endpoint  | URL                                                        | Reaches Maven Central?  |
|-----------|------------------------------------------------------------|-------------------------|
| Releases  | `https://central.sonatype.com/api/v1/publisher`            | Yes, after promotion    |
| Snapshots | `https://central.sonatype.com/repository/maven-snapshots/` | No (snapshot repo only) |

**Snapshots** are published directly to the snapshot repository via `maven-publish`.

**Releases** use a two-step process via [JReleaser](https://jreleaser.org):
1. `./gradlew publish -PisRelease=true` stages all artifacts into `build/staging-deploy/`
   (a local directory; Gradle calls it `"LocalStaging"` internally to generate task names).
2. `./gradlew jreleaserDeploy` bundles the staged artifacts into a ZIP and uploads them to the
   Central Portal publisher API, where JReleaser handles signing, checksums, and verification.

---

## One-time Central Portal setup

### 1. Create a Sonatype account

Register at the **Sonatype Central Portal**:

> <https://central.sonatype.com>

Click **Sign In** → **Sign Up** to create a new account.

### 2. Claim your `groupId`

1. Log in to <https://central.sonatype.com>.
2. Go to **Namespaces** (top menu) and click **Add Namespace**.
3. Enter `io.withlambda` and click **Submit**.
4. Sonatype will display a verification token — add it as a DNS TXT record on `withlambda.com`
   or create a GitHub repository named after the token under the `withlambda` organisation.
5. Click **Verify Namespace** once the record is in place.
6. Enable **Snapshots** for the namespace (required to publish to the snapshot repository).

Once approved, any artifact under `io.withlambda.*` can be published with these credentials.

> This is a one-time step. All future releases reuse the same approved namespace.

### 3. Generate a user token (recommended)

Rather than using your raw account password, generate a token pair:

1. Log in to <https://central.sonatype.com>.
2. Click your username (top-right) → **View Account**.
3. Scroll to **Generate User Token** and click it.
4. Save the displayed username and password.

Use these token values as `SONATYPE_USERNAME` and `SONATYPE_PASSWORD`.

---

## One-time GPG signing setup

Maven Central requires every release artifact to be signed with a GPG key.
JReleaser handles signing during `jreleaserDeploy` using the key material supplied via environment variables.

```bash
# 1. Generate a key pair (RSA 4096 recommended)
gpg --full-generate-key

# 2. List your keys to find the key ID
gpg --list-secret-keys --keyid-format SHORT
# Example output:
#   sec   rsa4096/ABCD1234 2024-01-01 [SC]
#                  ^^^^^^^^ <- this is your SIGNING_KEY_ID (last 8 hex chars)

# 3. Export the ASCII-armored private key — paste the full block as SIGNING_KEY
gpg --export-secret-keys --armor ABCD1234

# 4. Export the ASCII-armored public key — paste the full block as SIGNING_PUBLIC_KEY
gpg --export --armor ABCD1234

# 5. Upload the public key so Sonatype can verify signatures
gpg --keyserver keyserver.ubuntu.com --send-keys ABCD1234
gpg --keyserver keys.openpgp.org     --send-keys ABCD1234
```

---

## Credential properties reference

| Property             | Used by      | Purpose                                                  |
|----------------------|--------------|----------------------------------------------------------|
| `SONATYPE_USERNAME`  | Gradle       | Central Portal user token username (snapshot publishing) |
| `SONATYPE_PASSWORD`  | Gradle       | Central Portal user token password (snapshot publishing) |
| `SIGNING_KEY_ID`     | JReleaser    | Last 8 characters of your GPG key ID                     |
| `SIGNING_KEY`        | JReleaser    | Full ASCII-armored GPG private key block                 |
| `SIGNING_PASSWORD`   | JReleaser    | Passphrase for the GPG key                               |
| `SIGNING_PUBLIC_KEY` | JReleaser    | Full ASCII-armored GPG public key block                  |

For releases, JReleaser reads credentials via environment variables prefixed with `JRELEASER_`:

| Environment variable                       | Maps to              |
|--------------------------------------------|----------------------|
| `JRELEASER_MAVENCENTRAL_SONATYPE_USERNAME` | `SONATYPE_USERNAME`  |
| `JRELEASER_MAVENCENTRAL_SONATYPE_PASSWORD` | `SONATYPE_PASSWORD`  |
| `JRELEASER_GPG_PASSPHRASE`                 | `SIGNING_PASSWORD`   |
| `JRELEASER_GPG_SECRET_KEY`                 | `SIGNING_KEY`        |
| `JRELEASER_GPG_PUBLIC_KEY`                 | `SIGNING_PUBLIC_KEY` |

---

## Local publishing

### Credential options

#### Option A — `~/.gradle/gradle.properties` (recommended, never committed)

```properties
# ~/.gradle/gradle.properties
SONATYPE_USERNAME=<token-username>
SONATYPE_PASSWORD=<token-password>
```

#### Option B — `env/gradle-signing.private.env` (local file, git-ignored)

Create `env/gradle-signing.private.env` (already in `.gitignore`):

```properties
ORG_GRADLE_PROJECT_SONATYPE_USERNAME=<token-username>
ORG_GRADLE_PROJECT_SONATYPE_PASSWORD=<token-password>
```

#### Option C — environment variables

```bash
export SONATYPE_USERNAME="<token-username>"
export SONATYPE_PASSWORD="<token-password>"
```

### Publishing steps

> **The version is read from `gradle.properties` (`projectVersion`).** Set it there before publishing.
> Override with `-PprojectVersion=X.Y.Z` only when needed locally; the GitHub Actions workflow always
> uses the value already committed in the branch.

**Snapshot** (no signing needed):

```bash
./gradlew publish -PisRelease=false
```

**Release** — step 1: stage artifacts locally:

```bash
./gradlew publish -PisRelease=true
```

This writes all artifacts and POMs into `build/staging-deploy/`.

**Release** — step 2: bundle and upload via JReleaser:

```bash
export JRELEASER_MAVENCENTRAL_SONATYPE_USERNAME="<token-username>"
export JRELEASER_MAVENCENTRAL_SONATYPE_PASSWORD="<token-password>"
export JRELEASER_GPG_PASSPHRASE="<gpg-passphrase>"
export JRELEASER_GPG_SECRET_KEY="$(gpg --export-secret-keys --armor ABCD1234)"
export JRELEASER_GPG_PUBLIC_KEY="$(gpg --export --armor ABCD1234)"

./gradlew jreleaserDeploy
```

After `jreleaserDeploy` completes, log in to <https://central.sonatype.com>, find your deployment
under **Deployments**, and click **Publish** to promote both artifacts to Maven Central
(propagation takes ~10–30 minutes).

---

## GitHub Actions workflow

A reusable workflow is provided at `.github/workflows/publish.yml`. It can be triggered manually from the GitHub UI.

### Required GitHub Secrets

Go to your repository → **Settings → Secrets and variables → Actions** and add:

| Secret name           | Value                                     |
|-----------------------|-------------------------------------------|
| `SONATYPE_USERNAME`   | Central Portal user token username        |
| `SONATYPE_PASSWORD`   | Central Portal user token password        |
| `SIGNING_KEY_ID`      | Last 8 chars of your GPG key ID           |
| `SIGNING_KEY`         | Full ASCII-armored GPG private key block  |
| `SIGNING_PASSWORD`    | GPG key passphrase                        |
| `SIGNING_PUBLIC_KEY`  | Full ASCII-armored GPG public key block   |

### Triggering a release

1. Set `projectVersion` to the desired version in `gradle.properties` and push to the target branch.
   - Use a `releases/v*` branch for a release; any other branch for a snapshot.
2. Open the **Actions** tab in GitHub.
3. Select **Publish to Maven Central**.
4. Click **Run workflow** and select the branch.
5. Choose the **Publish type** (`release` on `releases/v*` branches, `snapshot` on all others).
6. Click **Run workflow**.

The workflow reads `projectVersion` directly from `gradle.properties` of the checked-out branch. `release` is only accepted on `releases/v*` branches; `snapshot` is only accepted on all other branches — the workflow fails fast otherwise.

For a **release** publish the workflow runs two steps automatically:
1. **Stage release artifacts** — `./gradlew publish -PisRelease=true` writes all artifacts to `build/staging-deploy/`.
2. **Deploy via JReleaser** — `./gradlew jreleaserDeploy` signs, bundles, and uploads the staged artifacts to the Central Portal.

You still need to manually click **Publish** on the deployment in the [Central Portal UI](https://central.sonatype.com) to promote it to Maven Central.

---

## Pre-release checklist

| Step                                                                                                                                                |   |
|-----------------------------------------------------------------------------------------------------------------------------------------------------|---|
| Sonatype Central Portal account created and `io.withlambda` namespace verified (with Snapshots enabled)                                             | ☐ |
| GPG key generated and uploaded to a keyserver                                                                                                       | ☐ |
| GitHub Secrets (`SONATYPE_*`, `SIGNING_*`, `SIGNING_PUBLIC_KEY`) configured                                                                         | ☐ |
| Version bumped in `gradle.properties` (`projectVersion=X.Y.Z`) and `isRelease=false` left as-is (overridden at publish time via `-PisRelease=true`) | ☐ |
| Changes committed and pushed to `main`                                                                                                              | ☐ |
| Workflow triggered and completed successfully                                                                                                       | ☐ |
| Deployment published via Central Portal UI                                                                                                          | ☐ |
| `ktor2-server-bind` visible on [Maven Central](https://central.sonatype.com/artifact/io.withlambda/ktor2-server-bind) (~10–30 min)                  | ☐ |
| `ktor2-server-bind-processor` visible on [Maven Central](https://central.sonatype.com/artifact/io.withlambda/ktor2-server-bind-processor)           | ☐ |
