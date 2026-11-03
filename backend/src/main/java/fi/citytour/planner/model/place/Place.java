package fi.citytour.planner.model.place;

import fi.citytour.planner.model.common.LocalizedText;

/**
 * A place that can become a stop on a tour, with its text in every language.
 * {@code indoor} and {@code visitMinutes} are curated by the team, not taken from an API.
 */
public record Place(
        String id,
        LocalizedText name,
        String city,
        PlaceCategory category,
        boolean indoor,
        double lat,
        double lon,
        int visitMinutes,
        LocalizedText link
) {

    /** Returns a copy of this place that belongs to the given city. */
    public Place withCity(String cityId) {
        return new Place(id, name, cityId, category, indoor,
                lat, lon, visitMinutes, link);
    }
}
