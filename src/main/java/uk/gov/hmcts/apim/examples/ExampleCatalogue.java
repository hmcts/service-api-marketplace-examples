package uk.gov.hmcts.apim.examples;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ExampleCatalogue {

    private final SpecProperties properties;
    private final SpecFetcher fetcher;
    private final SpecParser parser;
    private final Map<String, ApiExamples> loaded = new ConcurrentHashMap<>();

    public ExampleCatalogue(SpecProperties properties, SpecFetcher fetcher, SpecParser parser) {
        this.properties = properties;
        this.fetcher = fetcher;
        this.parser = parser;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void load() {
        for (SpecSource source : properties.getSpecs()) {
            loadSource(source);
        }
    }

    public Optional<ApiExamples> find(String code) {
        return Optional.ofNullable(loaded.get(code));
    }

    public List<ApiExamples> all() {
        return properties.getSpecs().stream()
            .map(source -> loaded.get(source.code()))
            .filter(java.util.Objects::nonNull)
            .toList();
    }

    public List<String> codes() {
        return properties.getSpecs().stream().map(SpecSource::code).toList();
    }

    private void loadSource(SpecSource source) {
        try {
            ApiExamples examples = parser.parse(source, fetcher.fetch(source.url()));
            loaded.put(source.code(), examples);
            log.info("Loaded {} operations for {} from {}", examples.operations().size(), source.code(), source.url());
        } catch (RuntimeException e) {
            log.error("Could not load spec for {} from {}, it will return 503 until reloaded",
                source.code(), source.url(), e);
        }
    }
}
