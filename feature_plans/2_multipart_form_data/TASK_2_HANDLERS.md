### Task 2: Implement Multipart and Form Data Handlers

**Description**: Create handlers to extract form fields and files from the `ApplicationCall`.

**TDD Requirement**:
- **Red Phase**: Write unit tests with a mock `MultiPartData` ensuring that fields and files can be correctly mapped to DTO properties.
- **Green Phase**: Implement `MultipartHandler` that parses `ApplicationCall.receiveMultipart()`.
- **Refactor Phase**: Ensure the handler is efficient and does not load file contents into memory unless explicitly read.

**Instructions**:
1.  Create `src/commonMain/kotlin/com/withlambda/ktor2/server/bind/util/MultipartHandler.kt`.
2.  The handler should process `PartData`.
    - `PartData.FormItem` should be mapped to `String` fields.
    - `PartData.FileItem` should be wrapped in `FileItemDTO`.
3.  Implement a way to bind these to the `RequestFormData` DTO properties.
    - Since this is Zero-reflection, the generated code (via KSP) will use the `MultipartHandler` to populate the DTO. For the manual implementation in tests, you can use a constructor that takes the extracted values.

**Verification**:
- Unit tests demonstrating that a multipart body with one form field and one file is correctly parsed into a DTO.
