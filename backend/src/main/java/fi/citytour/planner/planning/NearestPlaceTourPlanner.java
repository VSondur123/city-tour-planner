package fi.citytour.planner.planning;

import fi.citytour.planner.config.PlannerProperties;
import fi.citytour.planner.model.place.Place;
import fi.citytour.planner.model.tour.Stop;
import fi.citytour.planner.model.tour.TourRequest;
import fi.citytour.planner.model.traffic.Coordinate;
import fi.citytour.planner.model.traffic.GeoShape;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Builds a tour from candidate places: a random first stop, then always the
 * nearest place that still fits before the end of the tour. A place is skipped
 * for a visit that overlaps a traffic announcement near it. The strategies decide
 * which places are candidates; this class does the shared work.
 */
@Component
public class NearestPlaceTourPlanner {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final double walkingSpeedKmh;
    private final double trafficDistanceMeters;
    private final int maxOpenEndedAgeDays;
    private final ZoneId zone;

    /** Creates the planner with the settings from the configuration. */
    public NearestPlaceTourPlanner(PlannerProperties properties) {
        this.walkingSpeedKmh = properties.getPlanning().getWalkingSpeedKmh();
        this.trafficDistanceMeters = properties.getPlanning().getTrafficDistanceMeters();
        this.maxOpenEndedAgeDays = properties.getPlanning().getMaxOpenEndedAgeDays();
        this.zone = ZoneId.of(properties.getZone());
    }

