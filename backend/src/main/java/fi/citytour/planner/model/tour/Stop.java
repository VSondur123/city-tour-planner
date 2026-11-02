package fi.citytour.planner.model.tour;

import fi.citytour.planner.model.place.Place;

import java.time.OffsetDateTime;

/** Represents one planned visit on a tour. */
public record Stop(
        Place place,
        OffsetDateTime start,
        OffsetDateTime end
) {}
