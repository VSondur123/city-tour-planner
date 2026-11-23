package fi.citytour.planner.service;

import fi.citytour.planner.config.PlannerProperties;
import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.common.LocalizedText;
import fi.citytour.planner.model.place.City;
import fi.citytour.planner.model.place.Place;
import fi.citytour.planner.model.place.PlaceCategory;
import fi.citytour.planner.model.place.PlaceView;
import fi.citytour.planner.model.tour.DataStatus;
import fi.citytour.planner.model.tour.Factor;
import fi.citytour.planner.model.tour.Stop;
import fi.citytour.planner.model.tour.TourRequest;
import fi.citytour.planner.model.tour.TourResult;
import fi.citytour.planner.model.traffic.GeoShape;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;
import fi.citytour.planner.model.weather.HourlyForecast;
import fi.citytour.planner.planning.TourStrategy;
import fi.citytour.planner.planning.TourStrategyFactory;
import fi.citytour.planner.provider.ProviderException;
import fi.citytour.planner.provider.TrafficProvider;
import fi.citytour.planner.provider.WeatherProvider;
import fi.citytour.planner.repository.PlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TourServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Helsinki");
    /** Tomorrow, so the request is never in the past whatever the time zone of the machine. */
    private static final LocalDate DATE = LocalDate.now(ZONE).plusDays(1);

    private static final Place PLACE = new Place(
            "p1",
            new LocalizedText("Cathedral", "Tuomiokirkko"),
            "tampere",
            PlaceCategory.LANDMARK,
            true,
            61.5,
            23.77,
            30,
            new LocalizedText("https://example.org/en", "https://example.org/fi"));

    private static final HourlyForecast FORECAST = new HourlyForecast(
            OffsetDateTime.parse("2026-11-21T10:00:00+02:00"), 2.0, 0.0, 50.0, 3.0);

    private static final TrafficAnnouncement ANNOUNCEMENT = new TrafficAnnouncement(
            "Tie on suljettu liikenteeltä", "Rautatienkatu", "Tampere", "fi",
            OffsetDateTime.parse("2026-11-21T08:00:00Z"), null, new GeoShape(List.of()));

    @Mock
    private WeatherProvider weatherProvider;
    @Mock
    private TrafficProvider trafficProvider;
    @Mock
    private PlaceRepository placeRepository;
    @Mock
    private TourStrategyFactory strategyFactory;
    @Mock
    private TourStrategy strategy;

    private TourService service;

    @BeforeEach
    void setUp() {
        service = new TourService(weatherProvider, trafficProvider, placeRepository, strategyFactory, new PlannerProperties());
    }

    private TourRequest request(String city, Language language, Set<Factor> factors) {
        return new TourRequest(city, DATE, LocalTime.of(10, 0), 4, language, factors);
    }

    /** Wires the repository and strategy so that a tour for Tampere can be planned. */
    private void givenTampereWithOneStop() {
        when(placeRepository.findCities()).thenReturn(List.of(new City("tampere", "Tampere")));
        when(placeRepository.findByCity("tampere")).thenReturn(List.of(PLACE));
        when(strategyFactory.choose(any(), any())).thenReturn(strategy);
        when(strategy.plan(any(), any(), any(), any())).thenReturn(List.of(new Stop(
                PLACE,
                OffsetDateTime.parse("2026-11-21T10:00:00+02:00"),
                OffsetDateTime.parse("2026-11-21T10:30:00+02:00"))));
        when(strategy.explanation(any())).thenReturn("because");
    }

    @Test
    void rejectsADateBeforeTodayWithoutCallingProviders() {
        TourRequest yesterday = new TourRequest(
                "tampere", LocalDate.now(ZONE).minusDays(1), LocalTime.of(10, 0), 4, null, null);

        assertThatThrownBy(() -> service.generateTour(yesterday))
                .isInstanceOf(PastDateException.class);

        verifyNoInteractions(weatherProvider, trafficProvider);
    }

    @Test
    void acceptsToday() {
        givenTampereWithOneStop();
        when(weatherProvider.getForecast(eq("tampere"), any())).thenReturn(List.of());
        when(trafficProvider.getAnnouncements(eq("tampere"), any(), any(), any())).thenReturn(List.of());
        TourRequest today = new TourRequest(
                "tampere", LocalDate.now(ZONE), LocalTime.of(23, 0), 1, null, null);

        assertThat(service.generateTour(today).stops()).hasSize(1);
    }

    @Test
    void aDateBeyondTheForecastRangeStillGivesATourWithoutWeather() {
        givenTampereWithOneStop();
        LocalDate far = LocalDate.now(ZONE).plusDays(30);
        when(weatherProvider.getForecast("tampere", far)).thenReturn(List.of());
        when(trafficProvider.getAnnouncements(eq("tampere"), any(), any(), any())).thenReturn(List.of());
        TourRequest request = new TourRequest("tampere", far, LocalTime.of(10, 0), 4, null, null);

        TourResult result = service.generateTour(request);

        assertThat(result.weatherStatus()).isEqualTo(DataStatus.EMPTY);
        assertThat(result.stops()).hasSize(1);
    }

    @Test
    void failsForAnUnknownCityWithoutCallingProviders() {
        when(placeRepository.findCities()).thenReturn(List.of(new City("tampere", "Tampere")));

        assertThatThrownBy(() -> service.generateTour(request("atlantis", null, null)))
                .isInstanceOf(CityNotFoundException.class);

        verifyNoInteractions(weatherProvider, trafficProvider);
    }

    @Test
    void findsTheCityIgnoringCaseAndCallsProvidersWithItsId() {
        givenTampereWithOneStop();
        when(weatherProvider.getForecast("tampere", DATE)).thenReturn(List.of(FORECAST));
        when(trafficProvider.getAnnouncements(eq("tampere"), eq(Language.EN), any(), any())).thenReturn(List.of(ANNOUNCEMENT));

        TourResult result = service.generateTour(request("TAMPERE", null, null));

        assertThat(result.weatherStatus()).isEqualTo(DataStatus.AVAILABLE);
        verify(weatherProvider).getForecast("tampere", DATE);
    }

    @Test
    void returnsWeatherTrafficStopsAndExplanation() {
        givenTampereWithOneStop();
        when(weatherProvider.getForecast("tampere", DATE)).thenReturn(List.of(FORECAST));
        when(trafficProvider.getAnnouncements(eq("tampere"), eq(Language.FI), any(), any())).thenReturn(List.of(ANNOUNCEMENT));

        TourResult result = service.generateTour(request("tampere", Language.FI, null));

        assertThat(result.language()).isEqualTo(Language.FI);
        assertThat(result.forecast()).containsExactly(FORECAST);
        assertThat(result.announcements()).containsExactly(ANNOUNCEMENT);
        assertThat(result.weatherStatus()).isEqualTo(DataStatus.AVAILABLE);
        assertThat(result.trafficStatus()).isEqualTo(DataStatus.AVAILABLE);
        assertThat(result.trafficTextLanguage()).isEqualTo("fi");
        assertThat(result.explanation()).isEqualTo("because");
        assertThat(result.stops()).hasSize(1);
        assertThat(result.stops().get(0).place().name()).isEqualTo("Tuomiokirkko");
    }

    @Test
    void reportsEmptyWhenAProviderReturnsNothing() {
        givenTampereWithOneStop();
        when(weatherProvider.getForecast("tampere", DATE)).thenReturn(List.of());
        when(trafficProvider.getAnnouncements(eq("tampere"), eq(Language.EN), any(), any())).thenReturn(List.of());

        TourResult result = service.generateTour(request("tampere", null, null));

        assertThat(result.weatherStatus()).isEqualTo(DataStatus.EMPTY);
        assertThat(result.trafficStatus()).isEqualTo(DataStatus.EMPTY);
        assertThat(result.trafficTextLanguage()).isNull();
    }

    @Test
    void aFailingProviderIsUnavailableAndTheTourIsStillPlanned() {
        givenTampereWithOneStop();
        when(weatherProvider.getForecast("tampere", DATE))
                .thenThrow(new ProviderException("FMI", "down"));
        when(trafficProvider.getAnnouncements(eq("tampere"), eq(Language.EN), any(), any())).thenReturn(List.of(ANNOUNCEMENT));

        TourResult result = service.generateTour(request("tampere", null, null));

        assertThat(result.weatherStatus()).isEqualTo(DataStatus.UNAVAILABLE);
        assertThat(result.forecast()).isEmpty();
        assertThat(result.trafficStatus()).isEqualTo(DataStatus.AVAILABLE);
        assertThat(result.stops()).hasSize(1);
    }

    @Test
    void bothProvidersFailingStillGivesATour() {
        givenTampereWithOneStop();
        when(weatherProvider.getForecast("tampere", DATE))
                .thenThrow(new ProviderException("FMI", "down"));
        when(trafficProvider.getAnnouncements(eq("tampere"), eq(Language.EN), any(), any()))
                .thenThrow(new ProviderException("Digitraffic", "down"));

        TourResult result = service.generateTour(request("tampere", null, null));

        assertThat(result.weatherStatus()).isEqualTo(DataStatus.UNAVAILABLE);
        assertThat(result.trafficStatus()).isEqualTo(DataStatus.UNAVAILABLE);
        assertThat(result.stops()).hasSize(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void plannerGetsNoDataForFactorsThatAreSwitchedOffButTheResultStillHasIt() {
        givenTampereWithOneStop();
        when(weatherProvider.getForecast("tampere", DATE)).thenReturn(List.of(FORECAST));
        when(trafficProvider.getAnnouncements(eq("tampere"), eq(Language.EN), any(), any())).thenReturn(List.of(ANNOUNCEMENT));

        TourResult result = service.generateTour(request("tampere", null, Set.of()));

        ArgumentCaptor<List<HourlyForecast>> forecast = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<TrafficAnnouncement>> announcements = ArgumentCaptor.forClass(List.class);
        verify(strategy).plan(any(), any(), forecast.capture(), announcements.capture());

        assertThat(forecast.getValue()).isEmpty();
        assertThat(announcements.getValue()).isEmpty();
        assertThat(result.forecast()).containsExactly(FORECAST);
        assertThat(result.announcements()).containsExactly(ANNOUNCEMENT);
        assertThat(result.factors()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void plannerGetsOnlyTheDataOfTheEnabledFactor() {
        givenTampereWithOneStop();
        when(weatherProvider.getForecast("tampere", DATE)).thenReturn(List.of(FORECAST));
        when(trafficProvider.getAnnouncements(eq("tampere"), eq(Language.EN), any(), any())).thenReturn(List.of(ANNOUNCEMENT));

        service.generateTour(request("tampere", null, Set.of(Factor.WEATHER)));

        ArgumentCaptor<List<HourlyForecast>> forecast = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<TrafficAnnouncement>> announcements = ArgumentCaptor.forClass(List.class);
        verify(strategy).plan(any(), any(), forecast.capture(), announcements.capture());

        assertThat(forecast.getValue()).containsExactly(FORECAST);
        assertThat(announcements.getValue()).isEmpty();
    }

    @Test
    void passesThePlacesOfTheCityToTheStrategy() {
        givenTampereWithOneStop();
        when(weatherProvider.getForecast(eq("tampere"), any())).thenReturn(List.of());
        when(trafficProvider.getAnnouncements(eq("tampere"), any(), any(), any())).thenReturn(List.of());

        service.generateTour(request("tampere", null, null));

        verify(strategy).plan(eq(List.of(PLACE)), any(), any(), any());
    }

    @Test
    void asksTrafficOnlyForTheTourWindowInPlannerTime() {
        givenTampereWithOneStop();
        when(weatherProvider.getForecast("tampere", DATE)).thenReturn(List.of());
        when(trafficProvider.getAnnouncements(eq("tampere"), any(), any(), any())).thenReturn(List.of());

        service.generateTour(request("tampere", null, null));

        OffsetDateTime start = DATE.atTime(10, 0).atZone(ZONE).toOffsetDateTime();
        verify(trafficProvider).getAnnouncements("tampere", Language.EN, start, start.plusHours(4));
    }

    @Test
    void listsTheCities() {
        when(placeRepository.findCities()).thenReturn(List.of(new City("tampere", "Tampere")));

        assertThat(service.listCities()).containsExactly(new City("tampere", "Tampere"));
    }

    @Test
    void listsPlacesInTheRequestedLanguage() {
        when(placeRepository.findCities()).thenReturn(List.of(new City("tampere", "Tampere")));
        when(placeRepository.findByCity("tampere")).thenReturn(List.of(PLACE));

        List<PlaceView> places = service.listPlaces("Tampere", Language.FI);

        assertThat(places).extracting(PlaceView::name).containsExactly("Tuomiokirkko");
    }

    @Test
    void listPlacesFailsForAnUnknownCity() {
        when(placeRepository.findCities()).thenReturn(List.of(new City("tampere", "Tampere")));

        assertThatThrownBy(() -> service.listPlaces("atlantis", Language.EN))
                .isInstanceOf(CityNotFoundException.class);
    }
}
