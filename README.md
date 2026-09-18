# apim-marketplace-examples

Mock server for the HMCTS API Marketplace. It pulls published API specifications and serves the
examples they contain, so consumers can try an API before a real implementation exists.

Currently a single `hello world` endpoint — spec loading is not implemented yet.

## How it works

At startup the service fetches each configured OpenAPI spec, extracts the examples declared against
each operation's responses, and serves them under a path matching the API's short code. `GET
/hrds/event-types` returns the `EventTypesResponse` example from the hrds spec.

Specs are configured in `application.yaml`, so adding an API is data rather than code:

```yaml
marketplace:
  specs:
    - code: hrds
      name: Hearing Results Document Subscription
      url: https://raw.githubusercontent.com/hmcts/api-cp-crime-hearing-results-document-subscription/main/src/main/resources/openapi/openapi-spec.yml
```

A spec that cannot be fetched is logged and skipped — it never fails startup, so one unreachable
repository cannot take the pod's readiness probe down with it.

## Endpoints

| Method | Path               | Description                                      |
|--------|--------------------|--------------------------------------------------|
| GET    | `/apis`            | The APIs served here and their operations         |
| any    | `/{code}/**`       | The spec example for the matching operation       |
| GET    | `/`                | Welcome message (Azure "Always On" ping)          |
| GET    | `/hello`           | Returns `Hello World`                             |
| GET    | `/health`          | Actuator health                                   |
| GET    | `/swagger-ui.html` | OpenAPI UI                                        |

### Serving examples

Path templates in the spec match concrete values, and the most specific template wins:

```bash
curl http://localhost:8081/hrds/event-types
curl http://localhost:8081/hrds/client-subscriptions/aa12f3dd-4cc1-4da7-b9ea-552fa3b9bc44
```

The example returned is the lowest 2xx declared for the operation, and the first example under it.
Both are overridable:

| Parameter  | Effect                                      |
|------------|---------------------------------------------|
| `status`   | Return the example for that status instead  |
| `example`  | Return the named example instead            |

```bash
curl "http://localhost:8081/hrds/client-subscriptions?status=400"
```

Operations whose spec declares no response example return 404 naming the API and the operation.
Request-body examples are not served yet.

## Building and running

```bash
./gradlew build
```

```bash
./gradlew bootRun
```

The service listens on port `8081`.

```bash
curl http://localhost:8081/hello
```

### Docker

```bash
docker-compose up
```

## Tests

| Task                  | Scope                                   |
|-----------------------|-----------------------------------------|
| `./gradlew test`      | Unit tests                              |
| `./gradlew integration` | Spring MVC slice and OpenAPI publishing |
| `./gradlew smoke`     | Against a running instance (`TEST_URL`) |
| `./gradlew functional`| Against a running instance (`TEST_URL`) |

`./gradlew check` runs unit tests, integration tests and checkstyle.

## Infrastructure

Deployed via the `apim-marketplace-examples` Helm chart. No database and no key vault — the service
holds no secrets. If spec fetching later needs a credential, add a `keyVaults` block to
`charts/apim-marketplace-examples/values.yaml` and a `spring.config.import` configtree entry to
`src/main/resources/application.yaml`.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
