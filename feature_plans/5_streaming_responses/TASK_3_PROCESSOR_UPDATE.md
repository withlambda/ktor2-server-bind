### Task 3: Update RouteProcessor for Streaming Responses

**Description**: Extend `RouteProcessor` to handle the new specialized response types by calling the appropriate Ktor functions.

**TDD Requirement**:
- **Red Phase**: Write integration tests for File, Bytes, and SSE endpoints, asserting they all currently fail or return incorrect content.
- **Green Phase**: Update `RouteProcessorBase.customizedResponse` to use `when` and handle each specialized interface.
- **Refactor Phase**: Ensure headers and status codes are correctly applied before calling `respondFile`, `respondBytes`, etc.

**Instructions**:
1.  Edit `src/commonMain/kotlin/com/withlambda/ktor2/server/bind/RouteProcessor.kt`:
2.  Update `customizedResponse`:
    - If `ResponseDataWithFile`, call `call.respondFile(data.file)`.
    - If `ResponseDataWithBytes`, call `call.respondBytes(data.bytes)`.
    - If `ResponseDataWithOutputStream`, call `call.respondOutputStream { data.producer(this) }`.
    - If `ResponseDataWithSSE`, set content type and iterate over the flow:
      ```kotlin
      call.response.cacheControl(CacheControl.NoCache(null))
      call.respondTextWriter(contentType = ContentType.Text.EventStream) {
          data.events.collect { event ->
              write("data: ${event.data}\n\n")
              flush()
          }
      }
      ```

**Verification**:
- Integration tests for all specialized types pass, verifying correct content and headers.
