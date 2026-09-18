package uk.gov.hmcts.apim.examples;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpSpecFetcherTest {

    private static final String SPEC = "openapi: 3.1.0\ninfo:\n  title: Test\n";

    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void fetching_a_spec_that_is_served_should_return_its_content() {
        respondWith("/spec.yml", 200, SPEC);

        assertThat(new HttpSpecFetcher().fetch(baseUrl + "/spec.yml")).isEqualTo(SPEC);
    }

    @Test
    void fetching_a_spec_that_is_missing_should_report_the_status_code() {
        respondWith("/gone.yml", 404, "not here");

        assertThatThrownBy(() -> new HttpSpecFetcher().fetch(baseUrl + "/gone.yml"))
            .isInstanceOf(SpecLoadException.class)
            .hasMessageContaining("404")
            .hasMessageContaining("/gone.yml");
    }

    @Test
    void fetching_from_a_host_that_is_not_listening_should_report_the_url() {
        server.stop(0);

        assertThatThrownBy(() -> new HttpSpecFetcher().fetch(baseUrl + "/spec.yml"))
            .isInstanceOf(SpecLoadException.class)
            .hasMessageContaining("Could not fetch spec");
    }

    private void respondWith(String path, int status, String body) {
        server.createContext(path, exchange -> {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
    }
}
