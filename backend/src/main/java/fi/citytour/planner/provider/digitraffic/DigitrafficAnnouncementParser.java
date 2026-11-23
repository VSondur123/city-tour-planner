package fi.citytour.planner.provider.digitraffic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.traffic.Coordinate;
import fi.citytour.planner.model.traffic.GeoShape;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;
import fi.citytour.planner.provider.ProviderException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Parses Digitraffic traffic announcements JSON payloads and extracts announcements
 * relevant to a specific city/municipality and optional tour time window.
 */
@Component
@RequiredArgsConstructor
public class DigitrafficAnnouncementParser {

    private final ObjectMapper objectMapper;

    /**
     * Parses the Digitraffic GeoJSON response and returns active announcements matching the city.
     *
     * @param json              the response payload from Digitraffic
     * @param targetCity        the city identifier/name to filter by
     * @param preferredLanguage the language preference of the user
     * @return a list of parsed and filtered {@link TrafficAnnouncement} records
     * @throws ProviderException if the payload is malformed
     */
    public List<TrafficAnnouncement> parse(String json, String targetCity, Language preferredLanguage) {
        return parse(json, targetCity, preferredLanguage, null, null);
    }

    /**
     * Parses the Digitraffic GeoJSON response and returns active announcements matching the city
     * and overlapping with the requested tour time window.
     *
     * @param json              the response payload from Digitraffic
     * @param targetCity        the city identifier/name to filter by
     * @param preferredLanguage the language preference of the user
     * @param windowStart       the start of the tour window, or null if no time filter
     * @param windowEnd         the end of the tour window, or null if no time filter
     * @return a list of parsed and filtered {@link TrafficAnnouncement} records
     * @throws ProviderException if the payload is malformed
     */
    public List<TrafficAnnouncement> parse(
            String json,
            String targetCity,
            Language preferredLanguage,
            OffsetDateTime windowStart,
            OffsetDateTime windowEnd) {

        if (json == null || json.isBlank() || targetCity == null || targetCity.isBlank()) {
            return List.of();
        }

        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new ProviderException("Digitraffic", "Failed to parse Digitraffic JSON: " + e.getMessage(), e);
        }

        JsonNode featuresNode = rootNode.path("features");
        if (!featuresNode.isArray()) {
            return List.of();
        }

        List<TrafficAnnouncement> announcements = new ArrayList<>();

        for (JsonNode featureNode : featuresNode) {
            JsonNode propertiesNode = featureNode.path("properties");
            JsonNode announcementsArray = propertiesNode.path("announcements");
            if (!announcementsArray.isArray() || announcementsArray.isEmpty()) {
                continue;
            }

            JsonNode matchingAnnouncement = findMatchingAnnouncement(announcementsArray, targetCity, preferredLanguage);
            if (matchingAnnouncement == null) {
                continue;
            }

            JsonNode timeNode = matchingAnnouncement.path("timeAndDuration");
            OffsetDateTime startTime = parseOffsetDateTime(timeNode.path("startTime").asText(null));
            OffsetDateTime endTime = parseOffsetDateTime(timeNode.path("endTime").asText(null));

            // Time-window filter: announcement must start before window end, and end after window start (or have no end time)
            if (!isWithinTimeWindow(startTime, endTime, windowStart, windowEnd)) {
                continue;
            }

            String area = extractArea(matchingAnnouncement, targetCity);
            String rawLang = matchingAnnouncement.path("language").asText(
                    preferredLanguage != null ? preferredLanguage.code() : "fi"
            );
            String language = rawLang != null ? rawLang.trim().toLowerCase(Locale.ROOT) : "fi";
            String type = extractType(matchingAnnouncement, propertiesNode);
            String description = extractDescription(matchingAnnouncement);

            GeoShape geometry = parseGeometry(featureNode.path("geometry"));

            announcements.add(new TrafficAnnouncement(
                    type,
                    description,
                    area,
                    language,
                    startTime,
                    endTime,
                    geometry
            ));
        }

