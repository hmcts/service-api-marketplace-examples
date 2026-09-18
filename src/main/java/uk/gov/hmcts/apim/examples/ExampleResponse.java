package uk.gov.hmcts.apim.examples;

public record ExampleResponse(int status, String mediaType, String name, Object value) {
}
