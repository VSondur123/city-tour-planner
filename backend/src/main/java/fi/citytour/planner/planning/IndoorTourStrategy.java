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
 * Strategy used when rain is expected: only indoor places are used (D-019).
 * The tour itself is built by {@link NearestPlaceTourPlanner}.
 */
@Component
public class IndoorTourStrategy implements TourStrategy {

    /** Text for the "Why this tour" box (D-022). */
    private static final LocalizedText EXPLANATION = new LocalizedText(
            "Rain is expected during your tour, so only indoor places were considered.",
            "Kierroksen aikana odotetaan sadetta, joten huomioon otettiin vain sisäkohteet.");

    private final NearestPlaceTourPlanner planner;

    /** Creates the strategy. */
    public IndoorTourStrategy(NearestPlaceTourPlanner planner) {
        this.planner = planner;
    }

    @Override
    public List<Stop> plan(
            List<Place> places,
            TourRequest request,
            List<HourlyForecast> forecast,
            List<TrafficAnnouncement> announcements) {

        return planner.plan(places.stream().filter(Place::indoor).toList(), request, announcements);
    }

    @Override
    public String explanation(Language language) {
        return EXPLANATION.resolve(language);
    }
}
