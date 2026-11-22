package fi.citytour.planner.model.weather;

import java.time.OffsetDateTime;

/**
 * Represents the weather forecast for one hour.
 *
 * @param time                time of the forecast in local time
 * @param temperature         air temperature in degrees Celsius (°C), or null if missing
 * @param precipitationAmount precipitation amount accumulated over the 1-hour interval in millimeters (mm/h), or null if missing
 * @param cloudCover          cloud cover percentage (0–100%), or null if missing
 * @param wind                wind speed in meters per second (m/s), or null if missing
 */
public record HourlyForecast(
        OffsetDateTime time,
        Double temperature,
        Double precipitationAmount,
        Double cloudCover,
        Double wind
) {}
