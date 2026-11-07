### Task 4: E2E Cookie Support Verification

**Description**: Perform an end-to-end integration test of cookie support.

**TDD Requirement**:
- **Red Phase**: Write an integration test using `testApplication` that expects to read and set cookies, ensuring it fails with the current implementation.
- **Green Phase**: Ensure the previous tasks' implementations make this test pass.
- **Refactor Phase**: Add edge cases (e.g., special characters in cookies, expired cookies).

**Instructions**:
1.  Create `src/commonTest/kotlin/com/withlambda/ktor2/server/bind/CookieIntegrationTest.kt`.
2.  Test scenario:
    - Send a request with a `SessionID` cookie.
    - Controller reads `SessionID` and returns a response with a `LastVisit` cookie.
    - Assert `LastVisit` has all expected options (Path, HttpOnly, etc.).

**Verification**:
- `CookieIntegrationTest` passes.
