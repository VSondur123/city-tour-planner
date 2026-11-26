
# City Tour Planner

## Overview

City Tour Planner is a web application developed for the COMP.SE.110 Software Design course at Tampere University.

The application generates personalized city tours by combining weather forecasts, road traffic announcements, and a curated catalogue of places of interest.

Users can select a city, date, start time, duration, language, and planning factors. The backend generates an itinerary containing ordered stops, estimated visit times, weather forecasts, traffic announcements, and an explanation of the planning decisions.

The frontend provides an interactive map-first interface for planning and exploring city tours.

## Project Objectives

- Retrieve information from multiple independent data sources.
- Combine weather, traffic, and place information to generate meaningful tours.
- Apply weather- and traffic-aware planning rules.
- Display tour results through an interactive map and visual components.
- Handle external API failures without stopping the entire application.
- Support English and Finnish.
- Maintain an extensible architecture using software design patterns.

## Implemented Features

### Tour Planning

- City selection: Tampere, Helsinki, Vaasa, and Oulu.
- Date and start time selection.
- Tour duration selection (2, 4, or 6 hours in the UI).
- English and Finnish language support.
- Optional WEATHER and TRAFFIC planning factors.
- Automatic generation of ordered tour stops.
- Estimated visit times and walking-time calculations.

The backend API supports tour durations from 1 to 12 hours.

### Interactive Map

- OpenStreetMap tiles rendered using Leaflet.
- Numbered markers for selected tour stops.
- Lines showing the suggested visit order.
- Traffic announcement markers and geometries.
- Additional place markers.
- Map zooming and place selection.
- Collapsible planning and tour details panels.

The lines connecting tour stops represent visit order, not actual pedestrian routing paths.

### Places Catalogue

- Retrieve places using `GET /api/places`.
- Display places for the selected city.
- Search places by name.
- Filter places by category and indoor/outdoor status.
- Display place information and external links.
- Select a place to focus the map on its location.

### Weather Information

- Hourly weather forecasts from the Finnish Meteorological Institute (FMI).
- Temperature and precipitation visualization.
- Additional wind speed and cloud cover information.
- Handling of empty or unavailable weather data.

### Traffic Information

- Road traffic announcements from Digitraffic.
- Traffic descriptions, areas, and available timestamps.
- Traffic geometries displayed on the map where coordinates exist.
- Handling of missing or unavailable traffic information.

Traffic announcement text may remain in Finnish because it is provided by the external source.

### User Interface

- Responsive React interface.
- Map-first dashboard layout.
- Collapsible side panels and places panel.
- English and Finnish interface localization.
- Loading indicators and error messages.
- Empty states for missing data.
- Independent visualization layer controls.

## Technology Stack

| Layer | Technology | Purpose |
|---|---|---|
| Backend | Java 22 | Application implementation |
| Framework | Spring Boot 3.5.6 | REST API and application services |
| Build tool | Maven 3.9+ | Backend compilation and tests |
| Frontend | React 19 | User interface |
| Frontend tooling | Vite 8 | Development server and production build |
| Styling | Tailwind CSS 4 and custom CSS | Responsive interface |
| Mapping | Leaflet and React Leaflet | Interactive maps |
| Charts | Recharts | Weather visualization |
| Icons | Lucide React | Interface icons |
| Weather | FMI Open Data | Weather forecasts |
| Traffic | Digitraffic | Road traffic announcements |
| Places | Local JSON repository | Curated attractions |

## Supported Cities

- Tampere
- Helsinki
- Vaasa
- Oulu

The places catalogue currently contains 23 places across these four cities.

## Architecture and Design Patterns

The application follows a layered architecture.

### Backend

The backend separates HTTP handling, business logic, data access, and external API integration.

Main components:

- `TourController`: Handles REST API requests and validation.
- `TourService`: Coordinates tour generation and external data retrieval.
- `WeatherProvider`: Interface for weather data.
- `TrafficProvider`: Interface for traffic information.
- `PlaceRepository`: Interface for accessing places.
- `TourStrategyFactory`: Selects the appropriate planning strategy.
- `GeneralTourStrategy`: Considers all eligible places.
- `IndoorTourStrategy`: Considers indoor places when rain is expected.
- `NearestPlaceTourPlanner`: Creates ordered tours and estimates travel times.

### Design Patterns

| Pattern | Implementation | Purpose |
|---|---|---|
| Facade | `TourService` | Centralizes tour generation |
| Adapter | FMI and Digitraffic adapters | Integrates external APIs |
| Strategy | Tour planning strategies | Supports different planning rules |
| Factory Method | `TourStrategyFactory` | Selects the planning strategy |

A caching Proxy was considered but has not been implemented.

### Frontend

The frontend is responsible for:

- Collecting user input.
- Sending requests to the backend.
- Transforming API responses for presentation.
- Displaying maps, weather, traffic, and places.
- Managing language selection and visualization state.
- Handling API errors and unavailable information.

Tour planning decisions remain in the backend.

## Project Structure