    /**
     * Plans the stops from the given candidate places. The announcements are the
     * ones the user allowed (empty when the traffic factor is off).
     */
    public List<Stop> plan(
            List<Place> candidates,
            TourRequest request,
            List<TrafficAnnouncement> announcements) {

        // Tour window in the planner's local time
        ZonedDateTime tourStart = request.date().atTime(request.startTime()).atZone(zone);
        ZonedDateTime endOfTour = tourStart.plusHours(request.durationHours());
        int tourMinutes = request.durationHours() * 60;
        Random random = seededRandom(request);

        // Only places whose visit fits in the whole tour can be used at all
        List<Place> remaining = new ArrayList<>(candidates.stream()
                .filter(place -> place.visitMinutes() <= tourMinutes)
                .toList());

        // Which announcements are near each place (checked once, not on every step)
        Map<Place, List<TrafficAnnouncement>> nearby = nearbyAnnouncements(remaining, announcements, tourStart);

        // First stop: a random place that is not blocked at the tour start
        List<Place> startable = remaining.stream()
                .filter(place -> !isBlocked(place, tourStart,
                        tourStart.plusMinutes(place.visitMinutes()), endOfTour, nearby))
                .toList();

        if (startable.isEmpty()) {
            return List.of();
        }

        List<Stop> stops = new ArrayList<>();

        Place current = startable.get(random.nextInt(startable.size()));
        remaining.remove(current);
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

                // An announcement near the place must not be active during the visit
                ZonedDateTime visitStart = now.plusMinutes(travelMinutes);
                if (isBlocked(place, visitStart,
                        visitStart.plusMinutes(place.visitMinutes()), endOfTour, nearby)) {
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

    /**
     * Same request gives the same tour: the seed comes from the city, date, start
     * time and duration (not from the language or the factors).
     */
    private static Random seededRandom(TourRequest request) {
        return new Random(Objects.hash(
                request.city().toLowerCase(Locale.ROOT),
                request.date(),
                request.startTime(),
                request.durationHours()));
    }

    /**
     * A place is blocked for a visit if an announcement near it is active at the
     * same time. An announcement without an end time counts as active until the
     * end of the tour; one without a start time counts as already active.
     */
    private static boolean isBlocked(
            Place place,
            ZonedDateTime visitStart,
            ZonedDateTime visitEnd,
            ZonedDateTime endOfTour,
            Map<Place, List<TrafficAnnouncement>> nearby) {

        for (TrafficAnnouncement announcement : nearby.getOrDefault(place, List.of())) {
            Instant from = announcement.startTime() == null
                    ? Instant.MIN
                    : announcement.startTime().toInstant();
            Instant until = announcement.endTime() == null
                    ? endOfTour.toInstant()
                    : announcement.endTime().toInstant();

            // The two time ranges overlap
            if (from.isBefore(visitEnd.toInstant()) && visitStart.toInstant().isBefore(until)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds, for each place, the announcements located near it. Announcements that never end
     * and started long before the tour are ignored (see {@link #isStaleOpenEnded}).
     */
    private Map<Place, List<TrafficAnnouncement>> nearbyAnnouncements(
            List<Place> places,
            List<TrafficAnnouncement> announcements,
            ZonedDateTime tourStart) {

        Map<Place, List<TrafficAnnouncement>> result = new HashMap<>();
        for (Place place : places) {
            List<TrafficAnnouncement> near = announcements.stream()
                    .filter(announcement -> !isStaleOpenEnded(announcement, tourStart))
                    .filter(announcement -> isNear(place, announcement.geometry()))
                    .toList();
            if (!near.isEmpty()) {
                result.put(place, near);
            }
        }
        return result;
    }

    /**
     * True for an announcement with no end time that started more than the configured number of
     * days before the tour. Digitraffic keeps such notices "ongoing" until someone updates them,
     * so an old one is more likely forgotten than real. It is still shown to the user. An
     * announcement without a start time cannot be aged and is kept.
     */
    private boolean isStaleOpenEnded(TrafficAnnouncement announcement, ZonedDateTime tourStart) {
        return announcement.endTime() == null
                && announcement.startTime() != null
                && announcement.startTime().toInstant()
                        .isBefore(tourStart.minusDays(maxOpenEndedAgeDays).toInstant());
    }

    /** True if the place is within the configured distance of any path of the shape. */
    private boolean isNear(Place place, GeoShape geometry) {
        if (geometry == null || geometry.paths() == null) {
            return false;   // no location: we cannot say it is near
        }
        for (List<Coordinate> path : geometry.paths()) {
            if (path == null) {
                continue;
            }
            if (path.size() == 1
                    && distanceToSegmentMeters(place, path.get(0), path.get(0)) <= trafficDistanceMeters) {
                return true;
            }
            for (int i = 0; i + 1 < path.size(); i++) {
                if (distanceToSegmentMeters(place, path.get(i), path.get(i + 1)) <= trafficDistanceMeters) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Distance in metres from a place to the line segment between two points.
     * Uses a flat map around the place, which is accurate enough at city scale.
     */
    private static double distanceToSegmentMeters(Place place, Coordinate a, Coordinate b) {
        double metersPerDegree = Math.toRadians(1) * EARTH_RADIUS_KM * 1000;
        double cosLat = Math.cos(Math.toRadians(place.lat()));

        // Segment end points in metres, with the place at (0, 0)
        double ax = (a.lon() - place.lon()) * cosLat * metersPerDegree;
        double ay = (a.lat() - place.lat()) * metersPerDegree;
        double bx = (b.lon() - place.lon()) * cosLat * metersPerDegree;
        double by = (b.lat() - place.lat()) * metersPerDegree;

        double dx = bx - ax;
        double dy = by - ay;
        double lengthSquared = dx * dx + dy * dy;

        // Closest point on the segment, as a fraction t between 0 and 1
        double t = lengthSquared == 0
                ? 0
                : Math.max(0, Math.min(1, -(ax * dx + ay * dy) / lengthSquared));

        double closestX = ax + t * dx;
        double closestY = ay + t * dy;
        return Math.sqrt(closestX * closestX + closestY * closestY);
    }

    /** Walking time in whole minutes (rounded up) for a distance in kilometres. */
    private long travelMinutes(double km) {
        return (long) Math.ceil(km / walkingSpeedKmh * 60);
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
}
