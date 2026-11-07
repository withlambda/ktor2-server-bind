### Task 2: Implement SSE Support via Flow

**Description**: Add support for Server-Sent Events (SSE) using a `Flow`-based response type.

**TDD Requirement**:
- **Red Phase**: Write an integration test attempting to use a `Flow<ServerSentEvent>` in a controller and asserting it currently results in a serialization error or `500`.
- **Green Phase**: Implement `ResponseDataWithSSE` which holds a `Flow<ServerSentEvent>`.
- **Refactor Phase**: Ensure the SSE response sets the correct `Content-Type: text/event-stream`.

**Instructions**:
1.  Add `interface ResponseDataWithSSE : ResponseData { val events: Flow<ServerSentEvent> }` to `SpecializedResponseData.kt`.
2.  (Optional) Define a simple `ServerSentEvent` data class in `commonMain` if not using a library.
3.  Ensure `kotlinx-coroutines-core` is available in `commonMain`.

**Verification**:
- Unit test ensuring `ResponseDataWithSSE` correctly holds a `Flow`.
