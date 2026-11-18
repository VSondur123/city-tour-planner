package fi.citytour.planner.controller;

import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.traffic.GeoShape;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;
import fi.citytour.planner.model.weather.HourlyForecast;
import fi.citytour.planner.provider.TrafficProvider;
import fi.citytour.planner.provider.WeatherProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdapterTestControllerTest {

    @Mock
    private WeatherProvider weatherProvider;

    @Mock
    private TrafficProvider trafficProvider;

    private AdapterTestController controller;

    @BeforeEach
    void setUp() {
        controller = new AdapterTestController(weatherProvider, trafficProvider);
    }

    @Test
    void testWeatherEndpointDelegatesToWeatherProvider() {
        LocalDate date = LocalDate.of(2026, 11, 17);
        List<HourlyForecast> mockForecast = List.of(
                new HourlyForecast(OffsetDateTime.now(), 5.0, 0.0, 80.0, 3.5)
        );
        when(weatherProvider.getForecast(eq("tampere"), eq(date))).thenReturn(mockForecast);

        List<HourlyForecast> result = controller.testWeather("tampere", "2026-11-17");

        assertThat(result).isSameAs(mockForecast);
        verify(weatherProvider).getForecast("tampere", date);
    }

    @Test
    void testTrafficEndpointDelegatesToTrafficProvider() {
        List<TrafficAnnouncement> mockAnnouncements = List.of(
                new TrafficAnnouncement("Road work", "Maintenance", "Tampere", "en", null, null, new GeoShape(List.of()))
        );
        when(trafficProvider.getAnnouncements(eq("tampere"), eq(Language.EN))).thenReturn(mockAnnouncements);

        List<TrafficAnnouncement> result = controller.testTraffic("tampere", "en");

        assertThat(result).isSameAs(mockAnnouncements);
        verify(trafficProvider).getAnnouncements("tampere", Language.EN);
    }

    @Test
    void testOverviewEndpointReturnsCombinedData() {
        LocalDate date = LocalDate.of(2026, 11, 17);
        List<HourlyForecast> mockForecast = List.of(
                new HourlyForecast(OffsetDateTime.now(), 5.0, 0.0, 80.0, 3.5)
        );
        List<TrafficAnnouncement> mockAnnouncements = List.of(
                new TrafficAnnouncement("Road work", "Maintenance", "Tampere", "fi", null, null, new GeoShape(List.of()))
        );

        when(weatherProvider.getForecast(eq("tampere"), eq(date))).thenReturn(mockForecast);
        when(trafficProvider.getAnnouncements(eq("tampere"), eq(Language.FI))).thenReturn(mockAnnouncements);

        AdapterTestController.AdapterTestOverview overview = controller.testOverview("tampere", "2026-11-17", "fi");

        assertThat(overview.city()).isEqualTo("tampere");
        assertThat(overview.weatherCount()).isEqualTo(1);
        assertThat(overview.trafficCount()).isEqualTo(1);
        assertThat(overview.weather()).isSameAs(mockForecast);
        assertThat(overview.traffic()).isSameAs(mockAnnouncements);
    }
}
