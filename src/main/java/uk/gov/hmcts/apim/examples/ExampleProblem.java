package uk.gov.hmcts.apim.examples;

import java.util.List;

public record ExampleProblem(String api, String request, String message, List<String> available) {
}
