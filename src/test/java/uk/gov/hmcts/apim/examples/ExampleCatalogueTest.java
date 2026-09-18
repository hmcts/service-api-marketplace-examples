package uk.gov.hmcts.apim.examples;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExampleCatalogueTest {

    private static final String SPEC = """
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
        """;

    @Test
    void loading_a_reachable_spec_should_expose_it_in_the_catalogue() {
        ExampleCatalogue catalogue = catalogue(url -> SPEC);

        catalogue.load();

        assertThat(catalogue.find("hrds")).isPresent();
        assertThat(catalogue.all()).hasSize(1);
        assertThat(catalogue.codes()).containsExactly("hrds");
    }

    @Test
    void loading_a_spec_that_cannot_be_fetched_should_leave_it_out_rather_than_fail() {
        ExampleCatalogue catalogue = catalogue(url -> {
            throw new SpecLoadException("nope");
        });

        catalogue.load();

        assertThat(catalogue.find("hrds")).isEmpty();
        assertThat(catalogue.all()).isEmpty();
        assertThat(catalogue.codes()).containsExactly("hrds");
    }

    @Test
    void loading_a_spec_that_cannot_be_parsed_should_leave_it_out_rather_than_fail() {
        ExampleCatalogue catalogue = catalogue(url -> "not a spec at all");

        catalogue.load();

        assertThat(catalogue.find("hrds")).isEmpty();
    }

    private ExampleCatalogue catalogue(SpecFetcher fetcher) {
        SpecProperties properties = new SpecProperties();
        properties.setSpecs(List.of(new SpecSource("hrds", "Hearing Results", "https://example.com/spec.yml")));
        return new ExampleCatalogue(properties, fetcher, new SpecParser());
    }
}
