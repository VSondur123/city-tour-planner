package fi.citytour.planner.repository;

import fi.citytour.planner.model.place.City;
import fi.citytour.planner.model.place.Place;

import java.util.List;

/** Supplies the cities and places the application can offer. */
public interface PlaceRepository {

    /** Returns the cities for which place data exists. */
    List<City> findCities();

    /** Returns the places of a city. */
    List<Place> findByCity(String cityId);
}
