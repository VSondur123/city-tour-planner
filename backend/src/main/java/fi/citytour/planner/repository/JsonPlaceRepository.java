package fi.citytour.planner.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.citytour.planner.config.PlannerProperties;
import fi.citytour.planner.model.place.City;
import fi.citytour.planner.model.place.Place;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Supplies the cities and places from the JSON map (city name to places) set by {@code planner.places-file}. */
@Component
public class JsonPlaceRepository implements PlaceRepository {

    private final List<City> cities;
    private final Map<String, List<Place>> placesByCity = new LinkedHashMap<>();

    public JsonPlaceRepository(
            ObjectMapper objectMapper,
            ResourceLoader resourceLoader,
            PlannerProperties properties) {

        Resource resource = resourceLoader.getResource(properties.getPlacesFile());
        try (InputStream in = resource.getInputStream()) {
            Map<String, List<Place>> file = objectMapper.readValue(in, new TypeReference<>() {});
            file.forEach((name, places) -> placesByCity.put(
                    cityId(name),
                    places.stream().map(place -> place.withCity(cityId(name))).toList()));
            this.cities = file.keySet().stream()
                    .map(name -> new City(cityId(name), name))
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot read places file: " + properties.getPlacesFile(), e);
        }
    }

    @Override
    public List<City> findCities() {
        return cities;
    }

    @Override
    public List<Place> findByCity(String cityId) {
        return placesByCity.getOrDefault(cityId(cityId), List.of());
    }

    private static String cityId(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
