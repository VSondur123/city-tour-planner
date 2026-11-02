package fi.citytour.planner.provider.fmi;

import fi.citytour.planner.model.weather.HourlyForecast;
import fi.citytour.planner.provider.WeatherProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Adapts the FMI weather API to {@link WeatherProvider}.
 * TODO (Abdullah): empty placeholder that returns no forecast; request the forecast from FMI
 * and convert it to {@link HourlyForecast} objects.
 */
@Component
public class FmiWeatherAdapter implements WeatherProvider {

    @Override
    public List<HourlyForecast> getForecast(String city, LocalDate date) {
        return List.of();
    }
}
