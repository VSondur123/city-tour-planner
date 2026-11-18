package fi.citytour.planner.provider.digitraffic;

import fi.citytour.planner.client.ApiClientException;
import fi.citytour.planner.client.ApiHttpClient;
import fi.citytour.planner.client.ApiRequestFactory;
import fi.citytour.planner.client.ApiResponse;
import fi.citytour.planner.config.PlannerProperties;
import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.traffic.GeoShape;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;
import fi.citytour.planner.provider.ProviderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.time.OffsetDateTime;
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
class DigitrafficAdapterTest {

    @Mock
    private ApiHttpClient apiHttpClient;

    @Mock
    private ApiRequestFactory apiRequestFactory;

    @Mock
    private DigitrafficAnnouncementParser announcementParser;

    private PlannerProperties plannerProperties;
    private DigitrafficAdapter adapter;

    @BeforeEach
    void setUp() {
        plannerProperties = new PlannerProperties();
        PlannerProperties.Provider digitrafficProvider = new PlannerProperties.Provider();
        digitrafficProvider.setBaseUrl("https://tie.digitraffic.fi");
        digitrafficProvider.setPath("/api/traffic-message/v2/traffic-announcements");
        plannerProperties.getProviders().setDigitraffic(digitrafficProvider);

        adapter = new DigitrafficAdapter(
                apiHttpClient,
                apiRequestFactory,
                plannerProperties,
                announcementParser
        );
    }

    @Test
    void returnsEmptyListWhenCityNullOrBlank() {
        assertThat(adapter.getAnnouncements(null, Language.EN)).isEmpty();
        assertThat(adapter.getAnnouncements("   ", Language.EN)).isEmpty();

        verify(apiHttpClient, never()).get(any());
    }

    @Test
    void queriesDigitrafficAndDelegatesToParser() {
        URI expectedUri = URI.create("https://tie.digitraffic.fi/api/traffic-message/v2/traffic-announcements");
        when(apiRequestFactory.buildUri(eq(plannerProperties.getProviders().getDigitraffic()), eq(Map.of())))
                .thenReturn(expectedUri);

        String sampleJson = "{\"features\": []}";
        when(apiHttpClient.get(expectedUri)).thenReturn(new ApiResponse(200, "application/json", sampleJson));

        List<TrafficAnnouncement> expectedList = List.of(
                new TrafficAnnouncement(
                        "Tie suljettu",
                        "Itsenäisyydenkatu suljettu",
                        "Tampere",
                        "fi",
                        OffsetDateTime.now(),
                        null,
                        new GeoShape(List.of())
                )
        );

        when(announcementParser.parse(sampleJson, "tampere", Language.FI))
                .thenReturn(expectedList);

        List<TrafficAnnouncement> result = adapter.getAnnouncements("tampere", Language.FI);

        assertThat(result).isSameAs(expectedList);
        verify(apiHttpClient).get(expectedUri);
    }

    @Test
    void throwsProviderExceptionWhenHttpCallFails() {
        URI expectedUri = URI.create("https://tie.digitraffic.fi/api/traffic-message/v2/traffic-announcements");
        when(apiRequestFactory.buildUri(any(), any())).thenReturn(expectedUri);
        when(apiHttpClient.get(expectedUri)).thenThrow(new ApiClientException("HTTP 500"));

        assertThatThrownBy(() -> adapter.getAnnouncements("tampere", Language.EN))
                .isInstanceOf(ProviderException.class)
                .hasMessageContaining("Failed to retrieve announcements")
                .satisfies(e -> assertThat(((ProviderException) e).getSource()).isEqualTo("Digitraffic"));
    }
}
