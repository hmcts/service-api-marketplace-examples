package uk.gov.hmcts.apim.controllers;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs against a deployed instance, which loads each spec from its published location. These tests
 * therefore read the live spec rather than the cached copy under src/test/resources/specs, and are
 * what catches an upstream spec changing out from under us.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class SpecExamplesFunctionalTest {

    @Value("${TEST_URL:http://localhost:8081}")
    private String testUrl;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = testUrl;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    void listing_apis_should_report_hrds_with_operations_loaded_from_its_live_spec() {
        Response response = given().when().get("/apis").then().extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("code")).contains("hrds");
        assertThat(response.jsonPath().getList("find { it.code == 'hrds' }.operations"))
            .as("hrds spec loaded but yielded no operations with examples")
            .isNotEmpty();
    }

    @Test
    void getting_hrds_event_types_should_return_the_example_from_the_live_spec() {
        Response response = given().when().get("/hrds/event-types").then().extract().response();

        assertThat(response.statusCode())
            .as("the live hrds spec no longer serves an example for GET /event-types")
            .isEqualTo(200);
        assertThat(response.jsonPath().getList("events")).isNotEmpty();
    }
}
