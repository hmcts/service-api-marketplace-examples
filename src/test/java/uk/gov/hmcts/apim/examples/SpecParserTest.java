package uk.gov.hmcts.apim.examples;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SpecParserTest {

    private static final SpecSource HRDS = new SpecSource("hrds", "Hearing Results", "https://example.com/spec.yml");

    private ApiExamples parsed;

    @BeforeEach
    void setUp() throws IOException {
        parsed = new SpecParser().parse(HRDS, specContent());
    }

    @Test
    void parsing_the_hrds_spec_should_extract_the_event_types_example() {
        Optional<SpecOperation> operation = parsed.match(HttpMethod.GET, "/event-types");

        assertThat(operation).isPresent();
        Optional<ExampleResponse> example = operation.get().select(null, null);
        assertThat(example).isPresent();
        assertThat(example.get().status()).isEqualTo(200);
        assertThat(example.get().mediaType()).isEqualTo("application/json");
        assertThat(example.get().value()).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) example.get().value()).get("events")).isNotNull();
    }

    @Test
    void matching_a_templated_path_should_bind_a_concrete_identifier() {
        Optional<SpecOperation> operation =
            parsed.match(HttpMethod.GET, "/client-subscriptions/aa12f3dd-4cc1-4da7-b9ea-552fa3b9bc44");

        assertThat(operation).isPresent();
        assertThat(operation.get().pathTemplate()).isEqualTo("/client-subscriptions/{clientSubscriptionId}");
    }

    @Test
    void matching_should_prefer_the_more_specific_template() {
        Optional<SpecOperation> operation =
            parsed.match(HttpMethod.POST, "/client-subscriptions/aa12f3dd/secret/rotate");

        assertThat(operation).isPresent();
        assertThat(operation.get().pathTemplate())
            .isEqualTo("/client-subscriptions/{clientSubscriptionId}/secret/rotate");
    }

    @Test
    void selecting_without_a_status_should_prefer_the_success_example() {
        Optional<SpecOperation> operation = parsed.match(HttpMethod.POST, "/client-subscriptions");

        assertThat(operation).isPresent();
        assertThat(operation.get().select(null, null)).hasValueSatisfying(
            example -> assertThat(example.status()).isEqualTo(201));
    }

    @Test
    void selecting_an_error_status_should_return_that_example() {
        Optional<SpecOperation> operation = parsed.match(HttpMethod.POST, "/client-subscriptions");

        assertThat(operation).isPresent();
        assertThat(operation.get().select(400, null)).hasValueSatisfying(
            example -> assertThat(example.status()).isEqualTo(400));
    }

    @Test
    void an_operation_with_no_response_example_should_report_none() {
        Optional<SpecOperation> operation = parsed.match(HttpMethod.POST, "/notifications");

        assertThat(operation).isPresent();
        assertThat(operation.get().select(null, null)).isEmpty();
    }

    @Test
    void listing_operations_should_only_include_those_with_examples() {
        assertThat(parsed.operationsWithExamples())
            .contains("GET /event-types", "POST /client-subscriptions")
            .doesNotContain("POST /notifications");
    }

    private String specContent() throws IOException {
        try (var stream = getClass().getResourceAsStream("/specs/hrds-openapi-spec.cached.yml")) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
