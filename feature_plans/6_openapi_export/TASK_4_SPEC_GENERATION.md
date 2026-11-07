### Task 4: OpenAPI Spec Generation and Provider

**Description**: Convert the extracted metadata into a valid OpenAPI 3.x specification and provide a route to serve it.

**TDD Requirement**:
- **Red Phase**: Write an integration test attempting to fetch `/openapi.json` and asserting it returns 404.
- **Green Phase**: Implement the conversion from `ApiMetadata` to OpenAPI JSON/YAML and add the route.
- **Refactor Phase**: Support both JSON and YAML formats based on request headers or file extension.

**Instructions**:
1.  Use a library like `swagger-parser` or `openapi-models` (or just simple JSON generation) to create the final `openapi.json` file during the KSP process.
2.  Store the generated file in `resources`.
3.  Add a route in `RouteProcessor` or as a standalone plugin:
    ```kotlin
    get("/openapi.json") {
        // Use a Multiplatform-safe way to load resources if needed, 
        // but for Ktor Server on JVM, this is common:
        val content = javaClass.getResourceAsStream("/openapi.json")?.bufferedReader()?.readText()
        if (content != null) call.respondText(content, ContentType.Application.Json)
        else call.respond(HttpStatusCode.NotFound)
    }
    ```

**Verification**:
- Access `/openapi.json` in a running application and validate it against the Swagger Editor or another OpenAPI validator.
