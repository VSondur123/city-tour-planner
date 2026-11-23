package fi.citytour.planner.planning;

import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.common.LocalizedText;
import fi.citytour.planner.model.place.Place;
import fi.citytour.planner.model.tour.Stop;
import fi.citytour.planner.model.tour.TourRequest;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;
import fi.citytour.planner.model.weather.HourlyForecast;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Strategy that selects stops without weather restrictions: every place can be
 * used, indoor or outdoor. The tour itself is built by
 * {@link NearestPlaceTourPlanner}.
 */
@Component
public class GeneralTourStrategy implements TourStrategy {

    /** Text for the "Why this tour" box (D-013). */
    private static final LocalizedText EXPLANATION = new LocalizedText(
            "This tour uses both indoor and outdoor places because no rain is expected, "
                    + "or because the weather was not used or was not available.",
            "Tämä kierros käyttää sekä sisä- että ulkokohteita, "
                    + "koska sadetta ei odoteta tai säätä ei otettu huomioon "
                    + "tai säätiedot eivät olleet saatavilla.");

    private final NearestPlaceTourPlanner planner;

    /** Creates the strategy. */
    public GeneralTourStrategy(NearestPlaceTourPlanner planner) {
        this.planner = planner;
    }

    @Override
    public List<Stop> plan(
            List<Place> places,
            TourRequest request,
            List<HourlyForecast> forecast,
            List<TrafficAnnouncement> announcements) {

        return planner.plan(places, request, announcements);
    }

    @Override
    public String explanation(Language language) {
        return EXPLANATION.resolve(language);
    }
}
