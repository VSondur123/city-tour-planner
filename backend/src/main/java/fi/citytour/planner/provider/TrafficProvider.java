package fi.citytour.planner.provider;

import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;

import java.util.List;

/** Supplies road traffic announcements to the tour service. */
public interface TrafficProvider {

    /** Returns the active traffic announcements for a city. */
    List<TrafficAnnouncement> getAnnouncements(
            String city,
            Language preferredLanguage);
}
