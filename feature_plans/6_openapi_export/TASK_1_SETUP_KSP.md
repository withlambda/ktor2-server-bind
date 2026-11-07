### Task 1: Setup KSP Module and Basic Infrastructure

**Description**: Create a dedicated Gradle module for the KSP symbol processor and define the shared metadata model.

**TDD Requirement**:
- **Red Phase**: Before implementing the task, write a failing test demonstrating that the project currently does not have a KSP processor registered.
- **Green Phase**: Create the module `ktor2-server-bind-processor`, add KSP dependencies, and register a basic `SymbolProcessor`.
- **Refactor Phase**: Ensure the new module follows the same structure and coding style as the main project.

**Instructions**:
1.  Add `ktor2-server-bind-processor` to `settings.gradle.kts`.
2.  In `ktor2-server-bind-processor/build.gradle.kts`, add dependencies on `com.google.symbolprocessing:api`.
3.  Create `src/main/resources/META-INF/services/com.google.devtools.ksp.processing.SymbolProcessorProvider` and point it to your implementation.
4.  Define a shared `ApiMetadata` model in a new `common` module or within the processor module if it's only used at compile-time.

**Verification**:
- Run `./gradlew :ktor2-server-bind-processor:build` and ensure it passes.
- A simple log message from the processor appears when compiling the main project.
