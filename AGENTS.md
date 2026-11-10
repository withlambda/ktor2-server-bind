# AGENTS.md — Agentic Coding Guidelines

All rules apply unconditionally unless a task explicitly overrides one.

---

## 0. Concise Output

Keep all agent outputs (answers, plans, instructions, comments) as short as possible without losing information needed for further work. Be precise; omit filler.

## 1. Immutability by Default

- `val` over `var`; immutable collections (`listOf`, `mapOf`, `setOf`) unless mutation is algorithmic.
- `data class` with `val` fields; use `copy(…)` over mutation; return new values over side effects.

## 2. TDD

Red → Green → Refactor, strictly. Every public function/class/code path needs ≥1 test.
Tests under `src/commonTest/` mirroring `src/commonMain/`; fixtures in `src/commonTest/…/fixtures/`.

```bash
./gradlew test                       # unit tests
./gradlew integrationTest            # integration tests (default Ktor)
./gradlew integrationTestAllVersions # integration tests (all Ktor versions)
./gradlew check                      # ktlintCheck + detekt + all tests
```

## 3. Security

- Never log/expose tokens, passwords, secrets, PII — including in test output.
- Validate/sanitise all external input; prefer utilities in `src/commonMain/…/util/`.
- Fail fast with a typed exception; never swallow errors or return unexpected `null`.
- No reflection or dynamic class loading; use compile-time-safe constructs.

## 4. Maintainability

- SRP: one function, one responsibility; keep functions small.
- >2 parameters → data-class parameter; expression bodies for single-expression functions.
- Early-return guards over deep nesting; interfaces only when multiple impls or testability requires.
- Delete dead code; no commented-out code.

## 5. Self-Documenting Code

- Names reveal intent: `extractBearerToken` not `parse`.
- Encode constraints in types (`data class`, `@JvmInline value class`) over raw `String`/`Int`.
- Casing: `PascalCase` types · `camelCase` functions/properties · `SCREAMING_SNAKE_CASE` constants.

## 6. Formatting & Imports

`.editorconfig` rules: 4-space indent · 120-char lines · LF · final newline · no trailing whitespace · no wildcard imports · no trailing commas.
Import order: `*`, `java.**`, `javax.**`, `kotlin.**`, `^` (aliases). Remove unused imports.

```bash
./gradlew ktlintFormat  # auto-fix
./gradlew ktlintCheck   # must report zero violations
```

## 7. Documentation & Comments

- Every `public`/`internal` declaration needs KDoc: purpose, `@param`, `@return`, `@throws`.
- KDoc directly above declaration; describe *why*/*what*, not *how*; don't restate the signature.
- Module-level docs in `Module.md`.
- Comment non-obvious intent (*why*, not *what*); skip self-explanatory code.

## 8. Static Analysis & Coverage

```bash
./gradlew detekt        # zero violations; @Suppress only for confirmed false positives
./gradlew koverVerify   # coverage must meet configured thresholds
```

## 9. Error Handling

- Typed exceptions from `src/commonMain/…/exception/`; never `RuntimeException`.
- Never catch `Exception`/`Throwable` broadly unless all cases are handled or re-thrown.
- Propagate; never swallow silently.

## 10. Completion Checklist

- [ ] Red→Green→Refactor done.
- [ ] `val`/immutable collections used everywhere possible.
- [ ] KDoc on all public/internal declarations; non-obvious lines commented.
- [ ] No unused imports.
- [ ] `./gradlew ktlintFormat` run; `ktlintCheck` zero violations.
- [ ] `./gradlew detekt` zero violations.
- [ ] `./gradlew koverVerify` passes.
- [ ] `./gradlew check` passes.
