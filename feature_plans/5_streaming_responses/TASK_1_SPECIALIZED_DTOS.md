### Task 1: Define Specialized Response DTOs

**Description**: Create specialized `ResponseData` interfaces for common Ktor response types like Files, Bytes, and OutputStreams.

**TDD Requirement**:
- **Red Phase**: Write a failing test ensuring that there is currently no way to return a File or Byte array from a controller while keeping the `ResponseData` abstraction.
- **Green Phase**: Implement `ResponseDataWithFile`, `ResponseDataWithBytes`, and `ResponseDataWithOutputStream`.
- **Refactor Phase**: Ensure these interfaces extend the base `ResponseData` and share common properties like `httpStatusCode` and `responseHeaders`.

**Instructions**:
1.  Create `src/commonMain/kotlin/com/withlambda/ktor2/server/bind/response/SpecializedResponseData.kt`.
2.  Define:
    - `interface ResponseDataWithFile : ResponseData { val file: java.io.File }` (Note: `java.io.File` is JVM-specific; ensure this interface is usable in `jvmMain` or handle accordingly for KMP).
    - `interface ResponseDataWithBytes : ResponseData { val bytes: ByteArray }`
    - `interface ResponseDataWithOutputStream : ResponseData { val producer: suspend (java.io.OutputStream) -> Unit }`

**Verification**:
- Compilation of new interfaces.
- Ability to create DTOs implementing these interfaces.
