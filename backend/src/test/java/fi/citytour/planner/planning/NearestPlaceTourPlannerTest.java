package fi.citytour.planner.planning;

import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.place.Place;
import fi.citytour.planner.model.tour.Factor;
import fi.citytour.planner.model.tour.Stop;
import fi.citytour.planner.model.tour.TourRequest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fi.citytour.planner.planning.PlanningFixtures.ids;
import static fi.citytour.planner.planning.PlanningFixtures.place;
import static fi.citytour.planner.planning.PlanningFixtures.planner;
import static fi.citytour.planner.planning.PlanningFixtures.request;
import static org.assertj.core.api.Assertions.assertThat;

class NearestPlaceTourPlannerTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 23);

    /** Six places a few kilometres apart, 30 minutes each. */
    private static final List<Place> PLACES = List.of(
            place("A", true, 60.1700, 24.9400, 30),
            place("B", true, 60.1750, 24.9450, 30),
            place("C", false, 60.1650, 24.9500, 30),
            place("D", true, 60.1780, 24.9300, 30),
            place("E", false, 60.1620, 24.9380, 30),
            place("F", true, 60.1710, 24.9550, 30));

    private final NearestPlaceTourPlanner planner = planner();

    private List<Stop> plan(List<Place> places, TourRequest request) {
        return planner.plan(places, request, List.of());
    }

    // ---- repeatable (seeded) ----

    @Test
    void sameRequestGivesTheSameTour() {
        TourRequest request = request(DAY, 4);

        List<String> first = ids(plan(PLACES, request));

        for (int i = 0; i < 20; i++) {
            assertThat(ids(plan(PLACES, request))).isEqualTo(first);
        }
    }

    @Test
    void languageAndFactorsDoNotChangeTheTour() {
        List<String> english = ids(plan(PLACES,
                request("tampere", DAY, LocalTime.of(10, 0), 4, Language.EN, Set.of(Factor.WEATHER, Factor.TRAFFIC))));
        List<String> finnish = ids(plan(PLACES,
                request("tampere", DAY, LocalTime.of(10, 0), 4, Language.FI, Set.of(Factor.WEATHER, Factor.TRAFFIC))));
        List<String> noFactors = ids(plan(PLACES,
                request("tampere", DAY, LocalTime.of(10, 0), 4, Language.EN, Set.of())));

        assertThat(finnish).isEqualTo(english);
        assertThat(noFactors).isEqualTo(english);
    }

    @Test
    void theCityNameCaseDoesNotChangeTheTour() {
        List<String> lower = ids(plan(PLACES,
                request("tampere", DAY, LocalTime.of(10, 0), 4, Language.EN, Set.of())));
        List<String> upper = ids(plan(PLACES,
                request("TAMPERE", DAY, LocalTime.of(10, 0), 4, Language.EN, Set.of())));

        assertThat(upper).isEqualTo(lower);
    }

    @Test
    void differentRequestsStartWithDifferentPlaces() {
        Set<String> firstStops = new HashSet<>();
        for (int day = 0; day < 30; day++) {
            firstStops.add(ids(plan(PLACES, request(DAY.plusDays(day), 4))).get(0));
        }

        assertThat(firstStops.size()).isGreaterThan(2);
    }

    // ---- shape of a tour ----

    @Test
    void everyTourStartsAtTheTourStartAndStaysInsideTheTourWindow() {
        for (int day = 0; day < 40; day++) {
            for (int hours = 1; hours <= 8; hours++) {
                LocalDate date = DAY.plusDays(day);
                OffsetDateTime tourStart = date.atTime(10, 0).atOffset(PlanningFixtures.WINTER);
                OffsetDateTime tourEnd = tourStart.plusHours(hours);

                List<Stop> stops = plan(PLACES, request(date, hours));

                assertThat(stops).isNotEmpty();
                assertThat(stops.get(0).start().toInstant()).isEqualTo(tourStart.toInstant());

                OffsetDateTime previousEnd = null;
                Set<String> seen = new HashSet<>();
                for (Stop stop : stops) {
                    assertThat(seen.add(stop.place().id())).as("no place is visited twice").isTrue();
                    assertThat(Duration.between(stop.start(), stop.end()).toMinutes())
                            .isEqualTo(stop.place().visitMinutes());
                    assertThat(stop.end().isAfter(tourEnd)).as("ends inside the tour").isFalse();
                    if (previousEnd != null) {
                        assertThat(stop.start().isBefore(previousEnd)).as("no overlap").isFalse();
                    }
                    previousEnd = stop.end();
                }
            }
        }
    }

    @Test
    void nextStopIsTheNearestPlace() {
        // P1 and P2 are close together, P3 is further away.
        Place p1 = place("P1", true, 61.0, 24.00, 30);
        Place p2 = place("P2", true, 61.0, 24.01, 30);
        Place p3 = place("P3", true, 61.0, 24.05, 30);
        // Whichever place comes first, the nearest of the others must come second.
        Map<String, String> nearest = Map.of("P1", "P2", "P2", "P1", "P3", "P2");

        for (int day = 0; day < 40; day++) {
            List<String> tour = ids(plan(List.of(p1, p2, p3), request(DAY.plusDays(day), 6)));

            assertThat(tour).hasSize(3);
            assertThat(tour.get(1)).isEqualTo(nearest.get(tour.get(0)));
        }
    }

    @Test
    void walkingTimeBetweenStopsFollowsTheConfiguredSpeed() {
        // About 2.5 km apart on the same latitude.
        Place west = place("W", true, 60.0, 24.00000, 30);
        Place east = place("E", true, 60.0, 24.04497, 30);

        List<Stop> walking = planner().plan(List.of(west, east), request(DAY, 4), List.of());
        List<Stop> fast = planner(planning -> planning.setWalkingSpeedKmh(10.0))
                .plan(List.of(west, east), request(DAY, 4), List.of());

        // 2.5 km at 5 km/h is 30 minutes (rounded up), at 10 km/h it is 15.
        assertThat(gapMinutes(walking)).isBetween(30L, 31L);
        assertThat(gapMinutes(fast)).isBetween(15L, 16L);
    }

    private static long gapMinutes(List<Stop> stops) {
        assertThat(stops).hasSize(2);
        return Duration.between(stops.get(0).end(), stops.get(1).start()).toMinutes();
    }

    // ---- what fits ----

    @Test
    void aPlaceLongerThanTheTourIsLeftOut() {
        Place shortVisit = place("SHORT", true, 60.17, 24.94, 30);
        Place longVisit = place("LONG", true, 60.1701, 24.9401, 90);

        for (int day = 0; day < 20; day++) {
            assertThat(ids(plan(List.of(shortVisit, longVisit), request(DAY.plusDays(day), 1))))
                    .containsExactly("SHORT");
        }
    }

    @Test
    void aVisitExactlyAsLongAsTheTourFits() {
        Place exact = place("EXACT", true, 60.17, 24.94, 60);

        List<Stop> stops = plan(List.of(exact), request(DAY, 1));

        assertThat(stops).hasSize(1);
        assertThat(Duration.between(stops.get(0).start(), stops.get(0).end())).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void theTourIsEmptyWhenNoPlaceFits() {
        Place tooLong = place("LONG", true, 60.17, 24.94, 120);

        assertThat(plan(List.of(tooLong), request(DAY, 1))).isEmpty();
    }

    @Test
    void theTourIsEmptyWithoutPlaces() {
        assertThat(plan(List.of(), request(DAY, 4))).isEmpty();
    }

    @Test
    void theTourEndsWhenTheNextPlaceCannotBeReachedInTime() {
        // The two places are about 5 km apart: about 60 minutes of walking between them.
        // In a 1 hour tour, a 30 minute visit leaves 30 minutes, so the second place never fits.
        Place first = place("ONE", true, 60.0, 24.0, 30);
        Place second = place("TWO", true, 60.0, 24.0899, 30);

        for (int day = 0; day < 20; day++) {
            assertThat(plan(List.of(first, second), request(DAY.plusDays(day), 1))).hasSize(1);
        }
    }

    // ---- time zone ----

    @Test
    void stopsAreInFinnishTimeInWinterAndSummer() {
        List<Stop> winter = plan(PLACES, request(LocalDate.of(2026, 12, 1), 3));
        List<Stop> summer = plan(PLACES, request(LocalDate.of(2026, 7, 1), 3));

        assertThat(winter.get(0).start().getOffset()).isEqualTo(ZoneOffset.ofHours(2));
        assertThat(summer.get(0).start().getOffset()).isEqualTo(ZoneOffset.ofHours(3));
    }
}
