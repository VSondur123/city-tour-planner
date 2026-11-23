package fi.citytour.planner.planning;

import fi.citytour.planner.config.PlannerProperties;
import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.tour.Factor;
import fi.citytour.planner.model.tour.TourRequest;
import fi.citytour.planner.model.weather.HourlyForecast;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static fi.citytour.planner.planning.PlanningFixtures.hour;
import static fi.citytour.planner.planning.PlanningFixtures.request;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * The tour in these tests runs from 12:00 to 14:00 Finnish time on 2026-11-23.
 * In November Finland is UTC+2, so the forecast hours below, written in UTC, are
 * 10:00Z = 12:00, 11:00Z = 13:00 and 12:00Z = 14:00 local time.
 */
class TourStrategyFactoryTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 23);
    private static final Set<Factor> BOTH = Set.of(Factor.WEATHER, Factor.TRAFFIC);

    private final GeneralTourStrategy general = mock(GeneralTourStrategy.class);
    private final IndoorTourStrategy indoor = mock(IndoorTourStrategy.class);
    private final PlannerProperties properties = new PlannerProperties();

    private TourStrategy choose(List<HourlyForecast> forecast, Set<Factor> factors) {
        TourStrategyFactory factory = new TourStrategyFactory(general, indoor, properties);
        TourRequest request = request("tampere", DATE, LocalTime.of(12, 0), 2, Language.EN, factors);
        return factory.choose(forecast, request);
    }

    @Test
    void choosesIndoorWhenRainIsExpectedInsideTheTour() {
        assertThat(choose(List.of(hour("2026-11-23T11:00:00Z", 2.0)), BOTH)).isSameAs(indoor);
    }

    @Test
    void rainExactlyAtTheStartOfTheTourCounts() {
        assertThat(choose(List.of(hour("2026-11-23T10:00:00Z", 2.0)), BOTH)).isSameAs(indoor);
    }

    @Test
    void rainExactlyAtTheEndOfTheTourDoesNotCount() {
        assertThat(choose(List.of(hour("2026-11-23T12:00:00Z", 2.0)), BOTH)).isSameAs(general);
    }

    @Test
    void rainBeforeTheTourDoesNotCount() {
        assertThat(choose(List.of(hour("2026-11-23T09:00:00Z", 2.0)), BOTH)).isSameAs(general);
    }

    @Test
    void oneRainyHourIsEnoughEvenIfTheOtherHoursAreDry() {
        List<HourlyForecast> forecast = List.of(
                hour("2026-11-23T10:00:00Z", 0.0),
                hour("2026-11-23T11:00:00Z", 1.5));

        assertThat(choose(forecast, BOTH)).isSameAs(indoor);
    }

    @Test
    void forecastTimesAreComparedInFinnishTime() {
        // 12:30Z is 14:30 in Finland, after the tour ends at 14:00. Read as a plain 12:30
        // it would look like it is inside the tour and the factory would wrongly pick indoor.
        assertThat(choose(List.of(hour("2026-11-23T12:30:00Z", 2.0)), BOTH)).isSameAs(general);

        // 11:00Z is 13:00 in Finland, inside the tour. Read as a plain 11:00 it would be
        // before the tour and the rain would be missed.
        assertThat(choose(List.of(hour("2026-11-23T11:00:00Z", 2.0)), BOTH)).isSameAs(indoor);
    }

    @Test
    void rainExactlyAtTheThresholdIsNotRain() {
        assertThat(choose(List.of(hour("2026-11-23T11:00:00Z", 0.5)), BOTH)).isSameAs(general);
    }

    @Test
    void rainJustAboveTheThresholdIsRain() {
        assertThat(choose(List.of(hour("2026-11-23T11:00:00Z", 0.6)), BOTH)).isSameAs(indoor);
    }

    @Test
    void missingPrecipitationIsNotRain() {
        assertThat(choose(List.of(hour("2026-11-23T11:00:00Z", null)), BOTH)).isSameAs(general);
    }

    @Test
    void usesTheConfiguredThreshold() {
        properties.getPlanning().setRainThresholdMm(2.0);

        assertThat(choose(List.of(hour("2026-11-23T11:00:00Z", 1.0)), BOTH)).isSameAs(general);
        assertThat(choose(List.of(hour("2026-11-23T11:00:00Z", 2.5)), BOTH)).isSameAs(indoor);
    }

    @Test
    void ignoresRainWhenTheWeatherFactorIsOff() {
        List<HourlyForecast> rainy = List.of(hour("2026-11-23T11:00:00Z", 5.0));

        assertThat(choose(rainy, Set.of(Factor.TRAFFIC))).isSameAs(general);
        assertThat(choose(rainy, Set.of())).isSameAs(general);
    }

    @Test
    void choosesGeneralWhenThereIsNoForecast() {
        assertThat(choose(List.of(), BOTH)).isSameAs(general);
    }
}
