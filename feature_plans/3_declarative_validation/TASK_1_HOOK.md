### Task 1: Add Validation Hook to RouteConfig

**Description**: Add a way to define validation rules in `RouteConfig` using a Multiplatform-friendly validation library (e.g., Konform).

**TDD Requirement**:
- **Red Phase**: Write a test attempting to define validation in a `RouteConfig` and asserting it fails to compile or has no effect.
- **Green Phase**: Add `validation` property to `RouteConfig` and add `Konform` dependency.
- **Refactor Phase**: Ensure the validation DSL is clean and fits well with the existing `RouteConfig` structure.

**Instructions**:
1.  Add `libs.versions.toml` entry for `Konform` and add it to `commonMain` dependencies in `build.gradle.kts`.
2.  Edit `src/commonMain/kotlin/com/withlambda/ktor2/server/bind/RouteConfig.kt`:
    - Add `var validation: (Validation<T>)? = null` (where T is the DTO type) to the route configuration.
    - Provide a DSL method `validate(block: ValidationBuilder<T>.() -> Unit)`.

**Verification**:
- Ability to define `validate { ... }` in `RouteConfig` without compilation errors.
