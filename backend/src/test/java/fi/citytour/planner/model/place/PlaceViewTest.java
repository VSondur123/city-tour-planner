package fi.citytour.planner.model.place;

import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.common.LocalizedText;
import fi.citytour.planner.model.tour.Stop;
import fi.citytour.planner.model.tour.StopView;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceViewTest {

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

    @Test
    void resolvesTheTextToTheRequestedLanguage() {
        PlaceView view = PlaceView.of(PLACE, Language.FI);

        assertThat(view.name()).isEqualTo("Tuomiokirkko");
        assertThat(view.link()).isEqualTo("https://example.org/fi");
        assertThat(view.category()).isEqualTo(PlaceCategory.LANDMARK);
        assertThat(view.indoor()).isTrue();
        assertThat(view.visitMinutes()).isEqualTo(30);
    }

    @Test
    void withCityReturnsACopyForTheGivenCity() {
        Place moved = PLACE.withCity("helsinki");

        assertThat(moved.city()).isEqualTo("helsinki");
        assertThat(moved.id()).isEqualTo(PLACE.id());
        assertThat(PLACE.city()).isEqualTo("tampere");
    }

    @Test
    void stopViewKeepsTheTimesAndResolvesThePlace() {
        OffsetDateTime start = OffsetDateTime.parse("2026-11-21T10:00:00+02:00");
        OffsetDateTime end = start.plusMinutes(30);

        StopView view = StopView.of(new Stop(PLACE, start, end), Language.EN);

        assertThat(view.place().name()).isEqualTo("Cathedral");
        assertThat(view.start()).isEqualTo(start);
        assertThat(view.end()).isEqualTo(end);
    }
}
