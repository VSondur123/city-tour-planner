package fi.citytour.planner.demo;

import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.place.Place;
import fi.citytour.planner.model.tour.Factor;
import fi.citytour.planner.model.tour.Stop;
import fi.citytour.planner.model.tour.TourRequest;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;
import fi.citytour.planner.model.weather.HourlyForecast;
import fi.citytour.planner.planning.GeneralTourStrategy;
import fi.citytour.planner.planning.TourStrategy;
import fi.citytour.planner.planning.TourStrategyFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/** Demo replacement for the strategy factory: always returns the fixed stops. */
@Component
@Primary
@Profile("demo")
public class DemoTourStrategyFactory extends TourStrategyFactory {

    private final DemoData data;

    public DemoTourStrategyFactory(GeneralTourStrategy generalStrategy, DemoData data) {
        super(generalStrategy);
        this.data = data;
    }

    @Override
    public TourStrategy choose(List<HourlyForecast> forecast, Set<Factor> factors) {
        return new TourStrategy() {
            @Override
            public List<Stop> plan(
                    List<Place> places,
                    TourRequest request,
                    List<HourlyForecast> forecast,
                    List<TrafficAnnouncement> announcements) {

                return data.stops().stream()
                        .flatMap(stop -> places.stream()
                                .filter(place -> place.id().equals(stop.placeId()))
                                .map(place -> new Stop(place, stop.start(), stop.end())))
                        .toList();
            }

            @Override
            public String explanation(Language language) {
                return data.explanation().resolve(language);
            }
        };
    }
}
