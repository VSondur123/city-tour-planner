package fi.citytour.planner.demo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.citytour.planner.model.common.LocalizedText;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;
import fi.citytour.planner.model.weather.HourlyForecast;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Fixed adapter and strategy output for smoke tests, read from
 * {@code demo/adapter-and-strategy-output.json}. Active only with the {@code demo} profile.
 */
@Component
@Profile("demo")
public class DemoData {

    private final DemoFile file;

    public DemoData(ObjectMapper objectMapper) {
        try (InputStream in = new ClassPathResource("demo/adapter-and-strategy-output.json")
                .getInputStream()) {
            this.file = objectMapper.readerFor(DemoFile.class)
                    .without(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                    .readValue(in);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read demo data", e);
        }
    }

    public List<HourlyForecast> forecast() {
        return file.forecast();
    }

    public List<TrafficAnnouncement> announcements() {
        return file.announcements();
    }

    public List<DemoStop> stops() {
        return file.stops();
    }

    public LocalizedText explanation() {
        return file.explanation();
    }

    /** A planned visit, referring to a place of the repository by id. */
    public record DemoStop(String placeId, OffsetDateTime start, OffsetDateTime end) {}

    private record DemoFile(
            List<HourlyForecast> forecast,
            List<TrafficAnnouncement> announcements,
            List<DemoStop> stops,
            LocalizedText explanation) {}
}
