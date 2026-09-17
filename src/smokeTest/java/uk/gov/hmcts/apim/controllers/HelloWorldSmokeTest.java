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
class HelloWorldSmokeTest {

    @Value("${TEST_URL:http://localhost:8081}")
    private String testUrl;

    @BeforeEach
    void setUp() {
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    void calling_hello_on_a_deployed_instance_should_return_hello_world() {
        Response response = given()
            .baseUri(testUrl)
            .when()
            .get("/hello")
            .then()
            .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.asString()).isEqualTo("Hello World");
    }
}
