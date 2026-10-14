
package fi.citytour.research.client;

import fi.citytour.research.config.ResearchProperties;
import fi.citytour.research.model.ApiResponse;
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
 * HTTP client responsible for sending requests to external APIs and
 * converting their responses into readable text.
 *
 * <p>The client uses timeout settings from {@link ResearchProperties},
 * follows HTTP redirects, and supports gzip-compressed response bodies.</p>
 *
 * <p>It returns an {@link ApiResponse} containing the HTTP status code,
 * content type, and decoded response body.</p>
 */
@Component
public class ApiHttpClient {

    private final ResearchProperties researchProperties;
    private final HttpClient httpClient;

    /**
     * Creates an HTTP client using the configured connection timeout.
     *
     * @param researchProperties application configuration containing HTTP
     *                           timeout settings
     */
    public ApiHttpClient(ResearchProperties researchProperties) {
        this.researchProperties = researchProperties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(
                        researchProperties.getHttp().getConnectTimeoutSeconds()
                ))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Sends an HTTP request to the specified URI and returns its response.
     *
     * <p>The request uses the configured request timeout and advertises
     * support for gzip compression. If the response is gzip-compressed,
     * its body is decompressed before being converted to a UTF-8 string.</p>
     *
     * @param uri the complete URI of the API endpoint, including query parameters
     * @param method the HTTP method to use, such as GET or POST
     * @return an {@link ApiResponse} containing the HTTP status code,
     *         response content type, and decoded response body
     * @throws IOException if an I/O error occurs while sending the request
     *                     or reading a compressed response
     * @throws InterruptedException if the request is interrupted
     */
    public ApiResponse send(URI uri, String method)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(
                        researchProperties.getHttp().getRequestTimeoutSeconds()
                ))
                .header("Accept", "*/*")
                .header("Accept-Encoding", "gzip")
                .method(method.toUpperCase(), HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<byte[]> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofByteArray()
        );

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

        String responseBody = new String(body, StandardCharsets.UTF_8);

        String contentType = response.headers()
                .firstValue("Content-Type")
                .orElse("Unknown");

        return new ApiResponse(
                response.statusCode(),
                contentType,
                responseBody
        );
    }
}
