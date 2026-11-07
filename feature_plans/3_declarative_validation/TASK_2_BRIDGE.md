### Task 2: Bridge to Ktor RequestValidation Plugin

**Description**: Integrate the defined validation rules with Ktor's `RequestValidation` plugin.

**TDD Requirement**:
- **Red Phase**: Write an integration test where an invalid DTO is sent, and verify it is currently NOT rejected with 400.
- **Green Phase**: Implement the bridge that registers `RouteConfig.validation` with Ktor's `RequestValidation`.
- **Refactor Phase**: Ensure the bridge handles all DTO types correctly.

**Instructions**:
1.  In `RouteProcessor.install`, check if a `validation` is defined for the route.
2.  If yes, register a validation rule in the `RequestValidation` plugin for the specific DTO type.
3.  Ensure the Ktor `RequestValidation` plugin is installed in the application (or provide instructions to do so in the README).

**Verification**:
- Integration test where sending a DTO that fails `Konform` validation results in a `RequestValidationException`.
