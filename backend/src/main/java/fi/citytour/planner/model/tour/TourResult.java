package fi.citytour.planner.model.tour;

import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;
import fi.citytour.planner.model.weather.HourlyForecast;

import java.util.List;
import java.util.Set;

/**
 * The result of a tour request. Weather and traffic are always included when available;
 * {@code factors} only says what the planner used.
 */
public record TourResult(
        Language language,
        Set<Factor> factors,
        List<StopView> stops,
        List<HourlyForecast> forecast,
        List<TrafficAnnouncement> announcements,
        DataStatus weatherStatus,
        DataStatus trafficStatus,
        String trafficTextLanguage,
        String explanation
) {}
