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
- Designed the overall repository structure.
- Identified the proper design patterns for the application.
- Built the API Research Runner, and identified that we needed to move to a repository structure.
- Designed the high-level components and interfaces, and carried out the analysis behind them.

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
  - Reviewed and agreed on the implementation of the `TourStrategy` (interface), `GeneralTourStrategy`, `IndoorTourStrategy` and `TourStrategyFactory` classes (where the whole tour generation logic, factory and strategy must be implemented).
  - Strategy pattern: `GeneralTourStrategy` and `IndoorTourStrategy` have a common interface `TourStrategy` with `plan()` and `explanation()` methods, where:
    - `plan()` is meant to select the places that fit into the duration the user chose, using each place's visit time, and to return them as an ordered list of stops. Each stop is a place with a start and an end time. It is still a stub and the logic needs to be implemented.
    - `explanation()` returns a message explaining why a tour plan is chosen. It is still a stub and the logic needs to be implemented.
  - Factory pattern: in the `TourStrategyFactory` class, the `choose()` method is responsible for choosing between a tour strategy (general or indoor) based on 2 conditions (weather and expected rain). `IndoorTourStrategy` is returned only if the weather factor is enabled and rain is expected, otherwise `GeneralTourStrategy` is returned.
  - `IndoorTourStrategy` is meant to pick indoor places first and add outdoor places only if time remains, while `GeneralTourStrategy` can use both indoor and outdoor places.
  - Main flow: `TourService` asks the factory which strategy fits the forecast and the factors the user enabled. It then calls that strategy to select and sort the stops, and builds the final result from those stops, the explanation, the forecast and the traffic announcements. The forecast and announcements are always included in the result, whichever factors the user enabled.
  - Implemented the `TourStrategyFactory`'s `choose` method and created a separate (private) method `isRainExpected` that checks the hourly forecast and returns true if any hour has precipitation above the 0.5 mm threshold, otherwise false (I chose 0.5 mm for precipitation as the point where rain becomes noticeable. It is a constant and it might be changed later).
  - Reason for design pattern choice: Strategy is chosen so we can make separate classes for every rule (and add new possible rules) without touching `TourService`. Factory keeps the decision logic (if/else) out of `TourService`, in a separate class that chooses which strategy to use.
- Planned implementation idea for `GeneralTourStrategy`:
  - create `endOfTour` that calculates tour starting time + tour duration
  - add a 15 minute break (constant `VISIT_BREAK`) after a visit to a place is finished
  - look through the places from `places.json` in order with a for loop
  - for each place (stop), calculate the current time of the tour + the current place's visit duration + the visit break (`currentTime + visitMinutes + VISIT_BREAK`)
  - add an if statement checking whether there is room to add a place (stop) or not (checks that `currentTime + visitMinutes + VISIT_BREAK <= tour deadline`)
  - if the tour deadline is surpassed, the loop continues checking the next places from `places.json` in order and stops only after checking the last place (this might be changed later to stop when the tour's remaining time is less than the minimum visit duration (const `MIN_VISIT_DURATION`), so the loop doesn't need to go on for no reason)
  - NOTE: when (and how) to add breaks between stops is yet to be decided and the constant value might be changed later. The idea described is still a draft.
- AI usage note: AI was used on the text of my evaluation to fix grammar and typo mistakes and to rephrase some sentences so they make more sense.

### Abdullah
- [To be filled in by Abdullah.]

> [Where any member's contribution differs from the prototype submission, explain the difference here.]
