package fi.citytour.planner.provider;

import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;

import java.time.OffsetDateTime;
import java.util.List;

/** Supplies road traffic announcements to the tour service. */
public interface TrafficProvider {

    /** Returns the active traffic announcements for a city. */
    List<TrafficAnnouncement> getAnnouncements(
            String city,
            Language preferredLanguage);

    /**
     * Returns the active traffic announcements for a city relevant to a specific tour time window.
     * Announcements are filtered so that only incidents that start before the tour window ends
     * and either have no scheduled end time (ongoing) or end after the tour window begins are included.
     *
     * @param city the city name or identifier
     * @param preferredLanguage the preferred language for the announcements
     * @param windowStart the start of the tour time window, or null for no start boundary
     * @param windowEnd the end of the tour time window, or null for no end boundary
     * @return active traffic announcements overlapping the given time window
     */
    default List<TrafficAnnouncement> getAnnouncements(
            String city,
            Language preferredLanguage,
            OffsetDateTime windowStart,
            OffsetDateTime windowEnd) {
        return getAnnouncements(city, preferredLanguage);
    }
}
