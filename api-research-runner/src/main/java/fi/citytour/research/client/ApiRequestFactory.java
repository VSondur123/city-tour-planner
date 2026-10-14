
package fi.citytour.research.client;

import fi.citytour.research.config.ResearchProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

/**
 * Builds request URIs for external API endpoints using the application
 * configuration.
 *
 * <p>The factory combines an API source's base URL with the configured
 * endpoint path and appends its query parameters. This keeps endpoint
 * details in the YAML configuration instead of hardcoding URLs in Java.</p>
 */
@Component
public class ApiRequestFactory {

    /**
     * Builds a complete URI from the source and endpoint configurations.
     *
     * <p>Query parameters are added individually, and the resulting URI
     * is encoded before being returned.</p>
     *
     * @param source the API source configuration containing the base URL
     * @param endpoint the endpoint configuration containing the path and
     *                 query parameters
     * @return the constructed and encoded request URI
     */
    public URI buildUri(
            ResearchProperties.Source source,
            ResearchProperties.Endpoint endpoint) {

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(source.getBaseUrl())
                .path(endpoint.getPath());

        for (Map.Entry<String, String> parameter
                : endpoint.getParameters().entrySet()) {
            builder.queryParam(parameter.getKey(), parameter.getValue());
        }

        return builder.build()
                .encode()
                .toUri();
    }
}
