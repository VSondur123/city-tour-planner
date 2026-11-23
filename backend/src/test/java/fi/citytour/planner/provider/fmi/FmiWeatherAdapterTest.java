package fi.citytour.planner.provider.fmi;

import fi.citytour.planner.client.ApiClientException;
import fi.citytour.planner.client.ApiHttpClient;
import fi.citytour.planner.client.ApiRequestFactory;
import fi.citytour.planner.client.ApiResponse;
import fi.citytour.planner.config.PlannerProperties;
import fi.citytour.planner.model.weather.HourlyForecast;
import fi.citytour.planner.provider.ProviderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FmiWeatherAdapterTest {

    @Mock
    private ApiHttpClient apiHttpClient;

    @Mock
    private ApiRequestFactory apiRequestFactory;

    @Mock
    private FmiForecastParser fmiForecastParser;

    private PlannerProperties plannerProperties;
    private FmiWeatherAdapter adapter;

    private final LocalDate testDate = LocalDate.of(2026, 11, 16);

    @BeforeEach
    void setUp() {
        plannerProperties = new PlannerProperties();
        plannerProperties.setZone("Europe/Helsinki");
        PlannerProperties.Provider fmiProvider = new PlannerProperties.Provider();
        fmiProvider.setBaseUrl("https://opendata.fmi.fi");
        fmiProvider.setPath("/wfs");
        plannerProperties.getProviders().setFmi(fmiProvider);

        adapter = new FmiWeatherAdapter(
                apiHttpClient,
                apiRequestFactory,
                plannerProperties,
                fmiForecastParser
        );
    }

    @Test
    void returnsEmptyListWhenCityOrDateNull() {
        assertThat(adapter.getForecast(null, testDate)).isEmpty();
        assertThat(adapter.getForecast("   ", testDate)).isEmpty();
        assertThat(adapter.getForecast("tampere", null)).isEmpty();

        verify(apiHttpClient, never()).get(any());
    }

    @Test
    void queriesFmiAndDelegatesToParser() {
        URI expectedUri = URI.create("https://opendata.fmi.fi/wfs?place=tampere");
        when(apiRequestFactory.buildUri(any(), any())).thenReturn(expectedUri);

        String sampleXml = "<sample>response</sample>";
        when(apiHttpClient.get(expectedUri)).thenReturn(new ApiResponse(200, "text/xml", sampleXml));

        List<HourlyForecast> expectedForecasts = List.of(
                new HourlyForecast(
                        OffsetDateTime.parse("2026-11-16T12:00:00+02:00"),
                        5.0, 0.1, 50.0, 4.0
                )
        );
        when(fmiForecastParser.parse(eq(sampleXml), eq(testDate), any(ZoneId.class)))
                .thenReturn(expectedForecasts);

        List<HourlyForecast> result = adapter.getForecast("tampere", testDate);

        assertThat(result).isSameAs(expectedForecasts);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(apiRequestFactory).buildUri(eq(plannerProperties.getProviders().getFmi()), paramsCaptor.capture());

        Map<String, String> params = paramsCaptor.getValue();
        assertThat(params)
                .containsEntry("place", "tampere")
                .containsKey("starttime")
                .containsKey("endtime");
        assertThat(params.get("starttime")).endsWith("Z");
        assertThat(params.get("endtime")).endsWith("Z");
    }

    @Test
    void throwsProviderExceptionWhenHttpCallFails() {
        URI expectedUri = URI.create("https://opendata.fmi.fi/wfs?place=tampere");
        when(apiRequestFactory.buildUri(any(), any())).thenReturn(expectedUri);
        when(apiHttpClient.get(expectedUri)).thenThrow(new ApiClientException("HTTP 500 error"));

        assertThatThrownBy(() -> adapter.getForecast("tampere", testDate))
                .isInstanceOf(ProviderException.class)
                .hasMessageContaining("Failed to retrieve forecast")
                .satisfies(e -> assertThat(((ProviderException) e).getSource()).isEqualTo("FMI"));
    }
}
