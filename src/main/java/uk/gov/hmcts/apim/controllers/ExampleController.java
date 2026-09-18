package uk.gov.hmcts.apim.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.apim.examples.ApiExamples;
import uk.gov.hmcts.apim.examples.ExampleCatalogue;
import uk.gov.hmcts.apim.examples.ExampleProblem;
import uk.gov.hmcts.apim.examples.ExampleResponse;
import uk.gov.hmcts.apim.examples.SpecOperation;

import java.util.List;
import java.util.Optional;

@RestController
public class ExampleController {

    /**
     * Short codes are lowercase words. The lookahead keeps the catch-all off springdoc's and the
     * servlet container's own paths, which otherwise lose to an annotated controller.
     */
    private static final String CODE = "{code:(?!swagger-ui|v3|webjars|actuator|error|apis)[a-z][a-z0-9-]{1,20}}";

    private final ExampleCatalogue catalogue;

    public ExampleController(ExampleCatalogue catalogue) {
        this.catalogue = catalogue;
    }

    @Operation(summary = "Serve a spec example",
        description = "Returns the example declared in the API's published OpenAPI spec for the matching operation")
    @RequestMapping("/" + CODE + "/**")
    public ResponseEntity<Object> example(@PathVariable String code,
                                          @RequestParam(required = false) Integer status,
                                          @RequestParam(name = "example", required = false) String exampleName,
                                          HttpServletRequest request) {

        Optional<ApiExamples> api = catalogue.find(code);
        if (api.isEmpty()) {
            return unknownApi(code);
        }

        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        String path = pathWithin(code, request);
        Optional<SpecOperation> operation = api.get().match(method, path);
        if (operation.isEmpty()) {
            return noSuchOperation(api.get(), method, path);
        }

        Optional<ExampleResponse> example = operation.get().select(status, exampleName);
        if (example.isEmpty()) {
            return noExample(api.get(), operation.get());
        }

        ExampleResponse selected = example.get();
        return ResponseEntity.status(selected.status())
            .contentType(MediaType.parseMediaType(selected.mediaType()))
            .body(selected.value());
    }

    private ResponseEntity<Object> unknownApi(String code) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExampleProblem(
            code,
            null,
            "No API is registered under '" + code + "'. More APIs are being added soon.",
            catalogue.codes()));
    }

    private ResponseEntity<Object> noSuchOperation(ApiExamples api, HttpMethod method, String path) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExampleProblem(
            api.code(),
            method.name() + " " + path,
            "The " + api.code() + " spec declares no operation matching this request.",
            api.operationsWithExamples()));
    }

    private ResponseEntity<Object> noExample(ApiExamples api, SpecOperation operation) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExampleProblem(
            api.code(),
            operation.describe(),
            "The " + api.code() + " spec declares no response example for this operation yet. "
                + "More example options are coming soon.",
            List.of()));
    }

    private String pathWithin(String code, HttpServletRequest request) {
        String uri = request.getRequestURI().substring(request.getContextPath().length());
        String remainder = uri.substring(("/" + code).length());
        return remainder.isEmpty() ? "/" : remainder;
    }
}
