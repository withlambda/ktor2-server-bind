### Task 2: Implement Scoped Exception Handling

**Description**: Update `RouteProcessor` to apply controller-level exception handlers before falling back to global `StatusPages`.

**TDD Requirement**:
- **Red Phase**: Write an integration test where a controller defines a specific mapping for `EntityNotFoundException` and a global handler defines a different one. Assert that the global one currently wins.
- **Green Phase**: Update `RouteProcessorBase.install` to wrap the route handler in a `try-catch` block that uses the controller's `exceptionHandler`.
- **Refactor Phase**: Ensure that if the controller's handler doesn't catch the exception, it is re-thrown so that `StatusPages` can handle it.

**Instructions**:
1.  Edit `src/commonMain/kotlin/com/withlambda/ktor2/server/bind/RouteProcessor.kt`:
2.  In the `install` method, when setting up the Ktor route:
    - Wrap the call to `processRequest` (and associated mapping logic) in a `try-catch`.
    - If an exception occurs, check if `controller.exceptionHandler` is present.
    - If present, create an `ExceptionHandlerBuilderImpl`, apply the controller's block, and check if it can handle the exception.
    - If handled, respond accordingly; otherwise, re-throw.

**Verification**:
- Integration test where different controllers handle the same exception type differently.
- Verification that global handlers still catch exceptions not handled by controllers.
