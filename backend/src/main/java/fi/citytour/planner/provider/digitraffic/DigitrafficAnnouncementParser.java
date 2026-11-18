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
import java.util.regex.Pattern;

/**
 * Parses Digitraffic traffic announcements JSON payloads and extracts announcements
 * relevant to a specific city/municipality.
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

            String area = extractArea(matchingAnnouncement, targetCity);
            String language = matchingAnnouncement.path("language").asText(
                    preferredLanguage != null ? preferredLanguage.code() : "fi"
            );
            String type = extractType(matchingAnnouncement, propertiesNode);
            String description = extractDescription(matchingAnnouncement);

            JsonNode timeNode = matchingAnnouncement.path("timeAndDuration");
            OffsetDateTime startTime = parseOffsetDateTime(timeNode.path("startTime").asText(null));
            OffsetDateTime endTime = parseOffsetDateTime(timeNode.path("endTime").asText(null));

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

    private JsonNode findMatchingAnnouncement(
            JsonNode announcementsArray,
            String targetCity,
            Language preferredLanguage) {

        boolean matchesCity = false;
        JsonNode preferredLangNode = null;
        JsonNode fallbackNode = announcementsArray.get(0);

        String preferredCode = preferredLanguage != null ? preferredLanguage.code() : null;

        for (JsonNode annNode : announcementsArray) {
            if (matchesCity(annNode, targetCity)) {
                matchesCity = true;
            }
            if (preferredCode != null && preferredCode.equalsIgnoreCase(annNode.path("language").asText())) {
                preferredLangNode = annNode;
            }
        }

        if (!matchesCity) {
            return null;
        }

        return preferredLangNode != null ? preferredLangNode : fallbackNode;
    }

    private boolean matchesCity(JsonNode annNode, String targetCity) {
        JsonNode roadLocation = annNode.path("locationDetails").path("roadAddressLocation");
        String primaryMuni = roadLocation.path("primaryPoint").path("municipality").asText(null);
        String secondaryMuni = roadLocation.path("secondaryPoint").path("municipality").asText(null);

        if (isCityMatch(primaryMuni, targetCity) || isCityMatch(secondaryMuni, targetCity)) {
            return true;
        }

        String title = annNode.path("title").asText("");
        String locationDesc = annNode.path("location").path("description").asText("");

        return containsCity(title, targetCity) || containsCity(locationDesc, targetCity);
    }

    private boolean isCityMatch(String candidate, String targetCity) {
        return candidate != null && candidate.trim().equalsIgnoreCase(targetCity.trim());
    }

    private boolean containsCity(String text, String targetCity) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String pattern = "(?i).*\\b" + Pattern.quote(targetCity.trim()) + "\\b.*";
        return text.matches(pattern);
    }

    private String extractArea(JsonNode annNode, String fallbackCity) {
        JsonNode roadLocation = annNode.path("locationDetails").path("roadAddressLocation");
        String primaryMuni = roadLocation.path("primaryPoint").path("municipality").asText(null);
        if (primaryMuni != null && !primaryMuni.isBlank()) {
            return primaryMuni.trim();
        }
        String secondaryMuni = roadLocation.path("secondaryPoint").path("municipality").asText(null);
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
