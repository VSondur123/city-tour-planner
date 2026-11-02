package fi.citytour.planner.model.tour;

/** Describes the outcome of retrieving data from an external provider. */
public enum DataStatus {

    /** The provider responded and returned data. */
    AVAILABLE,

    /** The provider responded, but there was nothing to return. */
    EMPTY,

    /** The provider could not be reached or its response could not be read. */
    UNAVAILABLE
}
