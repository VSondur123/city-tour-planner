# Self-evaluation

## 1. How well have we been able to stick to our original design?

Since we started discussing this project we have stayed with one approach, because the team has been communicating throughout. Our original design is still intact.

The one change we had to make was forced by the lack of an open API that provides tour information for all locations. We moved to storing place information in our own JSON file, and we asked our TA for approval of this change.

> [Team to confirm before submission: list any other structural changes to the prototype design here, so that all changes are documented.]

## 2. How well have we been able to implement features based on our original plan?

We have been able to implement features fairly well according to the original plan. The plan has not changed so far, so nothing has needed to be dropped or reworked.

> [Update before submission with what is actually implemented at the tagged commit.]

## 3. How does our design correspond to quality?

Given what we have been presented with in the course, we believe we are using the right approaches and the correct patterns: Adapter, Facade and Proxy (with Strategy for stop selection).

We think this lets us reach better quality than building the application from simple classes. This matters especially because external APIs can fail, and we have already seen this happen. Our design handles a failing provider explicitly instead of letting the whole request fail.

## 4. What changes do we anticipate to implement the remaining features?

We expect to make a few changes here and there inside the Strategy part of the design, where the stop-selection logic is still being worked out.

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
- I added a demo profile that swaps the stub adapters and strategy for fixed output so the backend can be smoke-tested before those parts are finished.

### Thanh
- Created user interface prototypes in Figma (Option 1), consisting of: the planning screen, the loading screen, the results screen with data layer toggles, the results screen with one layer disabled, and the screen showing no traffic information.
- Optimized the screens after analyzing the APIs so that the UI matches the real data sources (road traffic alerts from Digitraffic instead of train schedules; places from the agreed data source rather than a live open places API).
- Implemented the React user interface using Vite, Tailwind CSS, Leaflet, and Recharts with the following screens/components: the planning form, data layer toggles, the map screen, the hourly weather graph, the stops list, and the road traffic information component.
- Added the ability to toggle layers so that Weather, Traffic, and Location data can be toggled on or off without having to make new API calls for data. The base map is still shown and layers control what is drawn on top of the base map.
- Added a "no traffic information" state where a notification is shown, the traffic panel is cleared out, and the related lines on the map are removed.
- Created the README file for the UI that includes the Node version requirement as well as instructions on how to install, run, and build the application.
- The UI at the moment uses sample data in the format that will be expected from the backend but is not connected to the Java backend. The next steps are: connecting the form to REST endpoints in the backend, replacing the sample data with API responses, and adding a loading bar for each data source.

### Georgios Malezoglou
- Working on research and development for the maps.
- Working with Varun on the progress of the API Research Runner.
- [Add anything else, to be filled in by Georgios.]

### Abdullah
- [To be filled in by Abdullah.]

> [Where any member's contribution differs from the prototype submission, explain the difference here.]
