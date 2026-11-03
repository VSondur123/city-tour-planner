package fi.citytour.planner.planning;

import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.place.Place;
import fi.citytour.planner.model.tour.Stop;
import fi.citytour.planner.model.tour.TourRequest;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;
import fi.citytour.planner.model.weather.HourlyForecast;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Strategy used when rain is expected, favouring indoor places.
 * TODO (Georgios): empty placeholder, implement the weather-aware selection rule.
 */
@Component
public class IndoorTourStrategy implements TourStrategy {

    @Override
    public List<Stop> plan(
            List<Place> places,
            TourRequest request,
            List<HourlyForecast> forecast,
            List<TrafficAnnouncement> announcements) {

        return List.of();
    }

    @Override
    public String explanation(Language language) {
        return "";
    }
}
