package uk.gov.hmcts.apim.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "marketplace.specs[0].code=hrds",
    "marketplace.specs[0].name=Hearing Results Document Subscription",
    "marketplace.specs[0].url=classpath:hrds"
})
class ExampleEndpointIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExampleCatalogue catalogue;

    @BeforeEach
    void setUp() {
        catalogue.load();
    }

    @Test
    void getting_a_known_operation_should_return_the_spec_example() throws Exception {
        String body = mockMvc.perform(get("/hrds/event-types"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/json"))
            .andExpect(jsonPath("$.events[0].eventName").value("PRISON_COURT_REGISTER_GENERATED"))
            .andReturn().getResponse().getContentAsString();

        log.info("GET /hrds/event-types\n{}", pretty(body));
    }

    @Test
    void getting_a_templated_path_should_return_the_example_for_any_identifier() throws Exception {
        String body = mockMvc.perform(get("/hrds/client-subscriptions/aa12f3dd-4cc1-4da7-b9ea-552fa3b9bc44"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        log.info("GET /hrds/client-subscriptions/{id}\n{}", pretty(body));
    }

    @Test
    void listing_apis_should_show_the_operations_loaded_from_the_spec() throws Exception {
        String body = mockMvc.perform(get("/apis"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        log.info("GET /apis\n{}", pretty(body));
    }

    @Test
    void posting_a_create_should_return_the_201_example() throws Exception {
        mockMvc.perform(post("/hrds/client-subscriptions"))
            .andExpect(status().isCreated());
    }

    @Test
    void asking_for_an_error_status_should_return_that_example() throws Exception {
        mockMvc.perform(post("/hrds/client-subscriptions").param("status", "400"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void an_operation_with_no_example_should_return_404_naming_the_api() throws Exception {
        mockMvc.perform(post("/hrds/notifications"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.api").value("hrds"))
            .andExpect(jsonPath("$.request").value("POST /notifications"))
            .andExpect(jsonPath("$.message").value(
                org.hamcrest.Matchers.containsString("More example options are coming soon")));
    }

    @Test
    void an_unmatched_path_should_return_404_listing_what_is_available() throws Exception {
        mockMvc.perform(get("/hrds/does-not-exist"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.api").value("hrds"))
            .andExpect(jsonPath("$.available").isNotEmpty());
    }

    @Test
    void an_unknown_api_code_should_return_404_listing_the_known_codes() throws Exception {
        mockMvc.perform(get("/nope/anything"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.available[0]").value("hrds"));
    }

    @Test
    void listing_apis_should_describe_the_loaded_specs() throws Exception {
        mockMvc.perform(get("/apis"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].code").value("hrds"))
            .andExpect(jsonPath("$[0].operations").isNotEmpty());
    }

    @Test
    void the_swagger_ui_should_not_be_captured_by_the_catch_all() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/html"));
    }

    @Test
    void the_openapi_document_should_not_be_captured_by_the_catch_all() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.openapi").exists());
    }

    @Test
    void the_hello_endpoint_should_still_work_alongside_the_catch_all() throws Exception {
        mockMvc.perform(get("/hello"))
            .andExpect(status().isOk())
            .andExpect(content().string("Hello World"));
    }

    private String pretty(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(json));
        } catch (Exception e) {
            return json;
        }
    }

    @TestConfiguration
    static class ClasspathSpecFetcher {

        @Bean
        @Primary
        SpecFetcher classpathSpecFetcher() {
            return url -> {
                try (var stream = getClass().getResourceAsStream("/specs/hrds-openapi-spec.cached.yml")) {
                    return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            };
        }
    }
}
