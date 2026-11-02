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
 * Strategy that selects stops without weather restrictions.
 * TODO (Georgios): empty placeholder, implement the general selection and ordering rule.
 */
@Component
public class GeneralTourStrategy implements TourStrategy {

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
