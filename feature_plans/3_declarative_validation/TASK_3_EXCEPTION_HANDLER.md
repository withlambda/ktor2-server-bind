### Task 3: Map Validation Exceptions to Responses

**Description**: Automatically map `RequestValidationException` to a `400 Bad Request` response with detailed error messages.

**TDD Requirement**:
- **Red Phase**: Write an integration test where an invalid DTO is sent, and verify it returns a `500` or a generic error instead of a structured `400`.
- **Green Phase**: Update the global `StatusPages` configuration (or provide a default handler) to catch `RequestValidationException`.
- **Refactor Phase**: Format the error messages from `Konform` into a user-friendly JSON structure.

**Instructions**:
1.  Implement a standard `ExceptionHandler` for `RequestValidationException`.
2.  Extract the validation errors (field name and error message) and return them in the response body.
3.  Ensure the status code is set to `HttpStatusCode.BadRequest`.

**Verification**:
- Integration test asserting that an invalid request receives a `400` status and a JSON body containing a list of validation errors.
