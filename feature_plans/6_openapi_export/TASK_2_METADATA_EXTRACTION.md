### Task 2: Implement Symbol Processor for Metadata Extraction

**Description**: Develop the KSP processor to analyze `@Controller` and `RouteConfig` definitions at compile-time to extract API metadata without reflection.

**TDD Requirement**:
- **Red Phase**: Write a test using `kotlin-compile-testing` that provides a sample `@Controller` and asserts that no metadata is currently generated.
- **Green Phase**: Implement the `SymbolProcessor` to visit `@Controller` classes and their `RouteConfig` properties.
- **Refactor Phase**: Optimize the symbol visiting logic for better performance.

**Instructions**:
1.  Implement `SymbolProcessor` to detect all classes annotated with `@Controller`.
2.  For each `@Controller`, find all methods that return `RouteConfig`.
3.  Extract:
    - Path and Method from the `RequestMapping` (e.g., `PostRequestMapping`).
    - DTO types for `RequestData` and `ResponseData`.
    - Parameter names and types from properties in the DTOs (checking their delegates like `getValue`).
4.  Generate a JSON intermediate representation of the API.

**Verification**:
- `kotlin-compile-testing` assertions verifying that all paths, methods, and DTO types are correctly identified.
