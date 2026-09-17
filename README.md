# apim-marketplace-examples

Mock server for the HMCTS API Marketplace. It pulls published API specifications and serves the
examples they contain, so consumers can try an API before a real implementation exists.

Currently a single `hello world` endpoint — spec loading is not implemented yet.

## Endpoints

| Method | Path           | Description                            |
|--------|----------------|----------------------------------------|
| GET    | `/`            | Welcome message (Azure "Always On" ping) |
| GET    | `/hello`       | Returns `Hello World`                  |
| GET    | `/health`      | Actuator health                        |
| GET    | `/swagger-ui.html` | OpenAPI UI                         |
| GET    | `/v3/api-docs` | OpenAPI spec                           |

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
