package fi.citytour.planner.controller;

import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.place.City;
import fi.citytour.planner.model.place.PlaceCategory;
import fi.citytour.planner.model.place.PlaceView;
import fi.citytour.planner.model.tour.DataStatus;
import fi.citytour.planner.model.tour.Factor;
import fi.citytour.planner.model.tour.TourResult;
import fi.citytour.planner.service.CityNotFoundException;
import fi.citytour.planner.service.PastDateException;
import fi.citytour.planner.service.TourService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TourControllerTest {

    private static final String VALID_BODY = """
            {"city":"tampere","date":"2026-11-21","startTime":"10:00","durationHours":4}""";

    @Mock
    private TourService tourService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new TourController(tourService))
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .build();
    }

    private ResultActions postTour(String body) throws Exception {
        return mockMvc.perform(
                post("/api/tour").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void generatesATourForAValidRequest() throws Exception {
        when(tourService.generateTour(any())).thenReturn(new TourResult(
                Language.EN, Set.of(Factor.WEATHER), List.of(), List.of(), List.of(),
                DataStatus.EMPTY, DataStatus.UNAVAILABLE, null, "why"));

        postTour(VALID_BODY)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.language").value("en"))
                .andExpect(jsonPath("$.weatherStatus").value("EMPTY"))
                .andExpect(jsonPath("$.trafficStatus").value("UNAVAILABLE"))
                .andExpect(jsonPath("$.explanation").value("why"));
    }

    @Test
    void rejectsADurationAboveTwelveHours() throws Exception {
        postTour("""
                {"city":"tampere","date":"2026-11-21","startTime":"10:00","durationHours":13}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("durationHours"))
                .andExpect(jsonPath("$.errors[0].code").value("MAX"));

        verifyNoInteractions(tourService);
    }

    @Test
    void rejectsADurationBelowOneHour() throws Exception {
        postTour("""
                {"city":"tampere","date":"2026-11-21","startTime":"10:00","durationHours":0}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].code").value("MIN"));
    }

    @Test
    void rejectsAMissingCity() throws Exception {
        postTour("""
                {"date":"2026-11-21","startTime":"10:00","durationHours":4}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("city"))
                .andExpect(jsonPath("$.errors[0].code").value("REQUIRED"));
    }

    @Test
    void rejectsAnUnsupportedLanguage() throws Exception {
        postTour("""
                {"city":"tampere","date":"2026-11-21","startTime":"10:00","durationHours":4,"language":"sv"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_LANGUAGE"))
                .andExpect(jsonPath("$.errors[0].field").value("language"));
    }

    @Test
    void rejectsAMalformedBody() throws Exception {
        postTour("{not json")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void rejectsAnInvalidDate() throws Exception {
        postTour("""
                {"city":"tampere","date":"garbage","startTime":"10:00","durationHours":4}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void answers404ForAnUnknownCity() throws Exception {
        when(tourService.generateTour(any())).thenThrow(new CityNotFoundException("atlantis"));

        postTour(VALID_BODY)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("CITY_NOT_FOUND"))
                .andExpect(jsonPath("$.errors[0].field").value("city"));
    }

    @Test
    void answers400ForADateInThePast() throws Exception {
        when(tourService.generateTour(any())).thenThrow(new PastDateException());

        postTour(VALID_BODY)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("date"))
                .andExpect(jsonPath("$.errors[0].code").value("PAST"));
    }

    @Test
    void listsTheCities() throws Exception {
        when(tourService.listCities()).thenReturn(List.of(new City("tampere", "Tampere")));

        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("tampere"))
                .andExpect(jsonPath("$[0].name").value("Tampere"));
    }

    @Test
    void listsPlacesInTheRequestedLanguage() throws Exception {
        when(tourService.listPlaces("tampere", Language.FI)).thenReturn(List.of(new PlaceView(
                "p1", "Tuomiokirkko", PlaceCategory.LANDMARK, true, 61.5, 23.77, 30, "https://x")));

        mockMvc.perform(get("/api/places").param("city", "tampere").param("language", "fi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Tuomiokirkko"));
    }

    @Test
    void placesDefaultToEnglish() throws Exception {
        when(tourService.listPlaces("tampere", Language.EN)).thenReturn(List.of());

        mockMvc.perform(get("/api/places").param("city", "tampere")).andExpect(status().isOk());

        verify(tourService).listPlaces("tampere", Language.EN);
    }

    @Test
    void placesRequireACity() throws Exception {
        mockMvc.perform(get("/api/places"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("city"))
                .andExpect(jsonPath("$.errors[0].code").value("REQUIRED"));
    }

    @Test
    void placesRejectAnUnsupportedLanguage() throws Exception {
        mockMvc.perform(get("/api/places").param("city", "tampere").param("language", "sv"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_LANGUAGE"));
    }
}
