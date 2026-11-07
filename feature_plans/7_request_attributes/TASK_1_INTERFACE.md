### Task 1: Define RequestAttributes Interface

**Description**: Create a marker interface for typed Ktor `Attributes` binding.

**TDD Requirement**:
- **Red Phase**: Write a failing test ensuring that `RequestData` currently lacks an `attributes` property.
- **Green Phase**: Define `RequestAttributes` interface and add it to `RequestData`.
- **Refactor Phase**: Ensure the interface follows existing naming conventions.

**Instructions**:
1.  Create `src/commonMain/kotlin/com/withlambda/ktor2/server/bind/request/RequestAttributes.kt`.
    - Define `interface RequestAttributes`.
2.  Update `src/commonMain/kotlin/com/withlambda/ktor2/server/bind/request/RequestData.kt`:
    - Add `val attributes: RequestAttributes`.

**Verification**:
- Compilation of new interface and property.
