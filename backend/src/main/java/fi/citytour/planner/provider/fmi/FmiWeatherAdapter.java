package fi.citytour.planner.provider.fmi;

import fi.citytour.planner.client.ApiClientException;
import fi.citytour.planner.client.ApiHttpClient;
import fi.citytour.planner.client.ApiRequestFactory;
import fi.citytour.planner.client.ApiResponse;
import fi.citytour.planner.config.PlannerProperties;
import fi.citytour.planner.model.weather.HourlyForecast;
import fi.citytour.planner.provider.ProviderException;
import fi.citytour.planner.provider.WeatherProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapts the FMI (Finnish Meteorological Institute) weather forecast WFS service
 * to the domain {@link WeatherProvider} interface.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FmiWeatherAdapter implements WeatherProvider {

    private final ApiHttpClient apiHttpClient;
    private final ApiRequestFactory apiRequestFactory;
    private final PlannerProperties plannerProperties;
    private final FmiForecastParser fmiForecastParser;

    @Override
    public List<HourlyForecast> getForecast(String city, LocalDate date) {
        if (city == null || city.isBlank() || date == null) {
            return List.of();
        }

        ZoneId zone = ZoneId.of(plannerProperties.getZone());
        ZonedDateTime localStart = date.atStartOfDay(zone);
        ZonedDateTime localEnd = date.plusDays(1).atStartOfDay(zone);

        String startUtc = DateTimeFormatter.ISO_INSTANT.format(
                localStart.toInstant().truncatedTo(java.time.temporal.ChronoUnit.SECONDS)
        );
        String endUtc = DateTimeFormatter.ISO_INSTANT.format(
                localEnd.toInstant().truncatedTo(java.time.temporal.ChronoUnit.SECONDS)
        );

        Map<String, String> requestParameters = new LinkedHashMap<>();
        requestParameters.put("place", city);
        requestParameters.put("starttime", startUtc);
        requestParameters.put("endtime", endUtc);

        URI uri = apiRequestFactory.buildUri(
                plannerProperties.getProviders().getFmi(),
                requestParameters
        );

        log.debug("Fetching FMI weather forecast from {}", uri);

        ApiResponse response;
        try {
            response = apiHttpClient.get(uri);
        } catch (ApiClientException exception) {
            throw new ProviderException("FMI", "Failed to retrieve forecast: " + exception.getMessage(), exception);
        }

        return fmiForecastParser.parse(response.body(), date, zone);
    }
}
