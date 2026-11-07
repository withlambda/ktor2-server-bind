### Task 3: Update Authorization Plugin

**Description**: Modify the `RoleBasedAuthorization` plugin to evaluate the custom predicate during request processing.

**TDD Requirement**:
- **Red Phase**: Write an integration test for an endpoint secured with a custom predicate (e.g., "only allow if header X is present") and verify it currently ignores the predicate.
- **Green Phase**: Update `RoleBasedAuthorization.kt` to call `configuration.predicate(roles, call)` and reject the request if it returns `false`.
- **Refactor Phase**: Ensure the plugin still works correctly with the legacy role-only approach.

**Instructions**:
1.  Edit `src/commonMain/kotlin/com/withlambda/ktor2/server/bind/plugins/RoleBasedAuthorization.kt`.
2.  In the `onCall` (or equivalent) interception, obtain the `PrincipalRoles`.
3.  Evaluate the predicate: `val authorized = configuration.predicate(roles, call)`.
4.  If `authorized` is false, respond with `HttpStatusCode.Forbidden` (or throw an exception handled by `StatusPages`).

**Verification**:
- Integration tests for:
    - Predicate that checks a custom header.
    - Predicate that combines roles and a custom condition.
    - Predicate that always denies (should always return 403).
