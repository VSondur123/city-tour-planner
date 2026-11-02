package fi.citytour.planner.provider;

import fi.citytour.planner.model.weather.HourlyForecast;

import java.time.LocalDate;
import java.util.List;

/** Supplies weather forecasts to the tour service. */
public interface WeatherProvider {

    /** Returns the hourly forecast for a city on a date. */
    List<HourlyForecast> getForecast(String city, LocalDate date);
}
