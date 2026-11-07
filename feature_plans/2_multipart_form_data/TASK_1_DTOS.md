### Task 1: Define File and Form Data DTOs

**Description**: Create core DTOs and interfaces for multipart and form data, ensuring streaming support for files.

**TDD Requirement**:
- **Red Phase**: Write a failing test ensuring that there is currently no way to handle `FileItem` as a stream in a controller DTO.
- **Green Phase**: Implement `FileItemDTO` and `RequestFormData` interface.
- **Refactor Phase**: Ensure `FileItemDTO` correctly exposes metadata like filename and content type.

**Instructions**:
1.  Create `src/commonMain/kotlin/com/withlambda/ktor2/server/bind/request/FileItemDTO.kt`:
    - Define `class FileItemDTO(val fileName: String?, val contentType: ContentType?, private val provider: () -> Input)`.
    - Provide a method `fun openStream(): Input = provider()`.
    - Note: `Input` from `io.ktor.utils.io.core` is Multiplatform and allows streaming.
2.  Create `src/commonMain/kotlin/com/withlambda/ktor2/server/bind/request/RequestFormData.kt`:
    - Define `interface RequestFormData : RequestData`.
    - This interface will be implemented by DTOs containing fields of type `String` (form fields) or `FileItemDTO`.

**Verification**:
- Compilation of new DTOs.
- `FileItemDTO` can be instantiated and its stream opened.
