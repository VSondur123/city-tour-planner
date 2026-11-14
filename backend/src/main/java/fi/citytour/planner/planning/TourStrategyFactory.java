package fi.citytour.planner.planning;

import fi.citytour.planner.model.tour.Factor;
import fi.citytour.planner.model.tour.TourRequest;
import fi.citytour.planner.model.weather.HourlyForecast;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Chooses the {@link TourStrategy} that suits the weather and the factors the
 * user allowed.
 * The indoor strategy is used only when the weather factor is enabled and rain
 * is expected;
 * otherwise the general strategy is used.
 */
@Component
public class TourStrategyFactory {

    private final GeneralTourStrategy generalStrategy;
    private final IndoorTourStrategy indoorStrategy;

    // Precipitation is water falling from the sky (rain, snow, sleet or hail),
    // measured in millimetres.
    // Precipitation (mm per hour) above which we treat the weather as rainy.
    private static final double RAIN_THRESHOLD_MM = 0.5;

    /** Creates the factory. */
    public TourStrategyFactory(GeneralTourStrategy generalStrategy, IndoorTourStrategy indoorStrategy) {
        this.generalStrategy = generalStrategy;
        this.indoorStrategy = indoorStrategy;
    }

    /**
     * Returns true if any forecast hour has precipitation above the rain threshold.
     */
    /**
     * Returns true if any forecast hour inside the tour window has precipitation
     * above the threshold.
     */
    private boolean isRainExpected(List<HourlyForecast> forecast, TourRequest request) {

        // Tour window: from the chosen start time to start + duration
        LocalDateTime tourStart = request.date().atTime(request.startTime());
        LocalDateTime tourEnd = tourStart.plusHours(request.durationHours());

        for (HourlyForecast hour : forecast) {
            LocalDateTime hourTime = hour.time().toLocalDateTime();

            // Ignore hours before the tour starts or at/after it ends
            if (hourTime.isBefore(tourStart) || !hourTime.isBefore(tourEnd)) {
                continue;
            }
            // The precipitation value can be missing, so check for null first
            if (hour.precipitationAmount() != null && hour.precipitationAmount() > RAIN_THRESHOLD_MM) {
                return true;
            }
        }
        return false;
    }

    /**
     * Chooses a strategy. The forecast is empty when the weather factor is off, and
     * the factors
     * check keeps the decision explicit.
     */
    public TourStrategy choose(
            List<HourlyForecast> forecast,
            TourRequest request) {

        // Rain expected during the tour and weather allowed: prefer indoor places
        if (request.factors().contains(Factor.WEATHER) && isRainExpected(forecast, request)) {
            return indoorStrategy;
        }
        // Default: no weather restriction
        return generalStrategy;
    }

}
