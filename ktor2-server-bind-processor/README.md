# ktor2-server-bind-processor

[![Maven Central](https://img.shields.io/maven-central/v/io.withlambda/ktor2-server-bind-processor)](https://central.sonatype.com/artifact/io.withlambda/ktor2-server-bind-processor)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

A **KSP (Kotlin Symbol Processing) compile-time companion** to [`ktor2-server-bind`](../README.md).

At build time this processor scans every class annotated with `@ApiController`, inspects its `@ApiEndpoint`-annotated `RouteConfig` properties, and writes a fully-formed `openapi.json` (OpenAPI 3.0.3) resource file into the compilation output — with no hand-written boilerplate required.

---

## Why use it?

Writing and maintaining an OpenAPI specification by hand is error-prone and quickly falls out of sync with the actual implementation. `ktor2-server-bind-processor` solves this by generating the spec **automatically from your existing `ktor2-server-bind` controller definitions** at compile time:

- **Zero extra code** — annotate what you already have; the processor does the rest.
- **Always in sync** — the spec is regenerated on every build, so it can never drift from the implementation.
- **No runtime overhead** — the processor runs only during compilation; nothing is added to your application's runtime classpath.
- **OpenAPI 3.0.3 output** — produces a standards-compliant `openapi.json` including paths, HTTP methods, request body schemas, and component schemas for all referenced DTOs.

---

## Installation

> **Prerequisite:** `ktor2-server-bind` must already be on your `implementation` classpath — the processor is meaningless without it.

### 1. Apply the KSP plugin

```kotlin
// build.gradle.kts
plugins {
    id("com.google.devtools.ksp") version "<ksp-version>"
}
```

Or with a version catalog (`libs.versions.toml`):

```toml
[versions]
ksp = "<ksp-version>"

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.ksp)
}
```

### 2. Add both artifacts

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.withlambda:ktor2-server-bind:1.0.0")
    ksp("io.withlambda:ktor2-server-bind-processor:1.0.0")
}
```

Or with a version catalog:

```toml
[versions]
ktor2-server-bind = "1.0.0"

[libraries]
ktor2-server-bind        = { module = "io.withlambda:ktor2-server-bind",           version.ref = "ktor2-server-bind" }
ktor2-server-bind-ksp    = { module = "io.withlambda:ktor2-server-bind-processor",  version.ref = "ktor2-server-bind" }
```

```kotlin
// build.gradle.kts
dependencies {
    implementation(libs.ktor2.server.bind)
    ksp(libs.ktor2.server.bind.ksp)
}
```

> **Important:** Always use the **same version** for both artifacts. The processor is tightly coupled to the annotations and `RouteConfig` types defined in `ktor2-server-bind`.

---

## Usage

### 1. Annotate your controller class with `@ApiController`

```kotlin
@ApiController(tag = "Greetings")
class GreetingControllerImpl : GreetingController {
    // ...
}
```

The optional `tag` groups all endpoints from this controller under a single tag in the generated spec.

### 2. Annotate each `RouteConfig` property with `@ApiEndpoint`

```kotlin
@ApiController(tag = "Greetings")
class GreetingControllerImpl : GreetingController {

    @ApiEndpoint(path = "/greetings/{name}", summary = "Returns a personalised greeting")
    override val getGreeting = GetGreeting.RouteProcessorImpl(
        processRequest = { requestData ->
            // ...
        }
    )

    @ApiEndpoint(path = "/greetings", summary = "Creates a new greeting")
    override val createGreeting = CreateGreeting.RouteProcessorImpl(
        processRequest = { requestData ->
            // ...
        }
    )
}
```

| Annotation argument | Required | Description |
|---|---|---|
| `path` | ✅ | The URL path (e.g. `"/greetings/{name}"`). |
| `summary` | ❌ | A short human-readable description of the endpoint. |

### 3. Build your project

```bash
./gradlew build
```

The processor runs automatically as part of the KSP compilation phase and writes `openapi.json` into the build output under `build/generated/ksp/`.

---

## Generated output

Given two annotated endpoints — one `GET` and one `POST` with a request body — the processor produces an `openapi.json` like this:

```json
{
  "openapi": "3.0.3",
  "info": {
    "title": "API",
    "version": "1.0.0"
  },
  "paths": {
    "/greetings/{name}": {
      "get": {
        "summary": "Returns a personalised greeting",
        "tags": ["Greetings"],
        "responses": {
          "200": {"description": "OK"}
        }
      }
    },
    "/greetings": {
      "post": {
        "summary": "Creates a new greeting",
        "tags": ["Greetings"],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {"$ref": "#/components/schemas/GreetingRequest"}
            }
          }
        },
        "responses": {
          "200": {"description": "OK"}
        }
      }
    }
  },
  "components": {
    "schemas": {
      "GreetingRequest": {
        "type": "object",
        "properties": {
          "message": {"type": "string"}
        }
      }
    }
  }
}
```

### What is included automatically

| OpenAPI section | Source |
|---|---|
| `paths` — HTTP method | Derived from the `RouteConfig` subtype (`GetRouteConfig` → `get`, `PostRouteConfig` → `post`, etc.) |
| `paths` — path | `@ApiEndpoint(path = …)` |
| `paths` — summary | `@ApiEndpoint(summary = …)` |
| `paths` — tags | `@ApiController(tag = …)` |
| `paths` — requestBody schema | First generic type argument of `PostRouteConfig<T>`, `PutRouteConfig<T>`, `PatchRouteConfig<T>` |
| `components/schemas` | All DTO types referenced as request bodies, collected recursively |

---

## HTTP methods supported

The processor recognises all `RouteConfig` subtypes from `ktor2-server-bind`:

| RouteConfig subtype | Generated HTTP method |
|---|---|
| `GetRouteConfig` | `get` |
| `PostRouteConfig<T>` | `post` |
| `PutRouteConfig<T>` | `put` |
| `PatchRouteConfig<T>` | `patch` |
| `DeleteRouteConfig` | `delete` |
| `HeadRouteConfig` | `head` |
| `OptionsRouteConfig` | `options` |

---

## Relationship to `ktor2-server-bind`

This processor is the **compile-time companion** to the `ktor2-server-bind` runtime library. The two artifacts are always versioned together and must be declared in a consuming project alongside each other:

| Artifact | Gradle configuration | Purpose |
|---|---|---|
| `io.withlambda:ktor2-server-bind` | `implementation(…)` | Runtime library your code compiles and runs against |
| `io.withlambda:ktor2-server-bind-processor` | `ksp(…)` | Compile-time KSP processor; never on the runtime classpath |

---

## Publishing to Maven Central

The build is pre-configured to publish to Maven Central via **OSSRH** (Sonatype OSS Repository Hosting). Both `ktor2-server-bind` and `ktor2-server-bind-processor` are always published together as part of the same release pipeline.

For full instructions — credentials, GPG signing setup, local publishing steps, and a pre-release checklist — see **[PUBLISHING.md](../PUBLISHING.md)**.

---

## Contributing

For instructions on how to contribute to the project, including environment setup, code style (ktlint/detekt), and running tests, please see **[CONTRIBUTE.md](../CONTRIBUTE.md)**.

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
