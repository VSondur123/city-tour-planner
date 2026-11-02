package fi.citytour.planner.provider.digitraffic;

import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;
import fi.citytour.planner.provider.TrafficProvider;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapts the Digitraffic road traffic API to {@link TrafficProvider}.
 * TODO (Abdullah): empty placeholder that returns no announcements; request them from
 * Digitraffic, keep those of the city, and convert them to {@link TrafficAnnouncement} objects.
 */
@Component
public class DigitrafficAdapter implements TrafficProvider {

    @Override
    public List<TrafficAnnouncement> getAnnouncements(
            String city,
            Language preferredLanguage) {

        return List.of();
    }
}
