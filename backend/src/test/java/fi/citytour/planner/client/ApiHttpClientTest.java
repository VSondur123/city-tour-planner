package fi.citytour.planner.client;

import com.sun.net.httpserver.HttpServer;
import fi.citytour.planner.config.PlannerProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiHttpClientTest {

    private HttpServer server;
    private ApiHttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);

        server.createContext("/ok", exchange -> {
            byte[] body = "hello äö".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        server.createContext("/gzip", exchange -> {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(buffer)) {
                gzip.write("zipped".getBytes(StandardCharsets.UTF_8));
            }
            byte[] body = buffer.toByteArray();
            exchange.getResponseHeaders().add("Content-Encoding", "gzip");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        server.createContext("/bad", exchange -> {
            exchange.sendResponseHeaders(400, -1);
            exchange.close();
        });

        server.start();
        client = new ApiHttpClient(new PlannerProperties());
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    private URI uri(String path) {
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + path);
    }

    @Test
    void returnsTheDecodedBodyStatusAndContentType() {
        ApiResponse response = client.get(uri("/ok"));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.contentType()).isEqualTo("text/plain");
        assertThat(response.body()).isEqualTo("hello äö");
    }

    @Test
    void unpacksGzipBodies() {
        assertThat(client.get(uri("/gzip")).body()).isEqualTo("zipped");
    }

    @Test
    void treatsANon2xxStatusAsAnError() {
        assertThatThrownBy(() -> client.get(uri("/bad")))
                .isInstanceOf(ApiClientException.class)
                .hasMessageContaining("HTTP 400");
    }

    @Test
    void wrapsConnectionFailures() {
        server.stop(0);

        assertThatThrownBy(() -> client.get(uri("/ok")))
                .isInstanceOf(ApiClientException.class);
    }
}
