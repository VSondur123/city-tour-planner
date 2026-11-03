package fi.citytour.planner.client;

import fi.citytour.planner.config.PlannerProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds request URIs from the provider configuration plus the parameters that are only known
 * at request time, such as the city or the date.
 */
@Component
public class ApiRequestFactory {

    /**
     * Builds the encoded URI. A request parameter replaces a configured parameter with the
     * same name.
     */
    public URI buildUri(
            PlannerProperties.Provider provider,
            Map<String, String> requestParameters) {

        Map<String, String> parameters =
                new LinkedHashMap<>(provider.getParameters());
        parameters.putAll(requestParameters);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(provider.getBaseUrl())
                .path(provider.getPath());

        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            builder.queryParam(parameter.getKey(), parameter.getValue());
        }

        return builder.build()
                .encode()
                .toUri();
    }
}
