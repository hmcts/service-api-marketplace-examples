package uk.gov.hmcts.apim.controllers;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class HelloWorldFunctionalTest {

    @Value("${TEST_URL:http://localhost:8081}")
    private String testUrl;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = testUrl;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    void calling_hello_should_return_hello_world() {
        Response response = given()
            .when()
            .get("/hello")
            .then()
            .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.asString()).isEqualTo("Hello World");
    }

    @Test
    void calling_root_should_return_welcome_message() {
        Response response = given()
            .when()
            .get("/")
            .then()
            .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.asString()).startsWith("Welcome");
    }
}
