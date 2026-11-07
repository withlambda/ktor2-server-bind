### Task 2: Implement Cookie Handlers

**Description**: Create delegate-based handlers to extract cookies from the request, similar to `ParametersHandler`.

**TDD Requirement**:
- **Red Phase**: Write unit tests ensuring that cookies can be extracted and converted to types like `Int` or `Boolean` using a mock `StringValues`.
- **Green Phase**: Implement `RequiredCookiesHandler` and `OptionalCookiesHandler`.
- **Refactor Phase**: Ensure consistency with `RequiredHeadersHandler` and `RequiredParametersHandler`.

**Instructions**:
1.  Create `src/commonMain/kotlin/com/withlambda/ktor2/server/bind/util/CookiesHandler.kt`.
2.  Define `AbstractCookiesHandler` extending `AbstractStringValuesHandler`.
    - It should wrap `io.ktor.util.StringValues`.
3.  Implement `RequiredCookiesHandler` and `OptionalCookiesHandler`.
    - Both should implement `RequestCookies`.
    - Use `getValue` delegate pattern:
      ```kotlin
      inline operator fun <reified R> getValue(thisRef: Any?, property: KProperty<*>): R
      ```
4.  Define `MissingCookieException` and `CookieConversionException` in `com.withlambda.ktor2.server.bind.exception`.

**Verification**:
- Unit tests for `RequiredCookiesHandler` and `OptionalCookiesHandler` demonstrating successful value extraction and type conversion.
