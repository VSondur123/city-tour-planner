package fi.citytour.planner.model.weather;

import java.time.OffsetDateTime;

/** Represents the weather forecast for one hour. */
public record HourlyForecast(
        OffsetDateTime time,
        Double temperature,
        Double precipitationAmount,
        Double cloudCover,
        Double wind
) {}
