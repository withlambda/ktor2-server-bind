### Task 3: Integration and E2E Testing

**Description**: Wire multipart support into `RouteProcessor` and verify with an integration test.

**TDD Requirement**:
- **Red Phase**: Write an integration test where a controller receives a file upload, and verify it currently fails.
- **Green Phase**: Update `RouteProcessor` to detect `multipart/form-data` and use the `MultipartHandler`.
- **Refactor Phase**: Ensure proper error handling for malformed multipart requests.

**Instructions**:
1.  Edit `src/commonMain/kotlin/com/withlambda/ktor2/server/bind/RouteProcessor.kt`:
    - In `prepareRequestData`, add logic to handle `RequestFormData`.
    - If the request content type is multipart, use `ApplicationCall.receiveMultipart()`.
2.  Create `src/commonTest/kotlin/com/withlambda/ktor2/server/bind/MultipartIntegrationTest.kt`.
3.  Test scenario:
    - Upload a file and a form field.
    - Controller reads the field and returns the size of the uploaded file stream.
    - Verify that the stream was indeed readable and not fully loaded in memory (by checking that we can process large inputs if possible in a test).

**Verification**:
- `MultipartIntegrationTest` passes.
