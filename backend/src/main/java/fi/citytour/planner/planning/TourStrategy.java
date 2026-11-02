package fi.citytour.planner.planning;

import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.place.Place;
import fi.citytour.planner.model.tour.Stop;
import fi.citytour.planner.model.tour.TourRequest;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;
import fi.citytour.planner.model.weather.HourlyForecast;

import java.util.List;

/** A rule for selecting and ordering the stops of a tour. */
public interface TourStrategy {

    /** Selects and orders stops that fit within the requested duration. */
    List<Stop> plan(
            List<Place> places,
            TourRequest request,
            List<HourlyForecast> forecast,
            List<TrafficAnnouncement> announcements);

    /** Explains, in the given language, why this strategy was chosen. */
    String explanation(Language language);
}
