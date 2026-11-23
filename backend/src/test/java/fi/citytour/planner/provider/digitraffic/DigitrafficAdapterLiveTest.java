package fi.citytour.planner.provider.digitraffic;

import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Tag("integration")
class DigitrafficAdapterLiveTest {

    @Autowired
    private DigitrafficAdapter digitrafficAdapter;

    @Test
    void retrievesLiveAnnouncementsFromDigitraffic() {
        List<TrafficAnnouncement> tampereAnnouncements = digitrafficAdapter.getAnnouncements("tampere", Language.FI);

        System.out.println("Live Digitraffic announcements count for Tampere: " + tampereAnnouncements.size());
        for (TrafficAnnouncement ann : tampereAnnouncements) {
            System.out.println("  [" + ann.type() + "] " + ann.description() + " (Area: " + ann.area() + ", Lang: " + ann.language() + ")");
            System.out.println("    Paths count: " + ann.geometry().paths().size() + ", Start: " + ann.startTime());
        }

        // The call should succeed without throwing exceptions, and if announcements exist, check required fields
        for (TrafficAnnouncement ann : tampereAnnouncements) {
            assertThat(ann.type()).isNotBlank();
            assertThat(ann.description()).isNotBlank();
            assertThat(ann.area()).isNotBlank();
        }
    }
}
