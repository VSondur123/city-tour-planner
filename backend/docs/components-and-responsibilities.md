# Components and responsibilities

## Frontend (React), MVC: View
| | |
|---|---|
| **Purpose** | Collect the tour request and display the result |
| **Responsibilities** | Language toggle and translated labels; input form (city list from the backend); loading screen; results with map, stops, weather chart and traffic table; display-layer switches (local); planning-mode switches (new request) |
| **Depends on** | The HTTP API of the backend |
| **Why** | Holds no business logic, so the backend can change without touching the UI |

## `TourController`, MVC: Controller
| | |
|---|---|
| **Purpose** | The HTTP boundary of the backend |
| **Responsibilities** | Receive `GET /api/cities`, `GET /api/places` and `POST /api/tour`; validate the input; call `TourService`; return JSON |
| **Depends on** | `TourService` |
| **Used by** | The frontend |
| **Why** | Keeps HTTP concerns out of the planning code |

## `TourService`, Facade
| | |
|---|---|
| **Purpose** | The single entry point for generating a tour and listing cities and places |
| **Responsibilities** | Collect weather, traffic and places; choose a strategy; build the `TourResult`; set `weatherStatus` and `trafficStatus` when a provider fails or returns nothing |
| **Depends on** | `WeatherProvider`, `TrafficProvider`, `PlaceRepository`, `TourStrategyFactory` |
| **Used by** | `TourController` |
| **Internals** | Calls the providers, handles failure per source, calls the factory, calls the strategy, assembles the result |
| **Why** | Decouples the controller from the subsystems, and is the one place where partial failure is handled |

## `FmiWeatherAdapter` (implements `WeatherProvider`), Adapter

*Owner: Abdullah. Placeholder for now; the table describes the intended design.*
| | |
|---|---|
| **Purpose** | Turn the FMI forecast into `HourlyForecast` objects |
| **Responsibilities** | Request the forecast for one local day (the day's boundaries are converted to UTC for FMI); parse the XML in `FmiForecastParser` (positions and value rows are matched by index, columns come from the field list); convert UTC times to Finnish time; pick the fields we use (temperature, precipitation amount, cloud cover, wind). For a date beyond FMI's horizon FMI returns rows of `NaN`, which become an empty result, and an FMI error report or HTTP failure becomes a `ProviderException` |
| **Depends on** | The FMI WFS API, `ApiHttpClient`, `FmiForecastParser` |
| **Used by** | `TourService` |
| **Why** | If the provider or its format changes, only this class changes |

## `DigitrafficAdapter` (implements `TrafficProvider`), Adapter

*Owner: Abdullah. Placeholder for now; the table describes the intended design.*
| | |
|---|---|
| **Purpose** | Turn Digitraffic announcements into `TrafficAnnouncement` objects for a city |
| **Responsibilities** | Request the nationwide announcements; in `DigitrafficAnnouncementParser`, filter them to the selected city by municipality (the API has no city filter) and read every optional field null-safely; map type (the first feature name, such as "road closed", because the general type is usually just "general"), title, municipality, language, times and geometry; report "none" (empty list) and "failed" (`ProviderException`) differently. The text is in the announcement's own language, mostly Finnish |
| **Depends on** | The Digitraffic API, `ApiHttpClient`, `DigitrafficAnnouncementParser` |
| **Used by** | `TourService` |
| **Why** | Same as the FMI adapter, plus the city filtering lives in one place |

## `JsonPlaceRepository` (implements `PlaceRepository`)

*Owner: Varun.*
| | |
|---|---|
| **Purpose** | Provide the places and cities we support |
| **Responsibilities** | Load `places.json` (a map of city name to places) at startup; the key is the city filter; return the cities and the places of a city |
| **Depends on** | `places.json` |
| **Used by** | `TourService` |
| **Why** | Hides how places are stored, so a database or API could replace the file |

## `TourStrategy` implementations and `TourStrategyFactory`, Strategy and Factory Method

*Owner: Georgios. Placeholder for now; the table describes the intended design.*
| | |
|---|---|
| **Purpose** | Select and order stops for the requested duration |
| **Responsibilities** | The factory picks a strategy from the forecast and the factors; the strategy filters places (for example indoor first in rain) and orders them within the duration; with weather mode off, the general strategy is used; each strategy also supplies the "Why this tour" explanation in the requested language |
| **Depends on** | The model classes |
| **Used by** | `TourService` |
| **Why** | A new rule is a new class, and the service needs no change |

## Model classes
| | |
|---|---|
| **Purpose** | The shared data structures: `TourRequest`, `TourResult`, `Place`, `Stop`, `HourlyForecast`, `TrafficAnnouncement`, `City`, the enums `Factor` and `DataStatus`, and the text helper `LocalizedText` |
| **Responsibilities** | Carry data between layers, with no behaviour beyond simple access. `Place` and `Stop` keep text in every language; the response uses `PlaceView` and `StopView`, which are resolved to the requested language |
| **Used by** | All backend components |

## `ApiHttpClient` and `ApiRequestFactory`
| | |
|---|---|
| **Purpose** | Send requests to the external APIs |
| **Responsibilities** | One shared HTTP client with configured timeouts, redirects and gzip support; build request URIs from the configured provider plus request-time parameters (city, date window); treat any status outside 200-299 as a failure (`ApiClientException`) so an error page is never parsed as data |
| **Depends on** | `PlannerProperties` |
| **Used by** | The adapters |
| **Why** | Adapted from the API Research Runner. Keeping HTTP details here means the adapters deal only with provider data |

## Caching proxy (planned)
| | |
|---|---|
| **Purpose** | Avoid repeated calls to FMI and Digitraffic for the same city and date |
| **Responsibilities** | Wrap a provider behind the same interface and serve cached results for a short time |
| **Why** | Makes planning-mode and language switches cheap |
