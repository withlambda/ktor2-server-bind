# AGENTS.md — Agentic Coding Guidelines

Instructions for every AI agent working in this repository. All rules apply unconditionally unless a task explicitly overrides one.

---

## 1. Immutability by Default (IDD)

- Use `val` over `var` everywhere (locals, properties, parameters).
- Use immutable collections (`listOf`, `mapOf`, `setOf`); reach for `mutableListOf` / `MutableList` only when mutation is algorithmic.
- Model data as `data class` with `val` fields; use `copy(…)` instead of mutating fields.
- Return new values instead of producing side effects.

## 2. Test-Driven Development (TDD)

- Write a failing test **before** any implementation — Red → Green → Refactor, strictly.
- Every public function, class, and code path needs at least one test.
- Place tests under `src/commonTest/` mirroring `src/commonMain/`. Use fixtures in `src/commonTest/…/fixtures/` for Ktor test-application setup.

```bash
./gradlew test                      # unit tests
./gradlew integrationTest           # integration tests (default Ktor version)
./gradlew integrationTestAllVersions # integration tests (all supported Ktor versions)
./gradlew check                     # full quality gate: ktlintCheck + detekt + all tests
```

## 3. Security

- Never log or expose sensitive values (tokens, passwords, secrets, PII) — including in test output.
- Validate and sanitise all external input (path/query params, headers, cookies, bodies); prefer handler utilities in `src/commonMain/…/util/`.
- Fail fast with an explicit, typed exception; never swallow errors or return `null` when absence is unexpected.
- Avoid reflection and dynamic class loading; use compile-time-safe constructs.

## 4. Maintainability & Low Cognitive Load

- One function, one responsibility (SRP). Keep functions small.
- More than two parameters → use a data-class parameter.
- Eliminate deep nesting with early-return guards and extracted functions.
- Use expression bodies (`= …`) for single-expression functions.
- Add interfaces only when multiple implementations exist or testability requires one.
- Delete dead code; never leave commented-out code.

## 5. Self-Documenting Code

- Names must reveal intent: `extractBearerToken` not `parse`, `isUserAuthorized` not `check`.
- Encode domain constraints in types (`data class`, `@JvmInline value class`) instead of raw `String` / `Int`.
- Follow existing casing: `PascalCase` for types, `camelCase` for functions/properties, `SCREAMING_SNAKE_CASE` for top-level constants.

## 6. Code Formatting — `.editorconfig` Compliance

| Setting             | Value                                                             |
|---------------------|-------------------------------------------------------------------|
| Indent style        | spaces (4)                                                        |
| Max line length     | 120 characters                                                    |
| End of line         | LF                                                                |
| Final newline       | required                                                          |
| Trailing whitespace | trimmed                                                           |
| Wildcard imports    | **not allowed** (`ktlint_standard_no-wildcard-imports = enabled`) |
| Trailing commas     | **not allowed** (call site and declaration site)                  |

```bash
./gradlew ktlintFormat  # auto-fix formatting
./gradlew ktlintCheck   # verify — must report zero violations
```

## 7. Import Hygiene

- Remove all unused imports before committing.
- No wildcard imports (`import foo.bar.*`).
- Import order per `.editorconfig`: `*`, `java.**`, `javax.**`, `kotlin.**`, `^` (aliases).
- Run `./gradlew ktlintFormat` after any refactor to auto-sort and clean imports.

## 8. Documentation

- Every `public` or `internal` declaration **must** have a KDoc comment covering purpose, `@param`, `@return`, and `@throws` where applicable.
- Place KDoc directly above the declaration — no blank line between.
- Describe *why* and *what*, not *how*; don't restate the type signature.
- Module-level docs belong in `Module.md`.

## 9. Clarifying Comments

- Comment any line or block whose intent isn't immediately obvious.
- Explain *why*, not *what*: `// Ktor finalises the response on the first write, so headers must be set first` ✓; `// set header` ✗.
- Skip comments on self-explanatory code.

## 10. Static Analysis — detekt

- All code must pass `./gradlew detekt` with zero violations.
- Use `@Suppress("RuleName")` with a brief inline explanation only for confirmed false positives; fix real issues instead.

## 11. Test Coverage — kover

- Coverage must meet or exceed the project's configured thresholds.

```bash
./gradlew koverVerify       # enforce thresholds
./gradlew koverHtmlReport   # human-readable report
```

## 12. Error Handling

- Use typed, domain-specific exceptions from `src/commonMain/…/exception/`; not `RuntimeException`.
- Never catch `Exception` or `Throwable` broadly unless every case is explicitly handled or re-thrown.
- Propagate errors to the caller; never swallow them silently.

## 13. Completion Checklist

Before marking a task done:

- [ ] Failing test written first (Red), implementation passes it (Green), code cleaned up (Refactor).
- [ ] All `val` / immutable-collection opportunities taken.
- [ ] All public/internal declarations have KDoc; non-obvious lines have clarifying comments.
- [ ] No unused imports remain.
- [ ] `./gradlew ktlintFormat` run; `./gradlew ktlintCheck` reports zero violations.
- [ ] `./gradlew detekt` reports zero violations.
- [ ] `./gradlew koverVerify` passes.
- [ ] `./gradlew check` passes in its entirety.
