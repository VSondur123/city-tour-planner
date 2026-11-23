# Self-evaluation

## 1. How well have we been able to stick to our original design?

Since we started discussing this project we have stayed with one approach, because the team has been communicating throughout. Our original design is still intact: the layers, the interfaces between them and the design patterns are the ones we planned.

The one change we had to make was forced by the lack of an open API that provides tour information for all locations. We moved to storing place information in our own JSON file, and we asked our TA for approval of this change.

During implementation we made a few smaller changes inside the same structure:
- `TrafficProvider` got a second method that takes the tour window (start and end), so Digitraffic announcements are filtered to the time of the tour.
- `TourStrategyFactory.choose` takes the whole request instead of only the factors, so it can find the tour window.
- The two strategies share one class, `NearestPlaceTourPlanner`, that builds the tour. The strategies only decide which places are candidates.
- The values that may need tuning (rain threshold, walking speed, traffic distance and the maximum age of open-ended announcements) are in `application.yml` under `planner.planning` and are validated at startup.
- The demo profile that we used before the adapters and strategies existed was removed.

## 2. How well have we been able to implement features based on our original plan?

We have been able to implement the backend according to the plan. The following is implemented and was checked against the live FMI and Digitraffic services: the FMI and Digitraffic adapters and parsers, the places repository with four cities (Tampere, Helsinki, Vaasa and Oulu), the facade, the controller with structured error responses, both tour strategies and the factory, and a unit test suite. A failing provider gives an `UNAVAILABLE` status and the tour is still produced.

Not done: the caching Proxy, which stays a planned improvement, and the connection of the React frontend to the backend (the frontend still uses sample data in the response format of the backend).

Known limits: FMI forecasts only about two days ahead; Digitraffic text is mostly Finnish and is shown as it is; when rain is expected and no indoor place fits the duration, the tour is empty and the explanation says why; only the Tampere places come from VisitTampere, the other cities were curated by hand and have English links only.

## 3. How does our design correspond to quality?

Given what we have been presented with in the course, we believe we are using the right approaches and the correct patterns: Adapter for FMI and Digitraffic, Facade for `TourService`, and Strategy with a Factory Method for the stop selection. We also use a layered architecture, MVC (React as the view) and Dependency Inversion through interfaces. A caching Proxy is planned but not built.

We think this lets us reach better quality than building the application from simple classes. This matters especially because external APIs can fail, and we have already seen this happen. Our design handles a failing provider explicitly instead of letting the whole request fail.

## 4. What changes do we anticipate to implement the remaining features?

The remaining work is to connect the frontend to the backend, and to add the caching Proxy around the providers so that switching the planning mode or the language does not call FMI and Digitraffic again. Two product questions are still open: what the traffic factor should do beyond skipping places near an active announcement, and whether a rainy tour with no fitting indoor place should fall back to the general strategy (today it is empty and the explanation says that only indoor places were considered).

## 5. Individual tasks accomplished or changed per group member

### Varun Ganesh Sondur
- I designed the repository structure. Chose the design patterns (Adapter, Strategy, Factory Method, Facade, layered architecture, Dependency Inversion). The caching Proxy is not built yet.
- I built the API Research Runner to test FMI, Digitraffic and VisitTampere. Its results led us to use our curated places data.
- I designed the components and interfaces (WeatherProvider, TrafficProvider, PlaceRepository, TourStrategy) and the shared models.
- I implemented the Spring Boot backend skeleton:
  - the HTTP client and request factory
  - the TourService facade, which keeps a failing provider from failing the request
  - the REST controller (/api/cities, /api/places, /api/tour) and structured error handling.
- I implemented JsonPlaceRepository and the seed places.json.
- I wrote the API contract, sample responses, PlantUML diagrams and the components and responsibilities document. The frontend and the other backend parts can build against them.
- Before the adapters and strategies existed, I added a temporary demo profile that replaced them with fixed output, so the backend could be smoke-tested and the response shape shown. I removed it once those parts were implemented.
- I reviewed the adapter and strategy branches against the design and the API contract, ran them against the live APIs, and fixed the integration problems: the rain window now matches the hourly precipitation, the FMI request includes the hour before midnight, and open-ended announcements older than a set age no longer block places.
- I added unit tests for the models, the repository, `TourService`, the controller with its error codes and the HTTP client, and added Helsinki, Vaasa and Oulu to `places.json`.
- I added the configuration and testing documents and updated the API contract, the diagrams and the components document for the final code.

