### Task 1: Extend Authorization Configuration

**Description**: Update `RoleBasedAuthorizationConfiguration` to support a custom predicate that has access to both `PrincipalRoles` and `ApplicationCall`.

**TDD Requirement**:
- **Red Phase**: Write a test attempting to configure a custom predicate in the authorization DSL and asserting it fails to compile.
- **Green Phase**: Update `RoleBasedAuthorizationConfiguration` interface and implementation to include `predicate: (PrincipalRoles, ApplicationCall) -> Boolean`.
- **Refactor Phase**: Ensure the predicate has a sensible default (e.g., always returns true or checks roles if provided).

**Instructions**:
1.  Edit `src/commonMain/kotlin/com/withlambda/ktor2/server/bind/config/auth/RoleBasedAuthorizationConfiguration.kt`:
    - Add `var predicate: (PrincipalRoles, ApplicationCall) -> Boolean`.
2.  Update `RoleBasedAuthorizationConfigurationImpl` to store and expose this predicate.

**Verification**:
- Successful compilation and ability to set the predicate in the DSL.
