# Testing

Run all commands in `backend/` with Java 22.

| Command | What it runs |
|---|---|
| `mvn test` | All unit tests. They use fixed XML and JSON, mocks and a local test server, so they need no network |
| `mvn test -Dtest='*LiveTest'` | The live tests, which call FMI and Digitraffic. They are left out of `mvn test` because they depend on the network and on current data |

## What is covered

| Area | Tests |
|---|---|
| Models | Language lookup, text fallback between languages, request defaults, place and stop views |
| Repository | `JsonPlaceRepository` with the shipped `places.json` and with a missing file |
| `TourService` | A failing or empty provider, the factor switches, the tour window sent to the traffic provider, unknown city, a past date (rejected), today and a date beyond the forecast range (accepted) |
| Controller | Every error code of the API contract, the language default, the cities and places endpoints |
| HTTP client | Decoding, gzip, a non-2xx status, a connection failure |
| FMI | `FmiForecastParser` (missing values, hourly precipitation, errors, forecast horizon) and `FmiWeatherAdapter` (request times) |
| Digitraffic | `DigitrafficAnnouncementParser` (municipality match, language, time window, geometry) and `DigitrafficAdapter` |
| Planning | `TourStrategyFactory` (rain window), `NearestPlaceTourPlanner` (fit, walking time, determinism, traffic rule), both strategies, and the validated planning settings |

The live tests only check that the real services answer and the fields are filled; an empty forecast or an empty traffic list also passes. To check a full tour against the real services, start the backend and call `POST /api/tour` with a date within the next two days (see the backend README).
