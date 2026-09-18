package uk.gov.hmcts.apim.examples;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class HttpSpecFetcher implements SpecFetcher {

    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();

    @Override
    public String fetch(String url) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(TIMEOUT).GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new SpecLoadException("Spec at " + url + " returned HTTP " + response.statusCode());
            }
            return response.body();
        } catch (IOException e) {
            throw new SpecLoadException("Could not fetch spec at " + url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SpecLoadException("Interrupted fetching spec at " + url, e);
        }
    }
}
