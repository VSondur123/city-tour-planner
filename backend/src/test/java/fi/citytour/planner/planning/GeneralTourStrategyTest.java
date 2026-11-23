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
import static fi.citytour.planner.planning.PlanningFixtures.place;
import static fi.citytour.planner.planning.PlanningFixtures.request;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GeneralTourStrategyTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 23);

    private final NearestPlaceTourPlanner planner = mock(NearestPlaceTourPlanner.class);
    private final GeneralTourStrategy strategy = new GeneralTourStrategy(planner);

    @Test
    void usesAllPlacesIndoorAndOutdoor() {
        List<Place> places = List.of(
                place("INDOOR", true, 60.17, 24.94, 30),
                place("OUTDOOR", false, 60.18, 24.95, 30));
        TourRequest request = request(DAY, 4);
        List<TrafficAnnouncement> announcements = List.of(announcement(at(DAY, 10, 0), null, 60.17, 24.94));
        List<Stop> planned = List.of();
        when(planner.plan(places, request, announcements)).thenReturn(planned);

        List<Stop> stops = strategy.plan(places, request, List.of(), announcements);

        assertThat(stops).isSameAs(planned);
        verify(planner).plan(places, request, announcements);
    }

    @Test
    void explainsInEnglishAndFinnish() {
        assertThat(strategy.explanation(Language.EN))
                .isEqualTo("This tour uses both indoor and outdoor places because no rain is expected, "
                        + "or because the weather was not used or was not available.");
        assertThat(strategy.explanation(Language.FI))
                .isEqualTo("Tämä kierros käyttää sekä sisä- että ulkokohteita, "
                        + "koska sadetta ei odoteta tai säätä ei otettu huomioon "
                        + "tai säätiedot eivät olleet saatavilla.");
    }
}
