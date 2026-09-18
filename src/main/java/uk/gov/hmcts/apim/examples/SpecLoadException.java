package uk.gov.hmcts.apim.examples;

public class SpecLoadException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SpecLoadException(String message) {
        super(message);
    }

    public SpecLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
