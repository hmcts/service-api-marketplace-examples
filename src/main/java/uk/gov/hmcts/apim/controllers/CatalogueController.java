package uk.gov.hmcts.apim.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.apim.examples.ExampleCatalogue;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class CatalogueController {

    private final ExampleCatalogue catalogue;

    public CatalogueController(ExampleCatalogue catalogue) {
        this.catalogue = catalogue;
    }

    @Operation(summary = "List the APIs whose examples are served here")
    @GetMapping("/apis")
    public ResponseEntity<List<ApiSummary>> apis() {
        return ok(catalogue.all().stream()
            .map(api -> new ApiSummary(api.code(), api.name(), api.url(), api.operationsWithExamples()))
            .toList());
    }

    public record ApiSummary(String code, String name, String specUrl, List<String> operations) {
    }
}
