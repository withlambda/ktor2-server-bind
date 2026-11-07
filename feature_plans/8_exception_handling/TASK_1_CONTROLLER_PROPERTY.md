### Task 1: Add Exception Handler Property to Controller

**Description**: Allow controllers to define their own exception mapping rules.

**TDD Requirement**:
- **Red Phase**: Write a test attempting to define an `exceptionHandler` in a `Controller` implementation and asserting it fails to compile.
- **Green Phase**: Update the `Controller` interface to include `val exceptionHandler: (ExceptionHandlerBuilder.() -> Unit)?`.
- **Refactor Phase**: Ensure the property is optional and defaults to `null`.

**Instructions**:
1.  Edit `src/commonMain/kotlin/com/withlambda/ktor2/server/bind/Controller.kt`:
    - Add `val exceptionHandler: (ExceptionHandlerBuilder.() -> Unit)?` to the `Controller` interface.
2.  Update existing controllers (in tests) to provide a default `null` value for this property.

**Verification**:
- Successful compilation and ability to override `exceptionHandler` in controllers.
