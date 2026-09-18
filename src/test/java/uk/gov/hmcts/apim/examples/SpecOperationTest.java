package uk.gov.hmcts.apim.examples;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpecOperationTest {

    private static final ExampleResponse CREATED = example(201, "created");
    private static final ExampleResponse OK = example(200, "ok");
    private static final ExampleResponse BAD_REQUEST = example(400, "invalid");

    @Test
    void selecting_without_filters_should_prefer_the_lowest_success_status() {
        assertThat(operation(BAD_REQUEST, CREATED, OK).select(null, null))
            .hasValueSatisfying(example -> assertThat(example.status()).isEqualTo(200));
    }

    @Test
    void selecting_a_status_should_return_only_that_example() {
        assertThat(operation(OK, BAD_REQUEST).select(400, null))
            .hasValueSatisfying(example -> assertThat(example.status()).isEqualTo(400));
    }

    @Test
    void selecting_a_status_with_no_example_should_return_nothing() {
        assertThat(operation(OK, BAD_REQUEST).select(404, null)).isEmpty();
    }

    @Test
    void selecting_by_name_should_return_that_example() {
        assertThat(operation(OK, BAD_REQUEST).select(null, "invalid"))
            .hasValueSatisfying(example -> assertThat(example.name()).isEqualTo("invalid"));
    }

    @Test
    void selecting_a_name_that_is_not_declared_should_return_nothing() {
        assertThat(operation(OK, BAD_REQUEST).select(null, "nope")).isEmpty();
    }

    @Test
    void an_operation_with_only_error_examples_should_still_select_one() {
        assertThat(operation(BAD_REQUEST).select(null, null))
            .hasValueSatisfying(example -> assertThat(example.status()).isEqualTo(400));
    }

    @Test
    void an_operation_with_no_examples_should_report_none() {
        SpecOperation operation = operation();

        assertThat(operation.hasExamples()).isFalse();
        assertThat(operation.select(null, null)).isEmpty();
    }

    @Test
    void describing_an_operation_should_read_as_method_then_path() {
        assertThat(operation(OK).describe()).isEqualTo("GET /thing/{id}");
    }

    private static ExampleResponse example(int status, String name) {
        return new ExampleResponse(status, "application/json", name, "body");
    }

    private static SpecOperation operation(ExampleResponse... examples) {
        return new SpecOperation(
            HttpMethod.GET,
            "/thing/{id}",
            new PathPatternParser().parse("/thing/{id}"),
            List.of(examples));
    }
}
