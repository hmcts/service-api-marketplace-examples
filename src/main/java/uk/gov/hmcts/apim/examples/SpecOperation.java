package uk.gov.hmcts.apim.examples;

import org.springframework.http.HttpMethod;
import org.springframework.web.util.pattern.PathPattern;

import java.util.List;
import java.util.Optional;

public record SpecOperation(HttpMethod method, String pathTemplate, PathPattern pattern,
                            List<ExampleResponse> examples) {

    public boolean hasExamples() {
        return !examples.isEmpty();
    }

    public Optional<ExampleResponse> select(Integer status, String name) {
        return examples.stream()
            .filter(example -> status == null || example.status() == status)
            .filter(example -> name == null || name.equals(example.name()))
            .min((left, right) -> {
                int byPreference = Boolean.compare(!isSuccess(left), !isSuccess(right));
                return byPreference != 0 ? byPreference : Integer.compare(left.status(), right.status());
            });
    }

    public String describe() {
        return method.name() + " " + pathTemplate;
    }

    private static boolean isSuccess(ExampleResponse example) {
        return example.status() >= 200 && example.status() < 300;
    }
}
