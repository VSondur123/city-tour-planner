package fi.citytour.planner.planning;

import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.place.Place;
import fi.citytour.planner.model.tour.Stop;
import fi.citytour.planner.model.tour.TourRequest;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static fi.citytour.planner.planning.PlanningFixtures.announcement;
import static fi.citytour.planner.planning.PlanningFixtures.at;
import static fi.citytour.planner.planning.PlanningFixtures.ids;
import static fi.citytour.planner.planning.PlanningFixtures.place;
import static fi.citytour.planner.planning.PlanningFixtures.planner;
import static fi.citytour.planner.planning.PlanningFixtures.request;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IndoorTourStrategyTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 23);

    private static final Place INDOOR_ONE = place("INDOOR1", true, 60.17, 24.94, 30);
    private static final Place INDOOR_TWO = place("INDOOR2", true, 60.175, 24.945, 30);
    private static final Place OUTDOOR = place("OUTDOOR", false, 60.18, 24.95, 30);

    @Test
    void handsOnlyTheIndoorPlacesToThePlanner() {
        NearestPlaceTourPlanner planner = mock(NearestPlaceTourPlanner.class);
        IndoorTourStrategy strategy = new IndoorTourStrategy(planner);
        TourRequest request = request(DAY, 4);
        List<TrafficAnnouncement> announcements = List.of(announcement(at(DAY, 10, 0), null, 60.17, 24.94));
        List<Stop> planned = List.of();
        when(planner.plan(List.of(INDOOR_ONE, INDOOR_TWO), request, announcements)).thenReturn(planned);

        List<Stop> stops = strategy.plan(
                List.of(INDOOR_ONE, OUTDOOR, INDOOR_TWO), request, List.of(), announcements);

        assertThat(stops).isSameAs(planned);
        verify(planner).plan(List.of(INDOOR_ONE, INDOOR_TWO), request, announcements);
    }

    @Test
    void neverPlansAnOutdoorPlace() {
        IndoorTourStrategy strategy = new IndoorTourStrategy(planner());

        for (int day = 0; day < 30; day++) {
            List<Stop> stops = strategy.plan(
                    List.of(INDOOR_ONE, OUTDOOR, INDOOR_TWO),
                    request(DAY.plusDays(day), 6), List.of(), List.of());

            assertThat(ids(stops)).isNotEmpty().doesNotContain("OUTDOOR");
        }
    }

    @Test
    void theTourIsEmptyWhenThereIsNoIndoorPlace() {
        IndoorTourStrategy strategy = new IndoorTourStrategy(planner());

        assertThat(strategy.plan(List.of(OUTDOOR), request(DAY, 4), List.of(), List.of())).isEmpty();
    }

    @Test
    void explainsInEnglishAndFinnish() {
        IndoorTourStrategy strategy = new IndoorTourStrategy(planner());

        assertThat(strategy.explanation(Language.EN))
                .isEqualTo("Rain is expected during your tour, so only indoor places were considered.");
        assertThat(strategy.explanation(Language.FI))
                .isEqualTo("Kierroksen aikana odotetaan sadetta, joten huomioon otettiin vain sisäkohteet.");
    }
}
