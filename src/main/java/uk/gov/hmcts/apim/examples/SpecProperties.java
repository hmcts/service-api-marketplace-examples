package uk.gov.hmcts.apim.examples;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "marketplace")
public class SpecProperties {

    private List<SpecSource> specs = List.of();

    public List<SpecSource> getSpecs() {
        return specs;
    }

    public void setSpecs(List<SpecSource> specs) {
        this.specs = specs;
    }
}
