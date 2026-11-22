package fi.citytour.planner.controller;

import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;
import fi.citytour.planner.model.weather.HourlyForecast;
import fi.citytour.planner.provider.TrafficProvider;
import fi.citytour.planner.provider.WeatherProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Controller providing test endpoints to directly inspect and verify
 * the external data adapters (FMI Weather and Digitraffic).
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class AdapterTestController {

    private final WeatherProvider weatherProvider;
    private final TrafficProvider trafficProvider;

    /**
     * Directly tests the FMI weather adapter.
     * Example: GET /api/test/weather?city=tampere&date=2026-11-17
     */
    @GetMapping("/weather")
    public List<HourlyForecast> testWeather(
            @RequestParam(defaultValue = "tampere") String city,
            @RequestParam(required = false) String date) {

        LocalDate targetDate = parseDate(date);
        return weatherProvider.getForecast(city, targetDate);
    }

    /**
     * Directly tests the Digitraffic adapter.
     * Example: GET /api/test/traffic?city=tampere&language=en
     */
    @GetMapping("/traffic")
    public List<TrafficAnnouncement> testTraffic(
            @RequestParam(defaultValue = "tampere") String city,
            @RequestParam(defaultValue = "en") String language) {

        Language lang = parseLanguage(language);
        return trafficProvider.getAnnouncements(city, lang);
    }

    /**
     * Tests both adapters in a single combined call.
     * Example: GET /api/test/overview?city=tampere
     */
    @GetMapping("/overview")
    public AdapterTestOverview testOverview(
            @RequestParam(defaultValue = "tampere") String city,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "en") String language) {

        LocalDate targetDate = parseDate(date);
        Language lang = parseLanguage(language);

        List<HourlyForecast> forecast = weatherProvider.getForecast(city, targetDate);
        List<TrafficAnnouncement> announcements = trafficProvider.getAnnouncements(city, lang);

        return new AdapterTestOverview(
                city,
                targetDate,
                forecast.size(),
                forecast,
                announcements.size(),
                announcements
        );
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            return LocalDate.now();
        }
    }

    private Language parseLanguage(String langStr) {
        try {
            return Language.fromCode(langStr);
        } catch (fi.citytour.planner.model.common.UnsupportedLanguageException e) {
            return Language.EN;
        }
    }

    /** Summary response payload for combined testing. */
    public record AdapterTestOverview(
            String city,
            LocalDate date,
            int weatherCount,
            List<HourlyForecast> weather,
            int trafficCount,
            List<TrafficAnnouncement> traffic
    ) {}
}
