package uk.gov.hmcts.apim.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class HelloWorldControllerTest {

    private final HelloWorldController controller = new HelloWorldController();

    @Test
    void calling_hello_should_return_hello_world() {
        ResponseEntity<String> response = controller.hello();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Hello World");
    }

    @Test
    void calling_root_should_return_welcome_message() {
        ResponseEntity<String> response = controller.welcome();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).startsWith("Welcome");
    }
}
