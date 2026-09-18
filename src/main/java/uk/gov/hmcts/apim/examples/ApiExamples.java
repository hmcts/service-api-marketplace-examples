package uk.gov.hmcts.apim.examples;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record ApiExamples(String code, String name, String url, List<SpecOperation> operations) {

    public Optional<SpecOperation> match(HttpMethod method, String path) {
        PathContainer requested = PathContainer.parsePath(path);
        return operations.stream()
            .filter(operation -> operation.method().equals(method))
            .filter(operation -> operation.pattern().matches(requested))
            .min(Comparator.comparing(SpecOperation::pattern, PathPattern.SPECIFICITY_COMPARATOR));
    }

    public List<String> operationsWithExamples() {
        return operations.stream().filter(SpecOperation::hasExamples).map(SpecOperation::describe).toList();
    }
}
