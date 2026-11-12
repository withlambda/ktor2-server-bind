# Contributing to ktor2-server-bind

First off, thank you for considering contributing to `ktor2-server-bind`! It's people like you that make the open-source community such an amazing place to learn, inspire, and create.

## Environment Setup

- **JDK**: Version 8 or higher is required.
- **Gradle**: The project uses the Gradle wrapper. You don't need to install Gradle locally; just use `./gradlew`.

## Code Style and Quality

We maintain high standards for code quality and consistency using automated tools.

### ktlint

We use **ktlint** to enforce a consistent Kotlin coding style.

- **Check style**: Run `./gradlew ktlintCheck` to see if your changes comply with the style guide.
- **Fix errors**: If you encounter validation errors, you can automatically fix most of them by running:
  ```bash
  ./gradlew ktlintFormat
  ```
  This command will reformat your code to match the project's `.editorconfig` settings.

### detekt

We use **detekt** for static code analysis to catch common code smells and potential bugs.

- **Run analysis**: Run `./gradlew detekt` to check for violations.
- **Handling violations**: We aim for zero violations in the main codebase. If a violation is a false positive or intentional, you can suppress it locally using `@Suppress("RuleName")`.

### kover

We use **kover** for test coverage analysis.

- **Generate reports**: Run `./gradlew koverHtmlReport` to generate a local HTML report (found in `build/reports/kover/html/index.html`).
- **Check coverage**: Run `./gradlew koverVerify` to ensure coverage meets the minimum requirements (if configured).

## Testing

Before submitting a Pull Request, please ensure all tests pass.

### Unit Tests
Run standard unit tests:
```bash
./gradlew test
```

### Integration Tests
This library is tested against multiple Ktor 2 versions to ensure compatibility.

- **Run against default version**:
  ```bash
  ./gradlew integrationTest
  ```
- **Run against all supported versions**:
  ```bash
  ./gradlew integrationTestAllVersions
  ```

## Development Workflow

1.  **Fork** the repository and create your branch from `main`.
2.  **Make your changes**.
3.  **Ensure quality**: Run the full check suite locally before pushing:
    ```bash
    ./gradlew check
    ```
    This task automatically triggers `ktlintCheck`, `detekt`, and all tests (including integration tests for all versions).
4.  **Submit a Pull Request** with a clear description of your changes.
5.  **Review CI Results**: Once you open a PR, GitHub Actions will automatically run the analysis.
    - **Code Scanning**: Static analysis results from `detekt` will appear in the "Security" tab and as annotations in the PR files.
    - **Test Coverage**: A summary of test coverage from `kover` will be added to the GitHub Actions run summary and as a comment on your PR.

## Changelog

This project maintains a `CHANGELOG.md` following the [Keep a Changelog 1.1.0](https://keepachangelog.com/en/1.1.0/) format with [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

### Generating or updating the changelog

A helper script auto-generates `CHANGELOG.md` from git commit history:

```bash
bash scripts/generate-changelog.sh
```

**Behaviour:**

- **No existing `CHANGELOG.md`**: creates the file with all commits listed under a section titled `## [<projectVersion>] - <today>`.
- **Existing `CHANGELOG.md`**: scans for the most recent commit hash already recorded in the file and prepends a new section containing only the commits made since then.

**Version** is read automatically from the `projectVersion` key in `gradle.properties`. If the key is absent, the section is titled `[Unreleased]`.

**Commit entry format:**

```
YYYY-MM-DDTHH:MM:SSZ <commit message> (<short-hash>)
```

Timestamps are always in UTC. The newest version section and commits appear at the top of the file; the `## [Unreleased]` anchor required by Keep a Changelog is preserved at the very top.

### When to run it

Run the script before opening a Pull Request or cutting a release so that `CHANGELOG.md` reflects the latest commits.

## Release Preparation

To cut a new release, run the interactive helper script from the repository root:

```bash
bash scripts/prepare-release.sh
```

**What it does:**

1. Reads the current `projectVersion` from `gradle.properties`.
2. Asks you to choose the release type — **Major**, **Minor**, or **Patch** — and increments the version accordingly (e.g. `1.0.0` → `1.1.0` for a minor release).
3. Writes the new version back to `gradle.properties`.
4. Runs `scripts/generate-changelog.sh` to prepend a new `## [<new-version>] - <today>` section to `CHANGELOG.md`.
5. Stages both changed files and creates a commit with the message:
   ```
   chore: prepare release <new-version>
   ```

After the script completes, review the generated changelog section, then push the branch and open a Pull Request.

## License

By contributing, you agree that your contributions will be licensed under its Apache License, Version 2.0.
