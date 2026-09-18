package uk.gov.hmcts.apim.examples;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpecParserEdgeCaseTest {

    private static final SpecSource SOURCE = new SpecSource("test", null, "https://example.com/spec.yml");

    private final SpecParser parser = new SpecParser();

    @Test
    void parsing_content_that_is_not_a_spec_should_report_the_api_code() {
        assertThatThrownBy(() -> parser.parse(SOURCE, "this is not a spec"))
            .isInstanceOf(SpecLoadException.class)
            .hasMessageContaining("test");
    }

    @Test
    void parsing_a_spec_with_no_paths_should_yield_no_operations() {
        ApiExamples parsed = parser.parse(SOURCE, """
            openapi: 3.0.3
            info:
              title: Empty
              version: '1'
            paths: {}
            """);

        assertThat(parsed.operations()).isEmpty();
        assertThat(parsed.operationsWithExamples()).isEmpty();
    }

    @Test
    void source_without_a_name_should_fall_back_to_the_spec_title() {
        ApiExamples parsed = parser.parse(SOURCE, """
            openapi: 3.0.3
            info:
              title: Titled Spec
              version: '1'
            paths: {}
            """);

        assertThat(parsed.name()).isEqualTo("Titled Spec");
    }

    @Test
    void response_with_no_content_should_produce_no_example() {
        ApiExamples parsed = parser.parse(SOURCE, """
            openapi: 3.0.3
            info:
              title: T
              version: '1'
            paths:
              /thing:
                delete:
                  responses:
                    '204':
                      description: gone
            """);

        assertThat(parsed.match(HttpMethod.DELETE, "/thing")).isPresent();
        assertThat(parsed.match(HttpMethod.DELETE, "/thing").get().hasExamples()).isFalse();
    }

    @Test
    void default_response_status_should_be_skipped() {
        ApiExamples parsed = parser.parse(SOURCE, """
            openapi: 3.0.3
            info:
              title: T
              version: '1'
            paths:
              /thing:
                get:
                  responses:
                    default:
                      description: anything
                      content:
                        application/json:
                          example:
                            message: ignored
            """);

        assertThat(parsed.match(HttpMethod.GET, "/thing").get().hasExamples()).isFalse();
    }

    @Test
    void singular_example_should_be_served_under_the_name_default() {
        ApiExamples parsed = parser.parse(SOURCE, """
            openapi: 3.0.3
            info:
              title: T
              version: '1'
            paths:
              /thing:
                get:
                  responses:
                    '200':
                      description: ok
                      content:
                        application/json:
                          example:
                            message: hello
            """);

        assertThat(parsed.match(HttpMethod.GET, "/thing").get().select(null, null))
            .hasValueSatisfying(example -> {
                assertThat(example.name()).isEqualTo("default");
                assertThat(example.status()).isEqualTo(200);
            });
    }

    @Test
    void an_example_ref_that_does_not_resolve_should_be_skipped() {
        ApiExamples parsed = parser.parse(SOURCE, """
            openapi: 3.0.3
            info:
              title: T
              version: '1'
            paths:
              /thing:
                get:
                  responses:
                    '200':
                      description: ok
                      content:
                        application/json:
                          examples:
                            Missing:
                              $ref: '#/components/examples/NotDeclared'
            components:
              examples:
                Declared:
                  value:
                    message: hello
            """);

        assertThat(parsed.match(HttpMethod.GET, "/thing").get().hasExamples()).isFalse();
    }

    @Test
    void an_example_ref_into_components_should_resolve_to_its_value() {
        ApiExamples parsed = parser.parse(SOURCE, """
            openapi: 3.0.3
            info:
              title: T
              version: '1'
            paths:
              /thing:
                get:
                  responses:
                    '200':
                      description: ok
                      content:
                        application/json:
                          examples:
                            Greeting:
                              $ref: '#/components/examples/Declared'
            components:
              examples:
                Declared:
                  value:
                    message: hello
            """);

        assertThat(parsed.match(HttpMethod.GET, "/thing").get().select(null, null))
            .hasValueSatisfying(example -> assertThat(example.name()).isEqualTo("Greeting"));
    }

    @Test
    void content_declaring_no_examples_should_produce_none() {
        ApiExamples parsed = parser.parse(SOURCE, """
            openapi: 3.0.3
            info:
              title: T
              version: '1'
            paths:
              /thing:
                get:
                  responses:
                    '200':
                      description: ok
                      content:
                        application/json:
                          schema:
                            type: object
            """);

        assertThat(parsed.match(HttpMethod.GET, "/thing").get().hasExamples()).isFalse();
    }
}
