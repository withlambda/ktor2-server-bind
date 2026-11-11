# Module ktor2-server-bind

Provides interface-driven, data-bound controller patterns for [Ktor 2](https://ktor.io) servers.

## Overview

`ktor2-server-bind` enforces a clean separation between an endpoint's **API specification**
(path, HTTP method, content types, parameter extraction, validation, authorization) defined in a
`RouteConfig`, and its **business logic** implemented in a `RouteProcessor`.

Key capabilities:
- Strongly typed extraction of path parameters, query parameters, headers, and request bodies
- Automatic `Accept` / `Content-Type` header negotiation
- Role-based authorization per endpoint via `RoleBasedAuthorizationConfiguration`
- Declarative request-body validation using [Konform](https://github.com/konform-kt/konform)
- Predefined `StatusPages` exception handlers for missing/invalid parameters and validation failures
- KSP annotation processor (`ktor2-server-bind-processor`) that generates an `openapi.json` resource
  from `@ApiController` / `@ApiEndpoint` annotated classes at compile time

## Packages

| Package                                       | Description                                                                                 |
|-----------------------------------------------|---------------------------------------------------------------------------------------------|
| `io.withlambda.ktor2.server.bind`            | Core interfaces: `Controller`, `RouteConfig`, `RouteProcessor` and all HTTP-method variants |
| `io.withlambda.ktor2.server.bind.config`     | `AuthenticationConfiguration`, `RoleBasedAuthorizationConfiguration`, `LogLevel`, `Logging` |
| `io.withlambda.ktor2.server.bind.exception`  | `BindValidationException`, `ExceptionHandlerBuilder`, `ExceptionHandling` helpers           |
| `io.withlambda.ktor2.server.bind.plugins`    | `RoleBasedAuthorization` Ktor plugin                                                        |
| `io.withlambda.ktor2.server.bind.request`    | Parameter/header delegate handlers, `FileItemDTO`, request-mapping types                    |
| `io.withlambda.ktor2.server.bind.response`   | `ResponseData`, `ResponseDataWithBody`, `ResponseHeaders`                                   |
| `io.withlambda.ktor2.server.bind.util`       | `MultipartHandler` and other internal utilities                                             |
| `io.withlambda.ktor2.server.bind.validation` | Bridges `RouteConfig.validation` to Ktor's `RequestValidation` plugin                       |

## See also

- [README](https://github.com/withlambda/ktor2-server-bind/blob/main/README.md) — full usage guide with examples
- [CONTRIBUTE.md](https://github.com/withlambda/ktor2-server-bind/blob/main/CONTRIBUTE.md) — development setup, style rules, and test instructions
- [PUBLISHING.md](https://github.com/withlambda/ktor2-server-bind/blob/main/PUBLISHING.md) — Maven Central publishing guide
