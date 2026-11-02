package fi.citytour.planner.controller;

import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.place.City;
import fi.citytour.planner.model.place.PlaceView;
import fi.citytour.planner.model.tour.TourRequest;
import fi.citytour.planner.model.tour.TourResult;
import fi.citytour.planner.service.TourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST controller that exposes the tour planner to the frontend. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;

    /** Generates a tour. */
    @PostMapping("/tour")
    public TourResult generateTour(@Valid @RequestBody TourRequest request) {
        return tourService.generateTour(request);
    }

    /** Lists the cities for which tours can be planned. */
    @GetMapping("/cities")
    public List<City> getCities() {
        return tourService.listCities();
    }

    /** Lists the places available in a city. */
    @GetMapping("/places")
    public List<PlaceView> getPlaces(
            @RequestParam String city,
            @RequestParam(defaultValue = "en") String language) {

        return tourService.listPlaces(city, Language.fromCode(language));
    }
}
