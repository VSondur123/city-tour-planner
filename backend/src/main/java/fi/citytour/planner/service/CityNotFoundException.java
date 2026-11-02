package fi.citytour.planner.service;

/** Signals that a request named a city for which the application has no place data. */
public class CityNotFoundException extends RuntimeException {

    /** Creates the exception for an unknown city. */
    public CityNotFoundException(String cityId) {
        super("Unknown city: " + cityId);
    }
}
