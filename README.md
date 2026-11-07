# ktor2-server-bind

[![Maven Central](https://img.shields.io/maven-central/v/com.withlambda/ktor2-server-bind)](https://central.sonatype.com/artifact/com.withlambda/ktor2-server-bind)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

A Kotlin multiplatform library that brings **interface-driven, data-bound controller patterns** to [Ktor 2](https://ktor.io) servers.

It gives you a clean separation between your **API specification** (what an endpoint accepts and produces) and your **business logic** (what it actually does) — similar to how Spring MVC's `@RestController` and `@RequestMapping` annotations work, but using idiomatic Kotlin interfaces and type safety instead of annotations.

---

## Why use it?

Ktor's routing DSL is flexible but low-level: request parsing, type conversion, header extraction, content-type negotiation, role checks, and response building are all left to you. As projects grow, routing handlers tend to accumulate boilerplate and become hard to test in isolation.

`ktor2-server-bind` solves this by providing:

- **Typed request data** — path parameters, query parameters, headers, and body are each extracted into a strongly typed data class.
- **Interface-based API specification** — define what each endpoint looks like as a Kotlin interface; implement the business logic separately.
- **Automatic content-type negotiation** — `Accept` and `Content-Type` headers are validated automatically against the endpoint's `produces`/`consumes` declarations.
- **Role-based authorization** — per-endpoint role enforcement built in via a pluggable authorization configuration.
- **Exception handling helpers** — predefined handlers for missing/invalid parameters and headers that respond with `400 Bad Request`.
- **Zero reflection** — everything is resolved at compile time.

---

## Installation

Add the dependency to your Gradle build:

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.withlambda:ktor2-server-bind:1.0.0")
}
```

Or with a version catalog (`libs.versions.toml`):

```toml
[versions]
ktor2-server-bind = "1.0.0"

[libraries]
ktor2-server-bind = { module = "com.withlambda:ktor2-server-bind", version.ref = "ktor2-server-bind" }
```

```kotlin
// build.gradle.kts
dependencies {
    implementation(libs.ktor2.server.bind)
}
```

> **Compatibility:** Requires Ktor `2.0.0+` and JVM 8+.

---

## Core concepts

| Concept           | Role                                                                                             |
|-------------------|--------------------------------------------------------------------------------------------------|
| `Controller`      | Groups a set of endpoints together and installs them into Ktor routing                           |
| `RouteConfig`     | Defines *what* an endpoint looks like — path, method, content types, parameter extraction        |
| `RouteProcessor`  | Defines *how* an endpoint works — receives typed request data, returns typed response data       |
| `RequestData`     | Typed container for path params, query params, headers, and optional body                        |
| `ResponseData`    | Typed container for HTTP status code, optional body, optional message, optional response headers |
| `ResponseHeaders` | Typed container for response headers sent back to the client                                     |

---

## Quick start

The following shows a full example of a `POST /greetings/{name}` endpoint.

### 1. Define your DTOs

```kotlin
@Serializable
data class GreetingRequest(val message: String)

@Serializable
data class GreetingResponse(val greeting: String)
```

### 2. Define the endpoint specification (RouteConfig)

A `RouteConfig` object declares everything about an endpoint: its path, method, content types, and how to extract typed parameters from the incoming `ApplicationCall`.

```kotlin
object CreateGreeting : PostRouteConfig {

    // -- Typed path parameters
    class PathParametersImpl(val name: String) : PathParameters

    // -- Typed optional query parameters
    class OptionalQueryParametersImpl(val locale: String?) : OptionalQueryParameters

    // -- Typed required query parameters
    class RequiredQueryParametersImpl(val version: Int) : RequiredQueryParameters

    // -- Typed request headers
    class RequestHeadersImpl(val traceId: String?) : RequestHeaders

    // -- Combined typed request data (path + query + headers + body)
    class RequestDataImpl(
        override val pathParameter: PathParametersImpl,
        override val optionalQueryParameter: OptionalQueryParametersImpl,
        override val requiredQueryParameter: RequiredQueryParametersImpl,
        override val headers: RequestHeadersImpl,
        override val requestBody: GreetingRequest
    ) : RequestWithBodyData<GreetingRequest>

    // -- Typed response data
    class ResponseDataImpl(
        override val httpStatusCode: HttpStatusCode,
        override val responseBody: GreetingResponse?,
        override val responseMessage: String?,
        override val responseHeaders: Nothing? = null
    ) : ResponseDataWithBody<GreetingResponse>

    // -- Endpoint declaration: POST /greetings/{name}, JSON in and out
    override val requestMapping = PostRequestMapping(
        name = "createGreeting",
        path = "/greetings/{name}",
        produces = ContentType.Application.Json,
        consumes = ContentType.Application.Json
    )

    override val conversionService = DefaultConversionService

    // -- Role-based authorization (empty set = no roles required)
    override val roleBasedAuthorizationConfiguration = RoleBasedAuthorizationConfigurationImpl(
        enabled = false,
        requiredRolesAllOf = emptySet()
    )

    // -- Extract typed path parameters from the call
    override suspend fun ApplicationCall.getPathParameters(): PathParametersImpl {
        val name: String by RequiredParametersHandler(parameters, conversionService)
        return PathParametersImpl(name = name)
    }

    // -- Extract optional query parameters
    override suspend fun ApplicationCall.getOptionalQueryParameters(): OptionalQueryParametersImpl {
        val locale: String? by OptionalParametersHandler(request.queryParameters, conversionService)
        return OptionalQueryParametersImpl(locale = locale)
    }

    // -- Extract required query parameters
    override suspend fun ApplicationCall.getRequiredQueryParameters(): RequiredQueryParametersImpl {
        val version: Int by RequiredParametersHandler(request.queryParameters, conversionService)
        return RequiredQueryParametersImpl(version = version)
    }

    // -- Extract request headers
    override suspend fun ApplicationCall.getRequestHeaders(): RequestHeadersImpl {
        val traceId: String? by OptionalHeadersHandler(request.headers, conversionService)
        return RequestHeadersImpl(traceId = traceId)
    }

    // -- The RouteProcessor: wires request data extraction to a business logic function
    class RouteProcessorImpl(
        override val requestMapping: PostRequestMapping = CreateGreeting.requestMapping,
        override val roleBasedAuthorizationConfiguration: RoleBasedAuthorizationConfigurationImpl =
            CreateGreeting.roleBasedAuthorizationConfiguration,
        override val processRequest: suspend PipelineContext<Unit, ApplicationCall>.(
            RequestDataImpl
        ) -> ResponseDataImpl
    ) : PostRouteProcessor<GreetingRequest, RequestDataImpl, GreetingResponse, ResponseDataImpl> {

        override suspend fun ApplicationCall.prepareRequestData() = RequestDataImpl(
            pathParameter = getPathParameters(),
            optionalQueryParameter = getOptionalQueryParameters(),
            requiredQueryParameter = getRequiredQueryParameters(),
            headers = getRequestHeaders(),
            requestBody = receive()
        )

        override suspend fun ApplicationCall.respondWithBody(
            httpStatusCode: HttpStatusCode,
            responseBody: GreetingResponse
        ) = respond(httpStatusCode, responseBody)
    }
}
```

### 3. Define the controller interface

```kotlin
interface GreetingController : Controller {
    val createGreeting: CreateGreeting.RouteProcessorImpl

    override val endpoints: List<RouteProcessorBase<*, *, *>>
        get() = listOf(createGreeting)
}
```

### 4. Implement the controller (wire in your service)

```kotlin
object GreetingControllerImpl : GreetingController {
    override val authenticationConfiguration: AuthenticationConfiguration =
        AUTHENTICATION_CONFIGURATION_DEFAULT

    override val createGreeting = CreateGreeting.RouteProcessorImpl(
        processRequest = { requestData ->
            val greeting = "Hello, ${requestData.pathParameter.name}! You said: ${requestData.requestBody.message}"
            CreateGreeting.ResponseDataImpl(
                httpStatusCode = HttpStatusCode.Created,
                responseBody = GreetingResponse(greeting),
                responseMessage = null
            )
        }
    )
}
```

### 5. Install in your Ktor application

```kotlin
fun Application.configureRouting() {
    routing {
        install(GreetingControllerImpl)
    }
}
```

That's it. The library automatically:
- Routes `POST /greetings/{name}` to your handler
- Validates `Accept: application/json` and `Content-Type: application/json`
- Extracts and converts all path parameters, query parameters, headers, and the body into typed objects
- Checks required roles (if configured)
- Serializes the response body and sets the HTTP status code

---

## HTTP methods supported

All standard HTTP methods have a matching pair of `RouteConfig` and `RouteProcessor` interfaces:

| HTTP Method | RouteConfig interface | RouteProcessor interface |
|-------------|-----------------------|--------------------------|
| GET         | `GetRouteConfig`      | `GetRouteProcessor`      |
| POST        | `PostRouteConfig`     | `PostRouteProcessor`     |
| PUT         | `PutRouteConfig`      | `PutRouteProcessor`      |
| PATCH       | `PatchRouteConfig`    | `PatchRouteProcessor`    |
| DELETE      | `DeleteRouteConfig`   | `DeleteRouteProcessor`   |
| HEAD        | `HeadRouteConfig`     | `HeadRouteProcessor`     |
| OPTIONS     | `OptionsRouteConfig`  | `OptionsRouteProcessor`  |

---

## Parameter extraction utilities

`ktor2-server-bind` provides delegate-based helpers to extract and type-convert parameters from `ApplicationCall`:

```kotlin
// Required parameter — throws MissingRequestParameterException if absent
val id: Int by RequiredParametersHandler(parameters, conversionService)

// Optional parameter — returns null if absent
val page: Int? by OptionalParametersHandler(request.queryParameters, conversionService)

// Required header — throws MissingHeaderException if absent
val authorization: String by RequiredHeadersHandler(request.headers, conversionService)

// Optional header — returns null if absent
val traceId: String? by OptionalHeadersHandler(request.headers, conversionService)
```

The variable name is used as the parameter key automatically via Kotlin property delegates.

---

## Authentication and authorization

### Controller-level authentication

Set `authenticationConfiguration` on your controller to wrap all its endpoints in Ktor's `authenticate {}` block:

```kotlin
override val authenticationConfiguration = AuthenticationConfigurationImpl(
    enable = true,
    providers = arrayOf("jwt"),  // matches your Ktor auth provider name
    optional = false
)
```

Use `AUTHENTICATION_CONFIGURATION_DEFAULT` to enable authentication for a controller using the default unnamed provider (equivalent to Ktor's `authenticate(null)`).

### Endpoint-level role-based authorization

Each `RouteProcessor` carries its own `roleBasedAuthorizationConfiguration`. Use `RoleBasedAuthorizationConfigurationImpl` to enforce that the authenticated principal holds all required roles:

```kotlin
override val roleBasedAuthorizationConfiguration = RoleBasedAuthorizationConfigurationImpl(
    enabled = true,
    requiredRolesAllOf = setOf("ADMIN", "WRITE_GREETINGS")
)
```

Your principal must implement `PrincipalRoles` so the library can inspect its roles:

```kotlin
data class UserPrincipal(
    val userId: String,
    override val roles: Set<String>
) : Principal, PrincipalRoles
```

---

## Exception handling

Install the predefined bad-request exception handler inside Ktor's `StatusPages` plugin to automatically respond `400 Bad Request` when required parameters or headers are missing or cannot be converted:

```kotlin
install(StatusPages) {
    predefinedBadRequestExceptionsHandler()
}
```

This handles:
- `MissingRequestParameterException` — a required query or path parameter is absent
- `ParameterConversionException` — a parameter value cannot be converted to the target type
- `MissingHeaderException` — a required header is absent

You can supply a custom handler builder to override the default behavior:

```kotlin
install(StatusPages) {
    predefinedBadRequestExceptionsHandler(
        predefinedBadExceptionsHandlerBuilder = ExceptionHandling.defaultPredefinedBadExceptionsHandlerBuilder
    )
}
```

---

## OpenAPI-first development with openapi-generator

`ktor2-server-bind` is an ideal foundation for building a **Ktor server generator** for the [OpenAPI Generator](https://openapi-generator.tech) project, enabling a fully **spec-first** (design-first) workflow for Ktor APIs.

### What is spec-first development?

In a spec-first approach you write your OpenAPI specification (`openapi.yaml`) before writing any code. A code generator then reads that specification and produces:

- **Controller interfaces** — one per API tag, each method corresponding to an operation in the spec.
- **`RouteConfig` objects** — one per operation, fully typed with the path, HTTP method, content types, path/query parameters, request body, and response models derived directly from the spec.
- **DTO data classes** — request and response models with `@Serializable` annotations, matching the spec's `components/schemas`.
- **Stub implementations** — ready-to-compile controller implementations that you fill in with your business logic.

Because `ktor2-server-bind` already separates the *shape* of an endpoint (`RouteConfig`) from its *behavior* (`RouteProcessor`), the boundary between generated and handwritten code is clean: the generator owns the interfaces and configs, you own the implementations.

### Why `ktor2-server-bind` is a natural fit

| OpenAPI concept                    | `ktor2-server-bind` equivalent                                                    |
|------------------------------------|-----------------------------------------------------------------------------------|
| Operation (`operationId`)          | `RouteConfig` object + `RouteProcessor` implementation                            |
| Tag                                | `Controller` interface grouping related `RouteProcessor`s                         |
| `parameters` (path, query, header) | Typed `PathParameters`, `RequiredQueryParameters`, `RequestHeaders` inner classes |
| `requestBody`                      | Typed body extracted via `receive<T>()` in `prepareRequestData()`                 |
| `responses`                        | Typed `ResponseData` / `ResponseDataWithBody` return values                       |
| `security`                         | `AuthenticationConfiguration` + `RoleBasedAuthorizationConfiguration`             |

### Example generator output for a `POST /pets` operation

Given an OpenAPI spec with:

```yaml
paths:
  /pets:
    post:
      operationId: createPet
      tags: [Pets]
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/NewPet'
      responses:
        '201':
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Pet'
```

A Ktor generator built on `ktor2-server-bind` would emit:

```kotlin
// Generated — do not edit
object CreatePet : PostRouteConfig {
    class RequestDataImpl(
        override val pathParameter: Nothing? = null,
        override val optionalQueryParameter: Nothing? = null,
        override val requiredQueryParameter: Nothing? = null,
        override val headers: Nothing? = null,
        override val requestBody: NewPet
    ) : RequestWithBodyData<NewPet>

    class ResponseDataImpl(
        override val httpStatusCode: HttpStatusCode,
        override val responseBody: Pet?,
        override val responseMessage: String?,
        override val responseHeaders: Nothing? = null
    ) : ResponseDataWithBody<Pet>

    override val requestMapping = PostRequestMapping(
        name = "createPet",
        path = "/pets",
        produces = ContentType.Application.Json,
        consumes = ContentType.Application.Json
    )
    // ... conversionService, roleBasedAuthorizationConfiguration, parameter extractors
}

// Generated — do not edit
interface PetsController : Controller {
    val createPet: CreatePet.RouteProcessorImpl
    override val endpoints get() = listOf(createPet)
}
```

You then implement only the business logic:

```kotlin
// Handwritten — your code
object PetsControllerImpl : PetsController {
    override val authenticationConfiguration = AUTHENTICATION_CONFIGURATION_DEFAULT

    override val createPet = CreatePet.RouteProcessorImpl { requestData ->
        val saved = petService.save(requestData.requestBody)
        CreatePet.ResponseDataImpl(HttpStatusCode.Created, saved, null)
    }
}
```

### Contributing a Ktor generator

If you are interested in contributing a `kotlin-ktor-server` generator template to the OpenAPI Generator project, `ktor2-server-bind` provides all the runtime primitives the generated code needs — no additional runtime dependencies required beyond Ktor itself. See the [OpenAPI Generator contribution guide](https://openapi-generator.tech/docs/contributing) for how to add a new server generator.

---

## Publishing to Maven Central

The build is pre-configured to publish to Maven Central via **OSSRH** (Sonatype OSS Repository Hosting).

For full instructions — what OSSRH is, how to get and configure credentials, GPG signing setup, local publishing steps, and a pre-release checklist — see **[PUBLISHING.md](PUBLISHING.md)**.

A **GitHub Actions workflow** is also provided at [`.github/workflows/publish.yml`](.github/workflows/publish.yml). Trigger it manually from the **Actions** tab, choose `snapshot` or `release`, and the pipeline handles the rest (signing included for releases).

---

## Contributing

For instructions on how to contribute to the project, including environment setup, code style (ktlint/detekt), and running tests, please see **[CONTRIBUTE.md](CONTRIBUTE.md)**.

---

## License

```
Copyright 2024 withLambda

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
