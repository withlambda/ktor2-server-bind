### Task 2: Implement Attribute Handlers

**Description**: Create handlers to extract attributes from Ktor's `Attributes` container.

**TDD Requirement**:
- **Red Phase**: Write unit tests ensuring that attributes can be extracted from a mock `Attributes` instance.
- **Green Phase**: Implement `RequiredAttributesHandler` and `OptionalAttributesHandler`.
- **Refactor Phase**: Ensure the handler supports all attribute types, not just strings.

**Instructions**:
1.  Create `src/commonMain/kotlin/com/withlambda/ktor2/server/bind/util/AttributesHandler.kt`.
2.  The handler should wrap `io.ktor.util.Attributes`.
3.  Use the `getValue` delegate pattern.
    - Note: Ktor attributes are identified by `AttributeKey<T>`. The delegate should probably use the property name to look up the key or expect the user to provide the key.
    - Recommendation: Use a naming convention where property `val transactionId: String` maps to an `AttributeKey<String>("transactionId")`.

**Verification**:
- Unit tests verifying that attributes of different types (String, Boolean, Custom Objects) are correctly retrieved.
