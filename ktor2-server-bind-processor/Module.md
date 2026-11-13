# Module ktor2-server-bind-processor

KSP (Kotlin Symbol Processing) compile-time companion to `ktor2-server-bind`.

At build time this processor scans every class annotated with `@ApiController`, inspects
its `@ApiEndpoint`-annotated `RouteConfig` properties, and writes a fully-formed
`openapi.json` (OpenAPI 3.0.3) resource file into the compilation output — with no
hand-written boilerplate required from the consumer.

# Package io.withlambda.ktor2.server.bind.processor

Entry point and core processing logic.

- `ApiBindProcessorProvider` — registered via `META-INF/services`; hands the KSP runtime
  an `ApiBindProcessor` instance.
- `ApiBindProcessor` — visits all `@ApiController` symbols, collects endpoint metadata, and
  delegates serialisation to `OpenApiSerializer`.
- `SchemaCollector` — recursively walks DTO property types to build JSON Schema definitions,
  avoiding infinite recursion for self-referencing types.
- `SchemaMapper` — maps Kotlin primitive and standard-library types to their OpenAPI
  `type`/`format` equivalents.
- `OpenApiSerializer` — converts the collected `ApiMetadata` into an OpenAPI 3.0.3 JSON
  string without any external JSON library dependency.

# Package io.withlambda.ktor2.server.bind.processor.model

Data classes representing the intermediate metadata model produced by `ApiBindProcessor`
and consumed by `OpenApiSerializer`.
