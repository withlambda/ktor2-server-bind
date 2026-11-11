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
| Releases  | `https://central.sonatype.com/api/v1/publisher/upload`     | Yes, after promotion    |
| Snapshots | `https://central.sonatype.com/repository/maven-snapshots/` | No (snapshot repo only) |

`build.gradle.kts` selects the correct URL automatically: release versions go to the publisher
API, versions ending in `-SNAPSHOT` go to the snapshot repository.

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

| Property                    | Purpose                                                  |
|-----------------------------|----------------------------------------------------------|
| `SONATYPE_USERNAME`         | Central Portal user token username (or account username) |
| `SONATYPE_PASSWORD`         | Central Portal user token password (or account password) |
| `SIGNING_KEY_ID`            | Last 8 characters of your GPG key ID                     |
| `SIGNING_KEY`               | Full ASCII-armored GPG private key block                 |
| `SIGNING_PASSWORD`          | Passphrase for the GPG key                               |
| `enableGradleSigningPlugin` | Set to `true` to activate signing (boolean)              |

---

## Local publishing

### Option A — `~/.gradle/gradle.properties` (recommended, never committed)

```properties
# ~/.gradle/gradle.properties
SONATYPE_USERNAME=<token-username>
SONATYPE_PASSWORD=<token-password>
SIGNING_KEY_ID=ABCD1234
SIGNING_KEY=-----BEGIN PGP PRIVATE KEY BLOCK-----\n...\n-----END PGP PRIVATE KEY BLOCK-----
SIGNING_PASSWORD=<gpg-passphrase>
```

### Option B — `env/gradle-signing.private.env` (local file, git-ignored)

Create `env/gradle-signing.private.env` (already in `.gitignore`):

```properties
ORG_GRADLE_PROJECT_SONATYPE_USERNAME=<token-username>
ORG_GRADLE_PROJECT_SONATYPE_PASSWORD=<token-password>
ORG_GRADLE_PROJECT_SIGNING_KEY_ID=ABCD1234
ORG_GRADLE_PROJECT_SIGNING_KEY=-----BEGIN PGP PRIVATE KEY BLOCK-----\n...\n-----END PGP PRIVATE KEY BLOCK-----
ORG_GRADLE_PROJECT_SIGNING_PASSWORD=<gpg-passphrase>
```

### Option C — environment variables

```bash
export SONATYPE_USERNAME="<token-username>"
export SONATYPE_PASSWORD="<token-password>"
export SIGNING_KEY_ID="ABCD1234"
export SIGNING_KEY="-----BEGIN PGP PRIVATE KEY BLOCK-----
...
-----END PGP PRIVATE KEY BLOCK-----"
export SIGNING_PASSWORD="<gpg-passphrase>"
```

### Publishing steps

> **The version and release flag are set via Gradle properties — no file edits required.**
> `gradle.properties` holds the defaults (`projectVersion`, `isRelease`). Override them with
> `-P` flags when running Gradle locally or let the GitHub Actions workflow supply them.

**Snapshot** (no signing needed):

```bash
./gradlew publish -PprojectVersion=1.0.0 -PisRelease=false
```

**Release**:

```bash
./gradlew publish \
  -PprojectVersion=1.0.0 \
  -PisRelease=true \
  -PenableGradleSigningPlugin=true
```

After the release task completes, log in to <https://central.sonatype.com>, find your deployment
under **Deployments**, click **Publish** to promote both artifacts to Maven Central
(propagation takes ~10–30 minutes).

---

## GitHub Actions workflow

A reusable workflow is provided at `.github/workflows/publish.yml`. It can be triggered manually from the GitHub UI.

### Required GitHub Secrets

Go to your repository → **Settings → Secrets and variables → Actions** and add:

| Secret name          | Value                                    |
|----------------------|------------------------------------------|
| `SONATYPE_USERNAME`  | Central Portal user token username       |
| `SONATYPE_PASSWORD`  | Central Portal user token password       |
| `SIGNING_KEY_ID`     | Last 8 chars of your GPG key ID          |
| `SIGNING_KEY`        | Full ASCII-armored GPG private key block |
| `SIGNING_PASSWORD`   | GPG key passphrase                       |

### Triggering a release

1. Open the **Actions** tab in GitHub.
2. Select **Publish to Maven Central**.
3. Click **Run workflow**.
4. Choose the branch, enter the **Version** (e.g. `1.0.0`), and select `release` or `snapshot` from the **Publish type** dropdown.
5. Click **Run workflow**.

The workflow passes `-PprojectVersion` and `-PisRelease` directly to Gradle — no changes to source files are needed before triggering.

For a **release** publish the workflow will sign and upload the artifacts to the Central Portal. You still need to manually click **Publish** on the deployment in the [Central Portal UI](https://central.sonatype.com) to promote it to Maven Central.

---

## Pre-release checklist

| Step                                                                                                                                                |   |
|-----------------------------------------------------------------------------------------------------------------------------------------------------|---|
| Sonatype Central Portal account created and `io.withlambda` namespace verified                                                                      | ☐ |
| GPG key generated and uploaded to a keyserver                                                                                                       | ☐ |
| GitHub Secrets (`SONATYPE_*`, `SIGNING_*`) configured                                                                                               | ☐ |
| Version bumped in `gradle.properties` (`projectVersion=X.Y.Z`) and `isRelease=false` left as-is (overridden at publish time via `-PisRelease=true`) | ☐ |
| Changes committed and pushed to `main`                                                                                                              | ☐ |
| Workflow triggered and completed successfully                                                                                                       | ☐ |
| Deployment published via Central Portal UI                                                                                                          | ☐ |
| `ktor2-server-bind` visible on [Maven Central](https://central.sonatype.com/artifact/io.withlambda/ktor2-server-bind) (~10–30 min)                  | ☐ |
| `ktor2-server-bind-processor` visible on [Maven Central](https://central.sonatype.com/artifact/io.withlambda/ktor2-server-bind-processor)           | ☐ |
