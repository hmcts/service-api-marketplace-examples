package uk.gov.hmcts.apim.examples;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SpecParser {

    private static final String COMPONENTS_EXAMPLES_PREFIX = "#/components/examples/";

    private final PathPatternParser patternParser = new PathPatternParser();

    public ApiExamples parse(SpecSource source, String content) {
        ParseOptions options = new ParseOptions();
        options.setResolve(true);

        SwaggerParseResult result = new OpenAPIV3Parser().readContents(content, null, options);
        OpenAPI openApi = result.getOpenAPI();
        if (openApi == null) {
            throw new SpecLoadException("Could not parse spec for " + source.code() + ": " + result.getMessages());
        }

        List<SpecOperation> operations = new ArrayList<>();
        if (openApi.getPaths() != null) {
            openApi.getPaths().forEach((template, pathItem) ->
                operations.addAll(operationsFor(openApi, template, pathItem)));
        }

        String name = source.name() != null ? source.name() : titleOf(openApi, source);
        return new ApiExamples(source.code(), name, source.url(), List.copyOf(operations));
    }

    private List<SpecOperation> operationsFor(OpenAPI openApi, String template, PathItem pathItem) {
        List<SpecOperation> operations = new ArrayList<>();
        pathItem.readOperationsMap().forEach((method, operation) -> {
            org.springframework.http.HttpMethod httpMethod =
                org.springframework.http.HttpMethod.valueOf(method.name());
            operations.add(new SpecOperation(
                httpMethod,
                template,
                patternParser.parse(template),
                examplesFor(openApi, operation)));
        });
        return operations;
    }

    private List<ExampleResponse> examplesFor(OpenAPI openApi, Operation operation) {
        List<ExampleResponse> examples = new ArrayList<>();
        if (operation.getResponses() == null) {
            return examples;
        }
        operation.getResponses().forEach((status, response) -> {
            Integer code = statusOf(status);
            if (code != null) {
                examples.addAll(examplesFor(openApi, code, response));
            }
        });
        return List.copyOf(examples);
    }

    private List<ExampleResponse> examplesFor(OpenAPI openApi, int status, ApiResponse response) {
        List<ExampleResponse> examples = new ArrayList<>();
        if (response.getContent() == null) {
            return examples;
        }
        response.getContent().forEach((mediaType, media) ->
            examples.addAll(examplesFor(openApi, status, mediaType, media)));
        return examples;
    }

    private List<ExampleResponse> examplesFor(OpenAPI openApi, int status, String mediaType, MediaType media) {
        List<ExampleResponse> examples = new ArrayList<>();
        Map<String, Example> declared = media.getExamples();
        if (declared != null) {
            declared.forEach((name, example) -> {
                Object value = valueOf(openApi, example);
                if (value != null) {
                    examples.add(new ExampleResponse(status, mediaType, name, value));
                }
            });
        }
        if (examples.isEmpty() && media.getExample() != null) {
            examples.add(new ExampleResponse(status, mediaType, "default", media.getExample()));
        }
        return examples;
    }

    private Object valueOf(OpenAPI openApi, Example example) {
        if (example == null) {
            return null;
        }
        if (example.getValue() != null) {
            return example.getValue();
        }
        String ref = example.get$ref();
        if (ref != null && ref.startsWith(COMPONENTS_EXAMPLES_PREFIX) && openApi.getComponents() != null
            && openApi.getComponents().getExamples() != null) {
            Example resolved = openApi.getComponents().getExamples()
                .get(ref.substring(COMPONENTS_EXAMPLES_PREFIX.length()));
            return resolved != null ? resolved.getValue() : null;
        }
        return null;
    }

    private Integer statusOf(String status) {
        try {
            return Integer.valueOf(status);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String titleOf(OpenAPI openApi, SpecSource source) {
        if (openApi.getInfo() != null && openApi.getInfo().getTitle() != null) {
            return openApi.getInfo().getTitle();
        }
        return source.code();
    }
}