### Thanh
- Created user interface prototypes in Figma (Option 1), consisting of: the planning screen, the loading screen, the results screen with data layer toggles, the results screen with one layer disabled, and the screen showing no traffic information.
- Optimized the screens after analyzing the APIs so that the UI matches the real data sources (road traffic alerts from Digitraffic instead of train schedules; places from the agreed data source rather than a live open places API).
- Implemented the React user interface using Vite, Tailwind CSS, Leaflet, and Recharts with the following screens/components: the planning form, data layer toggles, the map screen, the hourly weather graph, the stops list, and the road traffic information component.
- Added the ability to toggle layers so that Weather, Traffic, and Location data can be toggled on or off without having to make new API calls for data. The base map is still shown and layers control what is drawn on top of the base map.
- Added a "no traffic information" state where a notification is shown, the traffic panel is cleared out, and the related lines on the map are removed.
- Created the README file for the UI that includes the Node version requirement as well as instructions on how to install, run, and build the application.
- The UI at the moment uses sample data in the format that will be expected from the backend but is not connected to the Java backend. The next steps are: connecting the form to REST endpoints in the backend, replacing the sample data with API responses, and adding a loading bar for each data source.

### Georgios Malezoglou
- Tested and proposed OpenStreetMap and made a small prototype to check how the map looks and if it's functional.
  - The map displays the desired country (zoomed in on Finland) on load, with different marks for every city where the user can click to zoom into a city.
  - I did that just to have an idea of how the map could look when a user loads it, and showed it to the team members.
  - The reason for choosing it is that it is entirely open-source and does not require an API key to access its core geographical data. Also, it looks simple and user friendly.
  - The prototype was made with the help of Qwen AI coder to speed up the prototype implementation process.
- Reviewed, discussed and agreed on Varun's proposed design patterns and the overall repository structure for the project.
- Responsible for the tour generation logic and the factory and strategy pattern implementation. Specifically:
  - Strategy pattern: `GeneralTourStrategy` and `IndoorTourStrategy` implement the common interface `TourStrategy` with `plan()` and `explanation()`. `GeneralTourStrategy` offers all places, `IndoorTourStrategy` only indoor places. Both hand the tour building to `NearestPlaceTourPlanner` and return their own explanation text in English and Finnish.
  - `NearestPlaceTourPlanner` picks the first stop with a seeded random generator (the same request gives the same tour), then always the nearest place that still fits before the end of the tour. Walking time comes from the straight-line distance and a configurable speed. A place is skipped for a visit that overlaps a traffic announcement near it.
  - Factory pattern: `TourStrategyFactory.choose()` returns `IndoorTourStrategy` only if the weather factor is enabled and rain above the threshold is expected during the tour window (the hours after the start up to and including the end, in the planner time zone). Missing values do not count as rain. Otherwise it returns `GeneralTourStrategy`.
  - The rain threshold, walking speed and traffic distance are set in `application.yml` and validated at startup.
  - Main flow: `TourService` asks the factory which strategy fits the forecast and the factors the user enabled. It then calls that strategy to select and sort the stops, and builds the final result from those stops, the explanation, the forecast and the traffic announcements. The forecast and announcements are always included in the result, whichever factors the user enabled.
  - Reason for design pattern choice: Strategy is chosen so we can make separate classes for every rule (and add new possible rules) without touching `TourService`. Factory keeps the decision logic (if/else) out of `TourService`, in a separate class that chooses which strategy to use.
  - Wrote unit tests for the factory, the planner, both strategies and the planning settings.
- AI usage note: AI was used on the text of my evaluation to fix grammar and typo mistakes and to rephrase some sentences so they make more sense.

### Abdullah
- Implemented `FmiWeatherAdapter` and `FmiForecastParser`. They request one local day (and the hour before it, for the precipitation difference) from the FMI WFS service and parse the XML. NaN values beyond the forecast horizon give an empty result, an FMI error report gives a `ProviderException`, missing values stay `null`, and the cumulative precipitation of FMI is converted to hourly amounts.
- Implemented `DigitrafficAdapter` and `DigitrafficAnnouncementParser`. They read the nationwide announcements, keep those of the selected municipality and the tour window, choose the language variant and convert the geometry.
- Both adapters use the shared `ApiHttpClient` and report failures as `ProviderException`, so `TourService` can set `UNAVAILABLE`.
- Wrote unit tests for the adapters and parsers, and live tests that call the real APIs.
- Added temporary test endpoints for checking the adapters by hand, and removed them after the review.
