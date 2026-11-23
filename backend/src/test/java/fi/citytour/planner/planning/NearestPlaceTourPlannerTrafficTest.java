package fi.citytour.planner.planning;

import fi.citytour.planner.model.place.Place;
import fi.citytour.planner.model.tour.Stop;
import fi.citytour.planner.model.traffic.Coordinate;
import fi.citytour.planner.model.traffic.GeoShape;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static fi.citytour.planner.planning.PlanningFixtures.announcement;
import static fi.citytour.planner.planning.PlanningFixtures.at;
import static fi.citytour.planner.planning.PlanningFixtures.ids;
import static fi.citytour.planner.planning.PlanningFixtures.place;
import static fi.citytour.planner.planning.PlanningFixtures.planner;
import static fi.citytour.planner.planning.PlanningFixtures.request;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests of the traffic rule: a place is skipped for a visit that overlaps an
 * announcement near it (within 200 metres by default).
 *
 * <p>Place X is the place the announcements are put next to. 0.0009 degrees of
 * latitude is about 100 metres and 0.0027 about 300 metres. The tours start at 10:00
 * on days in November, when Finland is UTC+2.
 */
class NearestPlaceTourPlannerTrafficTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 23);
    private static final int DAYS = 40;

    private static final double X_LAT = 60.1700;
    private static final double X_LON = 24.9400;

    private static final Place X = place("X", true, X_LAT, X_LON, 30);
    private static final List<Place> PLACES = List.of(
            X,
            place("B", true, 60.1750, 24.9450, 30),
            place("C", false, 60.1650, 24.9500, 30),
            place("D", true, 60.1780, 24.9300, 30),
            place("E", false, 60.1620, 24.9380, 30),
            place("F", true, 60.1710, 24.9550, 30));

    private final NearestPlaceTourPlanner planner = planner();

    /** The tours of {@link #DAYS} different days, each with the announcements built for that day. */
    private List<List<Stop>> tours(
            NearestPlaceTourPlanner planner,
            Function<LocalDate, List<TrafficAnnouncement>> announcementsForDay) {
        List<List<Stop>> tours = new ArrayList<>();
        for (int i = 0; i < DAYS; i++) {
            LocalDate date = DAY.plusDays(i);
            tours.add(planner.plan(PLACES, request(date, 4), announcementsForDay.apply(date)));
        }
        return tours;
    }

    private List<List<Stop>> tours(Function<LocalDate, List<TrafficAnnouncement>> announcementsForDay) {
        return tours(planner, announcementsForDay);
    }

    private static long toursWithX(List<List<Stop>> tours) {
        return tours.stream().filter(tour -> ids(tour).contains("X")).count();
    }

    private static List<TrafficAnnouncement> none(LocalDate date) {
        return List.of();
    }

    @Test
    void withoutAnnouncementsPlaceXIsUsed() {
        assertThat(toursWithX(tours(NearestPlaceTourPlannerTrafficTest::none))).isGreaterThan(0);
    }

    @Test
    void aNearbyAnnouncementThatNeverEndsBlocksThePlace() {
        // 100 metres from X, started at 08:00, no end time.
        List<List<Stop>> tours = tours(date -> List.of(
                announcement(at(date, 8, 0), null, X_LAT + 0.0009, X_LON)));

        assertThat(toursWithX(tours)).isZero();
    }

    @Test
    void anAnnouncementFurtherAwayThanTheDistanceIsIgnored() {
        long baseline = toursWithX(tours(NearestPlaceTourPlannerTrafficTest::none));

        // 300 metres from X, further than the default 200 metres.
        List<List<Stop>> tours = tours(date -> List.of(
                announcement(at(date, 8, 0), null, X_LAT + 0.0027, X_LON)));

        assertThat(toursWithX(tours)).isEqualTo(baseline);
    }

    @Test
    void theConfiguredDistanceIsUsed() {
        long baseline = toursWithX(tours(NearestPlaceTourPlannerTrafficTest::none));
        NearestPlaceTourPlanner tight = planner(planning -> planning.setTrafficDistanceMeters(50));

        // 100 metres away is outside a 50 metre limit.
        List<List<Stop>> tours = tours(tight, date -> List.of(
                announcement(at(date, 8, 0), null, X_LAT + 0.0009, X_LON)));

        assertThat(toursWithX(tours)).isEqualTo(baseline);
    }

    @Test
    void aLongRoadPassingNearThePlaceBlocksIt() {
        // An east-west road about 100 metres north of X. Its end points are over a kilometre
        // away, so only the distance to the line (not to its points) can find X.
        GeoShape road = new GeoShape(List.of(List.of(
                new Coordinate(X_LAT + 0.0009, X_LON - 0.02),
                new Coordinate(X_LAT + 0.0009, X_LON + 0.02))));

        List<List<Stop>> tours = tours(date -> List.of(announcement(at(date, 8, 0), null, road)));

        assertThat(toursWithX(tours)).isZero();
    }

    @Test
    void anAnnouncementOnlyBlocksVisitsThatOverlapIt() {
        // Active 10:00 to 11:00: X may be visited from 11:00 on, but not before.
        List<List<Stop>> tours = tours(date -> List.of(
                announcement(at(date, 10, 0), at(date, 11, 0), X_LAT + 0.0009, X_LON)));

        long visitedLater = 0;
        for (int i = 0; i < DAYS; i++) {
            OffsetDateTime end = at(DAY.plusDays(i), 11, 0);
            for (Stop stop : tours.get(i)) {
                if (stop.place().id().equals("X")) {
                    assertThat(stop.start().isBefore(end)).as("X is not visited during the announcement").isFalse();
                    visitedLater++;
                }
            }
        }
        assertThat(visitedLater).as("X can still be visited once the announcement is over").isGreaterThan(0);
    }

    @Test
    void anAnnouncementWithoutAnEndTimeIsActiveUntilTheEndOfTheTour() {
        // Starts at 10:30 and never ends: X is only possible if its visit ends by 10:30.
        List<List<Stop>> tours = tours(date -> List.of(
                announcement(at(date, 10, 30), null, X_LAT + 0.0009, X_LON)));

        for (int i = 0; i < DAYS; i++) {
            OffsetDateTime announcementStart = at(DAY.plusDays(i), 10, 30);
            for (Stop stop : tours.get(i)) {
                if (stop.place().id().equals("X")) {
                    assertThat(stop.end().isAfter(announcementStart)).isFalse();
                }
            }
        }
    }

    @Test
    void anAnnouncementWithoutAStartTimeIsActiveFromTheBeginning() {
        // Ends at 11:00 and has no start time: X is not visited before 11:00.
        List<List<Stop>> tours = tours(date -> List.of(
                announcement(null, at(date, 11, 0), X_LAT + 0.0009, X_LON)));

        for (int i = 0; i < DAYS; i++) {
            OffsetDateTime end = at(DAY.plusDays(i), 11, 0);
            for (Stop stop : tours.get(i)) {
                if (stop.place().id().equals("X")) {
                    assertThat(stop.start().isBefore(end)).isFalse();
                }
            }
        }
    }

    @Test
    void anAnnouncementWithoutALocationIsIgnored() {
        long baseline = toursWithX(tours(NearestPlaceTourPlannerTrafficTest::none));

        List<List<Stop>> noGeometry = tours(date -> List.of(announcement(at(date, 8, 0), null, (GeoShape) null)));
        List<List<Stop>> noPaths = tours(date -> List.of(announcement(at(date, 8, 0), null, new GeoShape(null))));

        assertThat(toursWithX(noGeometry)).isEqualTo(baseline);
        assertThat(toursWithX(noPaths)).isEqualTo(baseline);
    }

    @Test
    void anOpenEndedAnnouncementOlderThanTheLimitIsIgnored() {
        long baseline = toursWithX(tours(NearestPlaceTourPlannerTrafficTest::none));

        // 100 metres from X, started 60 days ago, never ends: older than the default 30 days.
        List<List<Stop>> tours = tours(date -> List.of(
                announcement(at(date.minusDays(60), 8, 0), null, X_LAT + 0.0009, X_LON)));

        assertThat(toursWithX(tours)).isEqualTo(baseline);
    }

    @Test
    void anOpenEndedAnnouncementWithinTheLimitStillBlocks() {
        List<List<Stop>> tours = tours(date -> List.of(
                announcement(at(date.minusDays(10), 8, 0), null, X_LAT + 0.0009, X_LON)));

        assertThat(toursWithX(tours)).isZero();
    }

    @Test
    void theConfiguredAgeLimitIsUsed() {
        NearestPlaceTourPlanner patient = planner(planning -> planning.setMaxOpenEndedAgeDays(90));

        // 60 days old is within a 90 day limit, so it blocks.
        List<List<Stop>> tours = tours(patient, date -> List.of(
                announcement(at(date.minusDays(60), 8, 0), null, X_LAT + 0.0009, X_LON)));

        assertThat(toursWithX(tours)).isZero();
    }

    @Test
    void anOpenEndedAnnouncementWithoutAStartTimeStillBlocks() {
        List<List<Stop>> tours = tours(date -> List.of(
                announcement(null, null, X_LAT + 0.0009, X_LON)));

        assertThat(toursWithX(tours)).isZero();
    }

    @Test
    void theTourIsEmptyWhenEveryPlaceIsBlockedAtTheStart() {
        List<TrafficAnnouncement> everywhere = new ArrayList<>();
        for (Place place : PLACES) {
            everywhere.add(announcement(at(DAY, 8, 0), null, place.lat(), place.lon()));
        }

        assertThat(planner.plan(PLACES, request(DAY, 4), everywhere)).isEmpty();
    }

    @Test
    void duringAnAnnouncementTheTourStillHasNoOverlapsOrRepeats() {
        List<List<Stop>> tours = tours(date -> List.of(
                announcement(at(date, 11, 0), at(date, 12, 0), X_LAT + 0.0009, X_LON)));

        for (List<Stop> tour : tours) {
            assertThat(ids(tour)).doesNotHaveDuplicates();
            for (int i = 1; i < tour.size(); i++) {
                assertThat(tour.get(i).start().isBefore(tour.get(i - 1).end())).isFalse();
            }
        }
    }
}