        return List.copyOf(announcements);
    }

    /**
     * Selects the language variant strictly from the announcements that matched the city.
     */
    private JsonNode findMatchingAnnouncement(
            JsonNode announcementsArray,
            String targetCity,
            Language preferredLanguage) {

        List<JsonNode> cityMatchingAnnouncements = new ArrayList<>();
        for (JsonNode annNode : announcementsArray) {
            if (matchesCity(annNode, targetCity)) {
                cityMatchingAnnouncements.add(annNode);
            }
        }

        if (cityMatchingAnnouncements.isEmpty()) {
            return null;
        }

        String preferredCode = preferredLanguage != null ? preferredLanguage.code() : null;
        if (preferredCode != null) {
            for (JsonNode annNode : cityMatchingAnnouncements) {
                if (preferredCode.equalsIgnoreCase(annNode.path("language").asText())) {
                    return annNode;
                }
            }
        }

        return cityMatchingAnnouncements.get(0);
    }

    /**
     * Matches strictly against municipality fields (primaryPoint, secondaryPoint, and roadWorkPhases)
     * to avoid false positives where road titles or general descriptions merely mention the city.
     */
    private boolean matchesCity(JsonNode annNode, String targetCity) {
        JsonNode roadLocation = annNode.path("locationDetails").path("roadAddressLocation");
        String primaryMuni = roadLocation.path("primaryPoint").path("municipality").asText(null);
        String secondaryMuni = roadLocation.path("secondaryPoint").path("municipality").asText(null);

        if (isCityMatch(primaryMuni, targetCity) || isCityMatch(secondaryMuni, targetCity)) {
            return true;
        }

        JsonNode phases = annNode.path("roadWorkPhases");
        if (phases.isArray()) {
            for (JsonNode phase : phases) {
                JsonNode phaseLocation = phase.path("locationDetails").path("roadAddressLocation");
                String phasePrimary = phaseLocation.path("primaryPoint").path("municipality").asText(null);
                String phaseSecondary = phaseLocation.path("secondaryPoint").path("municipality").asText(null);
                if (isCityMatch(phasePrimary, targetCity) || isCityMatch(phaseSecondary, targetCity)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isCityMatch(String candidate, String targetCity) {
        return candidate != null && candidate.trim().equalsIgnoreCase(targetCity.trim());
    }

    /**
     * Checks whether an announcement overlaps with the requested tour time window.
     * <p>
     * Rationale:
     * Digitraffic long-term roadworks and maintenance notices often carry an initial start date
     * and either a future end date or no end date at all (ongoing indefinitely until updated).
     * To ensure only relevant events are displayed:
     * <ul>
     *   <li>The announcement must begin before the tour window ends (start < windowEnd, or null).
     *       This excludes future events that have not yet occurred.</li>
     *   <li>The announcement must end after the tour window begins (end > windowStart), or have no
     *       end time (null), indicating ongoing roadworks active during the tour.
     *       This excludes stale events that completed before the tour.</li>
     * </ul>
     */
    private boolean isWithinTimeWindow(
            OffsetDateTime startTime,
            OffsetDateTime endTime,
            OffsetDateTime windowStart,
            OffsetDateTime windowEnd) {

        if (windowStart == null || windowEnd == null) {
            return true;
        }

        boolean startsBeforeWindowEnds = startTime == null || startTime.isBefore(windowEnd);
        boolean endsAfterWindowStarts = endTime == null || endTime.isAfter(windowStart);

        return startsBeforeWindowEnds && endsAfterWindowStarts;
    }

    private String extractArea(JsonNode annNode, String fallbackCity) {
        JsonNode roadLocation = annNode.path("locationDetails").path("roadAddressLocation");
        String primaryMuni = roadLocation.path("primaryPoint").path("municipality").asText(null);
        String secondaryMuni = roadLocation.path("secondaryPoint").path("municipality").asText(null);

        if (isCityMatch(primaryMuni, fallbackCity)) {
            return primaryMuni.trim();
        }
        if (isCityMatch(secondaryMuni, fallbackCity)) {
            return secondaryMuni.trim();
        }

        JsonNode phases = annNode.path("roadWorkPhases");
        if (phases.isArray()) {
            for (JsonNode phase : phases) {
                JsonNode phaseLocation = phase.path("locationDetails").path("roadAddressLocation");
                String phasePrimary = phaseLocation.path("primaryPoint").path("municipality").asText(null);
                String phaseSecondary = phaseLocation.path("secondaryPoint").path("municipality").asText(null);
                if (isCityMatch(phasePrimary, fallbackCity)) {
                    return phasePrimary.trim();
                }
                if (isCityMatch(phaseSecondary, fallbackCity)) {
                    return phaseSecondary.trim();
                }
            }
        }

        if (primaryMuni != null && !primaryMuni.isBlank()) {
            return primaryMuni.trim();
        }
        if (secondaryMuni != null && !secondaryMuni.isBlank()) {
            return secondaryMuni.trim();
        }
        return fallbackCity;
    }

    private String extractType(JsonNode annNode, JsonNode propertiesNode) {
        JsonNode features = annNode.path("features");
        if (features.isArray() && !features.isEmpty()) {
            String featureName = features.get(0).path("name").asText(null);
            if (featureName != null && !featureName.isBlank()) {
                return featureName.trim();
            }
        }

        String announcementType = propertiesNode.path("trafficAnnouncementType").asText(null);
        if (announcementType != null && !announcementType.isBlank()) {
            return announcementType.trim();
        }

        String situationType = propertiesNode.path("situationType").asText(null);
        if (situationType != null && !situationType.isBlank()) {
            return situationType.trim();
        }

        return "general";
    }

    private String extractDescription(JsonNode annNode) {
        String title = annNode.path("title").asText(null);
        if (title != null && !title.isBlank()) {
            return title.trim();
        }

        String comment = annNode.path("comment").asText(null);
        if (comment != null && !comment.isBlank()) {
            return comment.trim();
        }

        return annNode.path("location").path("description").asText("").trim();
    }

    private GeoShape parseGeometry(JsonNode geomNode) {
        if (geomNode == null || geomNode.isNull() || geomNode.isMissingNode()) {
            return new GeoShape(List.of());
        }

        String type = geomNode.path("type").asText("");
        JsonNode coords = geomNode.path("coordinates");
        if (!coords.isArray() || coords.isEmpty()) {
            return new GeoShape(List.of());
        }

        List<List<Coordinate>> paths = new ArrayList<>();

        if ("Point".equalsIgnoreCase(type)) {
            if (coords.size() >= 2) {
                paths.add(List.of(new Coordinate(coords.get(1).asDouble(), coords.get(0).asDouble())));
            }
        } else if ("LineString".equalsIgnoreCase(type)) {
            List<Coordinate> path = parseCoordArray(coords);
            if (!path.isEmpty()) {
                paths.add(path);
            }
        } else if ("MultiLineString".equalsIgnoreCase(type) || "Polygon".equalsIgnoreCase(type)) {
            for (JsonNode line : coords) {
                if (line.isArray()) {
                    List<Coordinate> path = parseCoordArray(line);
                    if (!path.isEmpty()) {
                        paths.add(path);
                    }
                }
            }
        } else if ("MultiPolygon".equalsIgnoreCase(type)) {
            for (JsonNode polygon : coords) {
                if (polygon.isArray()) {
                    for (JsonNode ring : polygon) {
                        if (ring.isArray()) {
                            List<Coordinate> path = parseCoordArray(ring);
                            if (!path.isEmpty()) {
                                paths.add(path);
                            }
                        }
                    }
                }
            }
        }

        return new GeoShape(List.copyOf(paths));
    }

    private List<Coordinate> parseCoordArray(JsonNode coordArray) {
        List<Coordinate> coords = new ArrayList<>();
        for (JsonNode pt : coordArray) {
            if (pt.isArray() && pt.size() >= 2) {
                // GeoJSON format is [longitude, latitude]
                double lon = pt.get(0).asDouble();
                double lat = pt.get(1).asDouble();
                coords.add(new Coordinate(lat, lon));
            }
        }
        return coords;
    }

    private OffsetDateTime parseOffsetDateTime(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(text);
        } catch (DateTimeParseException e) {
            try {
                return Instant.parse(text).atOffset(ZoneOffset.UTC);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }
}
