# Publishing to Maven Central

This document explains how to publish `ktor2-server-bind` to Maven Central, covering what OSSRH is, how to obtain credentials, how to set up GPG signing, and how to trigger a release — both locally and via the provided GitHub Actions workflow.

---

## What is OSSRH?

**OSSRH** stands for **OSS Repository Hosting**. It is a free service provided by [Sonatype](https://www.sonatype.com/) that acts as the gateway to Maven Central. You cannot push artifacts directly to Maven Central; instead you publish to OSSRH and Sonatype promotes them.

OSSRH exposes two endpoints:

| Endpoint  | URL                                                                 | Reaches Maven Central?  |
|-----------|---------------------------------------------------------------------|-------------------------|
| Releases  | `https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/` | Yes, after promotion    |
| Snapshots | `https://s01.oss.sonatype.org/content/repositories/snapshots/`      | No (snapshot repo only) |

`build.gradle.kts` selects the correct URL automatically: versions matching `X.Y.Z-N` go to releases, versions ending in `-SNAPSHOT` go to snapshots.

---

## One-time OSSRH setup

### 1. Create a Sonatype account

Register at the **legacy OSSRH portal** (which this project targets — `s01.oss.sonatype.org`):

> <https://issues.sonatype.org/secure/Signup!default.jspa>

### 2. Claim your `groupId`

Open a new issue in the Sonatype JIRA project **OSSRH** and request the namespace `com.withlambda`.
Sonatype will ask you to prove domain ownership — the standard approach is to add a DNS TXT record on `withlambda.com` containing the issue URL, or to create a GitHub repo called `OSSRH-<issue-number>` under the `withlambda` organisation.
Once approved, any artifact under `com.withlambda.*` can be published with these credentials.

> This is a one-time step. All future releases reuse the same approved namespace.

### 3. Generate a user token (recommended)

Rather than using your raw account password, generate a token pair:

1. Log in to <https://s01.oss.sonatype.org>
2. Click your username (top-right) → **Profile**
3. Switch the dropdown from *Summary* to **User Token**
4. Click **Access User Token** and save the displayed username and password

Use these token values as `OSSRH_USERNAME` and `OSSRH_PASSWORD`.

---

## One-time GPG signing setup

Maven Central requires every release artifact to be signed with a GPG key.

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

# 4. Upload the public key so Sonatype can verify signatures
gpg --keyserver keyserver.ubuntu.com --send-keys ABCD1234
gpg --keyserver keys.openpgp.org     --send-keys ABCD1234
```

---

## Credential properties reference

The build reads five properties (in priority order): Gradle project property (`-P` flag or `gradle.properties`) → `env/gradle-signing.private.env` → environment variable.

| Property                    | Purpose                                         |
|-----------------------------|-------------------------------------------------|
| `OSSRH_USERNAME`            | OSSRH user token username (or account username) |
| `OSSRH_PASSWORD`            | OSSRH user token password (or account password) |
| `SIGNING_KEY_ID`            | Last 8 characters of your GPG key ID            |
| `SIGNING_KEY`               | Full ASCII-armored GPG private key block        |
| `SIGNING_PASSWORD`          | Passphrase for the GPG key                      |
| `enableGradleSigningPlugin` | Set to `true` to activate signing (boolean)     |

---

## Local publishing

### Option A — `~/.gradle/gradle.properties` (recommended, never committed)

```properties
# ~/.gradle/gradle.properties
OSSRH_USERNAME=<token-username>
OSSRH_PASSWORD=<token-password>
SIGNING_KEY_ID=ABCD1234
SIGNING_KEY=-----BEGIN PGP PRIVATE KEY BLOCK-----\n...\n-----END PGP PRIVATE KEY BLOCK-----
SIGNING_PASSWORD=<gpg-passphrase>
```

### Option B — `env/gradle-signing.private.env` (local file, git-ignored)

Create `env/gradle-signing.private.env` (already in `.gitignore`):

```properties
ORG_GRADLE_PROJECT_OSSRH_USERNAME=<token-username>
ORG_GRADLE_PROJECT_OSSRH_PASSWORD=<token-password>
ORG_GRADLE_PROJECT_SIGNING_KEY_ID=ABCD1234
ORG_GRADLE_PROJECT_SIGNING_KEY=-----BEGIN PGP PRIVATE KEY BLOCK-----\n...\n-----END PGP PRIVATE KEY BLOCK-----
ORG_GRADLE_PROJECT_SIGNING_PASSWORD=<gpg-passphrase>
```

### Option C — environment variables

```bash
export OSSRH_USERNAME="<token-username>"
export OSSRH_PASSWORD="<token-password>"
export SIGNING_KEY_ID="ABCD1234"
export SIGNING_KEY="-----BEGIN PGP PRIVATE KEY BLOCK-----
...
-----END PGP PRIVATE KEY BLOCK-----"
export SIGNING_PASSWORD="<gpg-passphrase>"
```

### Publishing steps

**Snapshot** (no signing needed):

```bash
# Ensure isSnapshot = true in build.gradle.kts
./gradlew publish
```

**Release**:

```bash
# 1. Set isSnapshot = false and bump libraryRelease in build.gradle.kts
# 2. Enable signing
./gradlew publish -PenableGradleSigningPlugin=true
```

After the release task completes, log in to <https://s01.oss.sonatype.org>, find the staging repository under **Staging Repositories**, click **Close** and then **Release** to promote it to Maven Central (propagation takes ~10–30 minutes).

---

## GitHub Actions workflow

A reusable workflow is provided at `.github/workflows/publish.yml`. It can be triggered manually from the GitHub UI.

### Required GitHub Secrets

Go to your repository → **Settings → Secrets and variables → Actions** and add:

| Secret name        | Value                                    |
|--------------------|------------------------------------------|
| `OSSRH_USERNAME`   | OSSRH user token username                |
| `OSSRH_PASSWORD`   | OSSRH user token password                |
| `SIGNING_KEY_ID`   | Last 8 chars of your GPG key ID          |
| `SIGNING_KEY`      | Full ASCII-armored GPG private key block |
| `SIGNING_PASSWORD` | GPG key passphrase                       |

### Triggering a release

1. Open the **Actions** tab in GitHub.
2. Select **Publish to Maven Central**.
3. Click **Run workflow**.
4. Choose the branch and select `release` or `snapshot` from the **Publish type** dropdown.
5. Click **Run workflow**.

For a **release** publish the workflow will sign and stage the artifacts on OSSRH. You still need to manually **Close** and **Release** the staging repository in the Sonatype OSSRH UI to promote it to Maven Central.

---

## Pre-release checklist

| Step                                                                                                                         |   |
|------------------------------------------------------------------------------------------------------------------------------|---|
| Sonatype account created and `com.withlambda` namespace approved                                                             | ☐ |
| GPG key generated and uploaded to a keyserver                                                                                | ☐ |
| GitHub Secrets (`OSSRH_*`, `SIGNING_*`) configured                                                                           | ☐ |
| `isSnapshot = false` and version set to `X.Y.Z-N` in `build.gradle.kts`                                                      | ☐ |
| Changes committed and pushed to `main`                                                                                       | ☐ |
| Workflow triggered and completed successfully                                                                                | ☐ |
| Staging repository closed and released in OSSRH UI                                                                           | ☐ |
| Artifact visible on [Maven Central](https://central.sonatype.com/artifact/com.withlambda/ktor2-server-bind) (~10–30 min) | ☐ |
