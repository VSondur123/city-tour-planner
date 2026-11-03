package fi.citytour.planner.planning;

import fi.citytour.planner.model.tour.Factor;
import fi.citytour.planner.model.weather.HourlyForecast;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Chooses the {@link TourStrategy} that suits the weather and the factors the user allowed.
 * TODO (Georgios): placeholder that always returns the general strategy; choose the indoor
 * strategy when weather is allowed and rain is expected.
 */
@Component
public class TourStrategyFactory {

    private final GeneralTourStrategy generalStrategy;

    /** Creates the factory. */
    public TourStrategyFactory(GeneralTourStrategy generalStrategy) {
        this.generalStrategy = generalStrategy;
    }

    /** Chooses a strategy. */
    public TourStrategy choose(
            List<HourlyForecast> forecast,
            Set<Factor> factors) {

        return generalStrategy;
    }
}
