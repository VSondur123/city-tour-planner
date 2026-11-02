package fi.citytour.planner.model.traffic;

import java.time.OffsetDateTime;

/** Represents a road traffic announcement. */
public record TrafficAnnouncement(
        String type,
        String description,
        String area,
        String language,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        GeoShape geometry
) {}
