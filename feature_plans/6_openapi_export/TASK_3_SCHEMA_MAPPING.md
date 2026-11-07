### Task 3: Schema Extraction and Mapping

**Description**: Extract detailed schema information for DTOs using KSP to avoid runtime reflection.

**TDD Requirement**:
- **Red Phase**: Write a test for a DTO with nested properties and assert that the schema is currently incomplete.
- **Green Phase**: Update the KSP processor to recursively visit DTO properties and generate OpenAPI schemas.
- **Refactor Phase**: Handle common Kotlin types (List, Map, Optionals) and map them to their OpenAPI equivalents.

**Instructions**:
1.  In the `SymbolProcessor`, when a DTO type is encountered, visit its `KSClassDeclaration`.
2.  Map property types to OpenAPI types (e.g., `kotlin.String` -> `string`, `kotlin.Int` -> `integer`).
3.  Handle `@Serializable` annotations (if used) to respect custom property names.
4.  Generate a `components/schemas` section for the OpenAPI spec.

**Verification**:
- Verify that the generated JSON representation contains accurate schemas for complex DTOs.
