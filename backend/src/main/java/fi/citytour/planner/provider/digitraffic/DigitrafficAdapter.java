package fi.citytour.planner.provider.digitraffic;

import fi.citytour.planner.client.ApiClientException;
import fi.citytour.planner.client.ApiHttpClient;
import fi.citytour.planner.client.ApiRequestFactory;
import fi.citytour.planner.client.ApiResponse;
import fi.citytour.planner.config.PlannerProperties;
import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;
import fi.citytour.planner.provider.ProviderException;
import fi.citytour.planner.provider.TrafficProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Adapts the Digitraffic road traffic announcements REST API to the domain {@link TrafficProvider} interface.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DigitrafficAdapter implements TrafficProvider {

    private final ApiHttpClient apiHttpClient;
    private final ApiRequestFactory apiRequestFactory;
    private final PlannerProperties plannerProperties;
    private final DigitrafficAnnouncementParser digitrafficAnnouncementParser;

    @Override
    public List<TrafficAnnouncement> getAnnouncements(String city, Language preferredLanguage) {
        return getAnnouncements(city, preferredLanguage, null, null);
    }

    @Override
    public List<TrafficAnnouncement> getAnnouncements(
            String city,
            Language preferredLanguage,
            OffsetDateTime windowStart,
            OffsetDateTime windowEnd) {

        if (city == null || city.isBlank()) {
            return List.of();
        }

        URI uri = apiRequestFactory.buildUri(
                plannerProperties.getProviders().getDigitraffic(),
                Map.of()
        );

        log.debug("Fetching Digitraffic announcements from {}", uri);

        ApiResponse response;
        try {
            response = apiHttpClient.get(uri);
        } catch (ApiClientException exception) {
            throw new ProviderException("Digitraffic", "Failed to retrieve announcements: " + exception.getMessage(), exception);
        }

        return digitrafficAnnouncementParser.parse(response.body(), city, preferredLanguage, windowStart, windowEnd);
    }
}
