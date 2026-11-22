package fi.citytour.planner.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.citytour.planner.config.PlannerProperties;
import fi.citytour.planner.model.place.City;
import fi.citytour.planner.model.place.Place;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonPlaceRepositoryTest {

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    private JsonPlaceRepository repository(String placesFile) {
        PlannerProperties properties = new PlannerProperties();
        properties.setPlacesFile(placesFile);
        return new JsonPlaceRepository(objectMapper, new DefaultResourceLoader(), properties);
    }

    @Test
    void loadsTheCitiesFromThePlacesFile() {
        JsonPlaceRepository repository = repository("classpath:data/places.json");

        assertThat(repository.findCities()).contains(new City("tampere", "Tampere"));
    }

    @Test
    void findsThePlacesOfACityIgnoringCase() {
        JsonPlaceRepository repository = repository("classpath:data/places.json");

        assertThat(repository.findByCity("TAMPERE")).isNotEmpty();
        assertThat(repository.findByCity("tampere")).isEqualTo(repository.findByCity("Tampere"));
    }

    @Test
    void marksEveryPlaceWithItsCityId() {
        JsonPlaceRepository repository = repository("classpath:data/places.json");

        assertThat(repository.findByCity("tampere"))
                .extracting(Place::city)
                .containsOnly("tampere");
    }

    @Test
    void returnsNoPlacesForAnUnknownCity() {
        JsonPlaceRepository repository = repository("classpath:data/places.json");

        assertThat(repository.findByCity("atlantis")).isEmpty();
    }

    @Test
    void shippedPlacesAreUsable() {
        JsonPlaceRepository repository = repository("classpath:data/places.json");

        assertThat(repository.findByCity("tampere")).allSatisfy(place -> {
            assertThat(place.id()).isNotBlank();
            assertThat(place.name().en()).isNotBlank();
            assertThat(place.name().fi()).isNotBlank();
            assertThat(place.visitMinutes()).isPositive();
        });
    }

    @Test
    void failsClearlyWhenThePlacesFileIsMissing() {
        assertThatThrownBy(() -> repository("classpath:data/does-not-exist.json"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does-not-exist.json");
    }
}