```text
city-tour-planner/
├── README.md
├── backend/
│   ├── README.md
│   ├── pom.xml
│   ├── docs/
│   │   ├── api-contract.md
│   │   ├── components-and-responsibilities.md
│   │   ├── configuration.md
│   │   ├── testing.md
│   │   ├── self-evaluation.md
│   │   └── samples/
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   └── resources/
│       │       └── data/places.json
│       └── test/
├── frontend/
│   └── city-tour-ui/
│       ├── README.md
│       ├── package.json
│       ├── vite.config.js
│       └── src/
│           ├── api/
│           ├── components/
│           ├── i18n/
│           ├── App.jsx
│           ├── dashboard.css
│           └── main.jsx
├── api-research-runner/
└── submission/
```

## Requirements

### Backend

- Java JDK 22
- Maven 3.9 or newer
- Internet connection
- Available port 8080

### Frontend

- Node.js 22.12 or newer recommended
- npm
- Modern web browser
- Available port 5173

## Installation and Running

Clone the repository:

```bash
git clone https://github.com/VSondur123/city-tour-planner.git
cd city-tour-planner
```

### 1. Start the Backend

Open a terminal in the project root:

```bash
cd backend
mvn spring-boot:run
```

*(On Windows, ensure Java 22 is active in your terminal, e.g. `set JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-22.x"` in Command Prompt or `$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-22.x"` in PowerShell).*

The backend starts at:

```text
http://localhost:8080
```

Verify the API:

```bash
# macOS / Linux / Git Bash
curl http://localhost:8080/api/cities

# Windows PowerShell
Invoke-RestMethod http://localhost:8080/api/cities
```

Expected cities:

```text
Tampere, Helsinki, Vaasa, Oulu
```

To run backend unit tests:

```bash
mvn test
```

### 2. Start the Frontend

Open a second terminal from the project root:

```bash
cd frontend/city-tour-ui
npm install
npm run dev
```

Open:

```text
http://localhost:5173
```

Both the backend and frontend must be running to use the complete application.

Vite forwards requests beginning with `/api` to the backend at `http://localhost:8080`.

### 3. Build the Frontend

From `frontend/city-tour-ui`:

```bash
npm run lint
npm run build
```

Production build files are generated in `dist/`.

To preview the build:

```bash
npm run preview
```

The development proxy is configured for Vite's development server. A separate deployment may require additional API proxy configuration.

## REST API

The backend provides three endpoints:

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/cities` | Retrieve supported cities |
| GET | `/api/places` | Retrieve places by city and language |
| POST | `/api/tour` | Generate a city tour |

Example request:

```http
POST /api/tour
Content-Type: application/json
```

```json
{
  "city": "tampere",
  "date": "2026-11-26",
  "startTime": "13:00",
  "durationHours": 4,
  "language": "en",
  "factors": ["WEATHER", "TRAFFIC"]
}
```

The date must be today or later in the configured Finnish time zone.

The response contains:

- `stops`
- `forecast`
- `announcements`
- `weatherStatus`
- `trafficStatus`
- `trafficTextLanguage`
- `language`
- `factors`
- `explanation`

For detailed request/response specifications, see:

`backend/docs/api-contract.md`

## Testing

### Backend Tests

Run:

```bash
cd backend
mvn test
```

The backend includes tests for:

- Models and validation.
- REST controllers.
- External API adapters and parsers.
- Tour generation.
- Weather and traffic planning rules.
- Repository and HTTP client behavior.
- Failure handling.

Optional live integration tests:

```bash
mvn test -Dtest='*LiveTest'
```

Live tests depend on FMI, Digitraffic, and network availability.

### Frontend Validation

From `frontend/city-tour-ui`:

```bash
npm run lint
npm run build
```

Manual testing has covered:

- All four supported cities.
- Tour generation and API integration.
- English/Finnish switching.
- Weather and traffic planning factors.
- Map interactions and layer toggles.
- Places search and filtering.
- Date validation.
- Backend connection failures and recovery.
- Empty or unavailable provider data.
- Mobile, tablet, and desktop layouts.

The frontend currently has no automated test suite.

## Known Limitations

- Weather forecasts are generally available only for approximately the next two days.
- Digitraffic announcement text is primarily Finnish.
- The place catalogue is curated locally rather than retrieved from a live places API.
- Map lines between tour stops indicate visit order, not actual walking routes.
- The backend estimates walking time using geographical distance and a configured walking speed.
- The caching Proxy pattern is proposed but not implemented.
- The application does not support user accounts, bookings, or payments.

## Team Responsibilities

| Member | Main Contribution |
|---|---|
| Varun Ganesh Sondur | Architecture, backend integration, REST API, shared models, repository, tests, documentation |
| Pham Thanh | Frontend implementation, API integration, localization, map-based UI, responsive layout |
| Georgios Malezoglou | Tour planning strategies, strategy factory, nearest-place planning, related tests |
| Abdullah | FMI and Digitraffic adapters, parsing, external API integration, related tests |

All members participated in design discussions and integration decisions.

## Documentation

- Final design document: `submission/City_Tour_Planner_Final_Design_Document.pdf` (contains architecture, design patterns, testing strategy, and self-evaluation)
- Backend documentation: `backend/docs/`
- API contract: `backend/docs/api-contract.md`
- Configuration details: `backend/docs/configuration.md`
- Backend instructions: `backend/README.md`
- Frontend instructions: `frontend/city-tour-ui/README.md`

## Course

COMP.SE.110 Software Design  
Tampere University, Academic Year 2026–2027
