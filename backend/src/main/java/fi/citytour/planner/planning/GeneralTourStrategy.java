package fi.citytour.planner.planning;

import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.common.LocalizedText;
import fi.citytour.planner.model.place.Place;
import fi.citytour.planner.model.tour.Stop;
import fi.citytour.planner.model.tour.TourRequest;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;
import fi.citytour.planner.model.weather.HourlyForecast;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Strategy that selects stops without weather restrictions.
 * The first stop is a random place that fits the tour; every next stop is the
 * nearest place that still fits (walking time included). The tour ends when no
 * remaining place fits before the end of the tour.
 */
@Component
public class GeneralTourStrategy implements TourStrategy {

    /** Walking speed used to turn a distance into travel time (D-020). */
    private static final double WALKING_SPEED_KMH = 5.0;

    /** The app's time is Finnish time (D-012). */
    private static final ZoneId FINLAND = ZoneId.of("Europe/Helsinki");

    private static final double EARTH_RADIUS_KM = 6371.0;

    /** Text for the "Why this tour" box (D-013). */
    private static final LocalizedText EXPLANATION = new LocalizedText(
            "This tour uses both indoor and outdoor places because rain is not expected, "
                    + "or the weather was not taken into account.",
            "Tämä kierros käyttää sekä sisä- että ulkokohteita, "
                    + "koska sadetta ei odoteta tai säätä ei otettu huomioon.");

    private final Random random = new Random();

    @Override
    public List<Stop> plan(
            List<Place> places,
            TourRequest request,
            List<HourlyForecast> forecast,
            List<TrafficAnnouncement> announcements) {

        // Tour window in Finnish time
        ZonedDateTime tourStart = request.date().atTime(request.startTime()).atZone(FINLAND);
        ZonedDateTime endOfTour = tourStart.plusHours(request.durationHours());
        int tourMinutes = request.durationHours() * 60;

        // Only places whose visit fits in the whole tour can be used at all
        List<Place> remaining = new ArrayList<>(places.stream()
                .filter(place -> place.visitMinutes() <= tourMinutes)
                .toList());

        if (remaining.isEmpty()) {
            return List.of();
        }

        List<Stop> stops = new ArrayList<>();

        // First stop: a random place, starting at the tour start
        Place current = remaining.remove(random.nextInt(remaining.size()));
        ZonedDateTime now = tourStart.plusMinutes(current.visitMinutes());
        stops.add(new Stop(current, tourStart.toOffsetDateTime(), now.toOffsetDateTime()));

        // Next stops: the nearest place that still fits, until none fits
        while (true) {
            List<Place> nearestPlaces = new ArrayList<>();
            double nearestKm = Double.MAX_VALUE;

            for (Place place : remaining) {
                double km = distanceKm(current, place);
                long travelMinutes = travelMinutes(km);

                // The whole visit must end before (or exactly at) the end of the tour
                if (now.plusMinutes(travelMinutes + place.visitMinutes()).isAfter(endOfTour)) {
                    continue;
                }
                if (km < nearestKm) {
                    nearestKm = km;
                    nearestPlaces.clear();
                    nearestPlaces.add(place);
                } else if (km == nearestKm) {
                    nearestPlaces.add(place);   // same distance: pick randomly below
                }
            }

            if (nearestPlaces.isEmpty()) {
                break;   // no remaining place fits
            }

            Place next = nearestPlaces.get(random.nextInt(nearestPlaces.size()));
            ZonedDateTime start = now.plusMinutes(travelMinutes(nearestKm));
            ZonedDateTime end = start.plusMinutes(next.visitMinutes());

            stops.add(new Stop(next, start.toOffsetDateTime(), end.toOffsetDateTime()));
            remaining.remove(next);
            current = next;
            now = end;
        }

        return stops;
    }

    /** Walking time in whole minutes (rounded up) for a distance in kilometres. */
    private static long travelMinutes(double km) {
        return (long) Math.ceil(km / WALKING_SPEED_KMH * 60);
    }

    /** Straight-line distance between two places (haversine formula). */
    private static double distanceKm(Place a, Place b) {
        double lat1 = Math.toRadians(a.lat());
        double lat2 = Math.toRadians(b.lat());
        double deltaLat = lat2 - lat1;
        double deltaLon = Math.toRadians(b.lon() - a.lon());

        double h = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(h));
    }

    @Override
    public String explanation(Language language) {
        return EXPLANATION.resolve(language);
    }
}
