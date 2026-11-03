package fi.citytour.planner.model.tour;

import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.place.PlaceView;

import java.time.OffsetDateTime;

/** Represents one planned visit as shown to the user. */
public record StopView(
        PlaceView place,
        OffsetDateTime start,
        OffsetDateTime end
) {

    /** Resolves a stop to the requested language. */
    public static StopView of(Stop stop, Language language) {
        return new StopView(
                PlaceView.of(stop.place(), language),
                stop.start(),
                stop.end()
        );
    }
}
