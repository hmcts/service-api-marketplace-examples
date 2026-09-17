package uk.gov.hmcts.apim.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class HelloWorldController {

    /**
     * Azure app service pings the root endpoint when "Always On" is enabled,
     * so this exists to keep those requests out of the 404 logs.
     */
    @GetMapping("/")
    public ResponseEntity<String> welcome() {
        return ok("Welcome to apim-marketplace-examples");
    }

    @Operation(summary = "Hello world", description = "Placeholder endpoint until spec-backed examples are served")
    @ApiResponse(responseCode = "200", description = "Greeting returned")
    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ok("Hello World");
    }
}
