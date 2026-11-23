package fi.citytour.planner.planning;

import fi.citytour.planner.config.PlannerProperties;
import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.common.LocalizedText;
import fi.citytour.planner.model.place.Place;
import fi.citytour.planner.model.place.PlaceCategory;
import fi.citytour.planner.model.tour.Factor;
import fi.citytour.planner.model.tour.Stop;
import fi.citytour.planner.model.tour.TourRequest;
import fi.citytour.planner.model.traffic.Coordinate;
import fi.citytour.planner.model.traffic.GeoShape;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;
import fi.citytour.planner.model.weather.HourlyForecast;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/** Small builders shared by the planning tests. */
final class PlanningFixtures {

    /** Finnish time in winter (the test dates in November). */
    static final ZoneOffset WINTER = ZoneOffset.ofHours(2);

    private PlanningFixtures() {}

    static Place place(String id, boolean indoor, double lat, double lon, int visitMinutes) {
        return new Place(
                id,
                new LocalizedText(id, id),
                "tampere",
                PlaceCategory.LANDMARK,
                indoor,
                lat,
                lon,
                visitMinutes,
                new LocalizedText("https://example.org/en", "https://example.org/fi"));
    }

    /** A request for a tour that starts at 10:00, with both factors on. */
    static TourRequest request(LocalDate date, int hours) {
        return request("tampere", date, LocalTime.of(10, 0), hours, Language.EN,
                Set.of(Factor.WEATHER, Factor.TRAFFIC));
    }

    static TourRequest request(
            String city,
            LocalDate date,
            LocalTime startTime,
            int hours,
            Language language,
            Set<Factor> factors) {
        return new TourRequest(city, date, startTime, hours, language, factors);
    }

    /** A planner with the default settings. */
    static NearestPlaceTourPlanner planner() {
        return planner(planning -> { });
    }

    /** A planner whose planning settings are changed by the given customizer. */
    static NearestPlaceTourPlanner planner(Consumer<PlannerProperties.Planning> customizer) {
        PlannerProperties properties = new PlannerProperties();
        customizer.accept(properties.getPlanning());
        return new NearestPlaceTourPlanner(properties);
    }

    /** A forecast hour; the time is written in UTC, for example "2026-11-23T11:00:00Z". */
    static HourlyForecast hour(String utcTime, Double precipitation) {
        return new HourlyForecast(OffsetDateTime.parse(utcTime), 1.0, precipitation, 50.0, 3.0);
    }

    /** An announcement at one point; a null time means "no start" or "no end". */
    static TrafficAnnouncement announcement(
            OffsetDateTime from, OffsetDateTime to, double lat, double lon) {
        return announcement(from, to, new GeoShape(List.of(List.of(new Coordinate(lat, lon)))));
    }

    static TrafficAnnouncement announcement(OffsetDateTime from, OffsetDateTime to, GeoShape geometry) {
        return new TrafficAnnouncement("ROAD_WORKS", "Road works", "Tampere", "en", from, to, geometry);
    }

    /** A time on the given date in Finnish winter time. */
    static OffsetDateTime at(LocalDate date, int hour, int minute) {
        return date.atTime(hour, minute).atOffset(WINTER);
    }

    static List<String> ids(List<Stop> stops) {
        return stops.stream().map(stop -> stop.place().id()).toList();
    }
}
