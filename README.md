# City Tour Planner

## Overview

City Tour Planner is a software design course project that generates a simple city tour by combining data from multiple independent external sources.

Users select a city, date, start time and duration. The system retrieves weather, traffic and places data, then produces a basic itinerary. Results are shown on a map together with weather charts and traffic information.

## Objectives

- Retrieve data from multiple independent external sources.
- Combine the retrieved data in a meaningful way.
- Generate a basic itinerary based on the available information.
- Present the results through visualizations (map, weather chart, traffic panel, stop list).
- Design the system so that additional data sources can be integrated later.

## Current Scope

- City and date/time selection (currently focused on Tampere).
- Weather forecasts via **FMI Open Data**.
- Traffic / disruption information via **Digitraffic**.
- Places of interest stored in a local JSON file (Visit Tampere data, bilingual en/fi).
- Tour generation that can take weather and traffic into account (strategy still under development).
- Visualization: interactive map (Leaflet), weather chart (Recharts), traffic panel and stop list.

## Data Sources

| Source | Purpose | Status |
|--------|---------|--------|
| **FMI Open Data** | Weather forecasts & observations | Implemented (`FmiWeatherAdapter` + `FmiForecastParser`) |
| **Digitraffic** | Traffic announcements / disruptions | Implemented (`DigitrafficAdapter` + `DigitrafficAnnouncementParser`) |
| **Local JSON** (`places.json`) | Museums, landmarks and other points of interest | Implemented (Visit Tampere data) |

Open city data catalogues were investigated; a local repository was chosen because no suitable open API covering all required places was found.

## Architecture & Design Patterns

- **Backend**: Spring Boot (Java 22)
  - `TourController` – HTTP boundary
  - `TourService` – Facade that orchestrates providers and handles partial failure
  - `WeatherProvider` / `TrafficProvider` – Adapter interfaces (FMI & Digitraffic)
  - `PlaceRepository` – JSON-based repository
  - `TourStrategy` + `TourStrategyFactory` – Strategy pattern for stop selection
- **Frontend**: React + Vite + Tailwind CSS + Leaflet + Recharts
  - Pure view layer; no business logic
  - Currently uses mock data; ready to call the backend API

Design documentation (class diagrams, sequence diagrams, API contract, component responsibilities) lives in `backend/docs/`.

## Technology Stack

| Layer | Technology |
|-------|------------|
| Backend | Java 22, Spring Boot, Maven |
| Frontend | React 19, Vite, Tailwind CSS 4, Leaflet, Recharts |
| External APIs | FMI Open Data, Digitraffic |
| Places data | Local JSON file |

## Project Structure

```
.
├── backend/                  # Spring Boot application
│   ├── docs/                 # Design docs, API contract, samples, self-evaluation
│   ├── src/main/java/...     # Controllers, services, adapters, models, repository
│   ├── src/main/resources/
│   │   └── data/places.json
│   └── README.md             # Backend run instructions
└── frontend/
    └── city-tour-ui/         # React + Vite application
        ├── src/
        │   ├── components/   # PlanningForm, MapView, WeatherChart, …
        │   └── data/         # mockTour.js (temporary)
        └── README.md
```

## Running the Application

### Backend

Requirements: **Java 22**, **Maven 3.9+**, free port **8080**.

```bash
cd backend
mvn spring-boot:run
```

Or build a jar:

```bash
mvn clean package -DskipTests
java -jar target/city-tour-planner-backend-0.0.1-SNAPSHOT.jar
```

Smoke test:

```bash
curl http://localhost:8080/api/cities
# → [{"id":"tampere","name":"Tampere"},{"id":"helsinki","name":"Helsinki"},{"id":"vaasa","name":"Vaasa"},{"id":"oulu","name":"Oulu"}]
```

Plan a tour (FMI forecasts reach about two days ahead, so use a date within the next two days):

```bash
curl -X POST http://localhost:8080/api/tour \
  -H "Content-Type: application/json" \
  -d '{"city":"tampere","date":"<YYYY-MM-DD>","startTime":"13:00","durationHours":4,"language":"en","factors":["WEATHER","TRAFFIC"]}'
```

See `backend/README.md` and `backend/docs/api-contract.md` for full details.

### Frontend

```bash
cd frontend/city-tour-ui
npm install
npm run dev
```

Opens at `http://localhost:5173`.  
The UI currently uses mock data; it is prepared to call the backend (`CORS` already allows `http://localhost:5173`).

## API Overview

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/cities` | List available cities |
| GET | `/api/places?city=…&language=…` | Places of a city |
| POST | `/api/tour` | Generate a tour |

Full contract: `backend/docs/api-contract.md`.

## Team Responsibilities

| Member | Focus |
|--------|--------|
| **Varun** | Architecture, design patterns, integration, repository structure |
| **George** | Tour-generation logic and design rationale |
| **Thanh** | User-interface prototype (Figma → React) |
| **Abdullah** | External data adapters & parsers (FMI weather & Digitraffic), unit tests |

The team collaborates on API research and overall system decisions.

## Project Status

**Backend**
- Skeleton is running: interfaces, models, HTTP client, facade (`TourService`), controller, places repository.
- Weather (`FmiWeatherAdapter`) and traffic (`DigitrafficAdapter`) adapters are implemented with live API integration and dedicated parsers.
- Tour strategies (`GeneralTourStrategy`, `IndoorTourStrategy`, `TourStrategyFactory`) are implemented; the stops are built by `NearestPlaceTourPlanner`, and the rain threshold, walking speed and traffic distance are set in `application.yml` under `planner.planning`.
- Places are available for Tampere, Helsinki, Vaasa and Oulu.

**Frontend**
- Planning form, map (Leaflet), weather chart, traffic panel, stop list and layer toggles are implemented.
- Currently driven by mock data; integration with the real backend API is the next step.

**Known design decision**
- Places are stored in a local JSON file rather than fetched from an external open-data API (approved by the teaching assistant).

## Documentation

- Backend design & API: `backend/docs/`
- Backend run instructions: `backend/README.md`
- Frontend: `frontend/city-tour-ui/README.md` (default Vite template notes)
