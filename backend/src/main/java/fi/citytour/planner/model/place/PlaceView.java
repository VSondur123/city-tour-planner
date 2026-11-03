package fi.citytour.planner.model.place;

import fi.citytour.planner.model.common.Language;

/** Represents a place as shown to the user, with its text resolved to one language. */
public record PlaceView(
        String id,
        String name,
        PlaceCategory category,
        boolean indoor,
        double lat,
        double lon,
        int visitMinutes,
        String link
) {

    /** Resolves a place to the requested language. */
    public static PlaceView of(Place place, Language language) {
        return new PlaceView(
                place.id(),
                place.name().resolve(language),
                place.category(),
                place.indoor(),
                place.lat(),
                place.lon(),
                place.visitMinutes(),
                place.link().resolve(language)
        );
    }
}
