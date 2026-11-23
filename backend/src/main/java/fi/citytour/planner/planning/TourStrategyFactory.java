package fi.citytour.planner.planning;

import fi.citytour.planner.config.PlannerProperties;
import fi.citytour.planner.model.tour.Factor;
import fi.citytour.planner.model.tour.TourRequest;
import fi.citytour.planner.model.weather.HourlyForecast;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Chooses the {@link TourStrategy} that suits the weather and the factors the
 * user allowed. The indoor strategy is used only when the weather factor is
 * enabled and rain is expected during the tour; otherwise the general strategy
 * is used.
 */
@Component
public class TourStrategyFactory {

    private final GeneralTourStrategy generalStrategy;
    private final IndoorTourStrategy indoorStrategy;
    private final double rainThresholdMm;
    private final ZoneId zone;

    /** Creates the factory. */
    public TourStrategyFactory(
            GeneralTourStrategy generalStrategy,
            IndoorTourStrategy indoorStrategy,
            PlannerProperties properties) {
        this.generalStrategy = generalStrategy;
        this.indoorStrategy = indoorStrategy;
        this.rainThresholdMm = properties.getPlanning().getRainThresholdMm();
        this.zone = ZoneId.of(properties.getZone());
    }

    /**
     * Chooses a strategy: indoor if the weather factor is on and rain is
     * expected during the tour, general otherwise.
     */
    public TourStrategy choose(List<HourlyForecast> forecast, TourRequest request) {
        if (request.factors().contains(Factor.WEATHER) && isRainExpected(forecast, request)) {
            return indoorStrategy;
        }
        return generalStrategy;
    }

    /**
     * Returns true if any forecast hour inside the tour window has precipitation
     * above the configured threshold. A forecast value at time T is the precipitation of the hour
     * ending at T, so the hours that count are those after the start up to and including the end.
     * Missing values do not count as rain.
     */
    private boolean isRainExpected(List<HourlyForecast> forecast, TourRequest request) {
        LocalDateTime tourStart = request.date().atTime(request.startTime());
        LocalDateTime tourEnd = tourStart.plusHours(request.durationHours());

        for (HourlyForecast hour : forecast) {
            // The forecast time carries an offset; convert it to the tour's local time
            LocalDateTime hourTime = hour.time().atZoneSameInstant(zone).toLocalDateTime();

            // The value at time T is the rain of the hour before T, so the window is (start, end]
            if (!hourTime.isAfter(tourStart) || hourTime.isAfter(tourEnd)) {
                continue;   // outside the tour window
            }
            if (hour.precipitationAmount() != null && hour.precipitationAmount() > rainThresholdMm) {
                return true;
            }
        }
        return false;
    }
}
