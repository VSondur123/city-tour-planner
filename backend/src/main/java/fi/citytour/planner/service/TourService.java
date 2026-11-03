package fi.citytour.planner.service;

import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.place.City;
import fi.citytour.planner.model.place.Place;
import fi.citytour.planner.model.place.PlaceView;
import fi.citytour.planner.model.tour.DataStatus;
import fi.citytour.planner.model.tour.Factor;
import fi.citytour.planner.model.tour.StopView;
import fi.citytour.planner.model.tour.TourRequest;
import fi.citytour.planner.model.tour.TourResult;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;
import fi.citytour.planner.model.weather.HourlyForecast;
import fi.citytour.planner.planning.TourStrategy;
import fi.citytour.planner.planning.TourStrategyFactory;
import fi.citytour.planner.provider.ProviderException;
import fi.citytour.planner.provider.TrafficProvider;
import fi.citytour.planner.provider.WeatherProvider;
import fi.citytour.planner.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

/**
 * Facade that offers the single entry point for generating tours and for listing cities and
 * places. A failing provider is reported as {@code UNAVAILABLE} and the rest of the tour is
 * still produced.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TourService {

    private final WeatherProvider weatherProvider;
    private final TrafficProvider trafficProvider;
    private final PlaceRepository placeRepository;
    private final TourStrategyFactory tourStrategyFactory;

    /** Generates a tour for a request. */
    public TourResult generateTour(TourRequest request) {

        City city = requireCity(request.city());
        Language language = request.language();

        Fetched<HourlyForecast> weather = fetch(
                "weather",
                () -> weatherProvider.getForecast(city.id(), request.date()));

        Fetched<TrafficAnnouncement> traffic = fetch(
                "traffic",
                () -> trafficProvider.getAnnouncements(city.id(), language));

        List<HourlyForecast> plannerForecast =
                request.factors().contains(Factor.WEATHER)
                        ? weather.items()
                        : List.of();
        List<TrafficAnnouncement> plannerAnnouncements =
                request.factors().contains(Factor.TRAFFIC)
                        ? traffic.items()
                        : List.of();

        TourStrategy strategy = tourStrategyFactory.choose(
                plannerForecast, request.factors());

        List<StopView> stops = strategy
                .plan(placeRepository.findByCity(city.id()),
                        request, plannerForecast, plannerAnnouncements)
                .stream()
                .map(stop -> StopView.of(stop, language))
                .toList();

        return new TourResult(
                language,
                request.factors(),
                stops,
                weather.items(),
                traffic.items(),
                weather.status(),
                traffic.status(),
                traffic.items().stream()
                        .map(TrafficAnnouncement::language)
                        .findFirst()
                        .orElse(null),
                strategy.explanation(language)
        );
    }

    /** Lists the cities for which places are available. */
    public List<City> listCities() {
        return placeRepository.findCities();
    }

    /** Lists the places of a city, with their text in one language. */
    public List<PlaceView> listPlaces(String cityId, Language language) {
        City city = requireCity(cityId);

        return placeRepository.findByCity(city.id()).stream()
                .map((Place place) -> PlaceView.of(place, language))
                .toList();
    }

    /** Finds a city by its identifier, or fails if it is unknown. */
    private City requireCity(String cityId) {
        return placeRepository.findCities().stream()
                .filter(city -> city.id().equalsIgnoreCase(cityId))
                .findFirst()
                .orElseThrow(() -> new CityNotFoundException(cityId));
    }

    /** Calls a provider and records the outcome as a status. */
    private <T> Fetched<T> fetch(String name, Supplier<List<T>> call) {
        try {
            List<T> items = call.get();

            return new Fetched<>(items, items.isEmpty()
                    ? DataStatus.EMPTY
                    : DataStatus.AVAILABLE);

        } catch (ProviderException exception) {
            log.warn("Could not retrieve {}: {}", name, exception.getMessage());

            return new Fetched<>(List.of(), DataStatus.UNAVAILABLE);
        }
    }

    /** Data retrieved from a provider together with the outcome. */
    private record Fetched<T>(List<T> items, DataStatus status) {}
}
