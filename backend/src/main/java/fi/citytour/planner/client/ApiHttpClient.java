package fi.citytour.planner.client;

import fi.citytour.planner.config.PlannerProperties;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.zip.GZIPInputStream;

/**
 * Sends GET requests to external APIs with the configured timeouts, following redirects and
 * unpacking gzip. A response outside 200-299 is an error, so an error page is never parsed as
 * data.
 */
@Component
public class ApiHttpClient {

    private final PlannerProperties plannerProperties;
    private final HttpClient httpClient;

    public ApiHttpClient(PlannerProperties plannerProperties) {
        this.plannerProperties = plannerProperties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(
                        plannerProperties.getHttp().getConnectTimeoutSeconds()
                ))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /** Sends a GET request and returns the decoded response. */
    public ApiResponse get(URI uri) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(
                        plannerProperties.getHttp().getRequestTimeoutSeconds()
                ))
                .header("Accept", "*/*")
                .header("Accept-Encoding", "gzip")
                .GET()
                .build();

        try {
            HttpResponse<byte[]> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofByteArray()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiClientException(
                        "HTTP " + response.statusCode() + " from " + uri);
            }

            String contentType = response.headers()
                    .firstValue("Content-Type")
                    .orElse("Unknown");

            return new ApiResponse(
                    response.statusCode(),
                    contentType,
                    decode(response)
            );

        } catch (IOException exception) {
            throw new ApiClientException(
                    "Request failed for " + uri, exception);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiClientException(
                    "Request interrupted for " + uri, exception);
        }
    }

    private String decode(HttpResponse<byte[]> response) throws IOException {

        byte[] body = response.body();

        String encoding = response.headers()
                .firstValue("Content-Encoding")
                .orElse("");

        if (encoding.toLowerCase().contains("gzip")) {
            try (GZIPInputStream gzip = new GZIPInputStream(
                    new ByteArrayInputStream(body))) {
                body = gzip.readAllBytes();
            }
        }

        return new String(body, StandardCharsets.UTF_8);
    }
}
