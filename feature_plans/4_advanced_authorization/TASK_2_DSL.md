### Task 2: Implement Predicate DSL and Logic

**Description**: Provide logical combinators (`and`, `or`, `not`) for authorization predicates.

**TDD Requirement**:
- **Red Phase**: Write a test attempting to combine two predicates using `or` and asserting it fails to compile.
- **Green Phase**: Implement extension functions for `(PrincipalRoles, ApplicationCall) -> Boolean` to allow logical combinations.
- **Refactor Phase**: Ensure the DSL is intuitive and readable.

**Instructions**:
1.  In `com.withlambda.ktor2.server.bind.config.auth`, add extension functions:
    - `infix fun AuthorizationPredicate.and(other: AuthorizationPredicate): AuthorizationPredicate`
    - `infix fun AuthorizationPredicate.or(other: AuthorizationPredicate): AuthorizationPredicate`
    - `fun AuthorizationPredicate.not(): AuthorizationPredicate`
2.  (Optional) Define a typealias `AuthorizationPredicate = (PrincipalRoles, ApplicationCall) -> Boolean`.

**Verification**:
- Unit tests verifying that combined predicates evaluate correctly for different inputs.
