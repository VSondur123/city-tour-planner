
# City Tour Planner — Frontend

## Overview

This directory contains the React frontend for the City Tour Planner application.

The frontend provides a responsive, map-first interface for generating and exploring city tours using weather forecasts, traffic announcements, and a curated places catalogue.

It communicates with the Spring Boot backend through REST APIs.

The frontend uses real backend data. The application no longer depends on mock tour responses during normal operation.

## Requirements

- Node.js 22.12 or newer recommended
- npm
- A modern browser (Chrome, Firefox, or Edge)
- Backend running at `http://localhost:8080`
- Internet connection for map tiles and backend data sources

## Technologies

| Technology | Purpose |
|---|---|
| React 19 | UI components and state |
| Vite 8 | Development server and production build |
| Tailwind CSS 4 | Utility styling |
| Custom CSS | Dashboard and responsive layout |
| Leaflet | Interactive map engine |
| React Leaflet | React integration for Leaflet |
| Recharts | Weather chart |
| Lucide React | Icons |
| JavaScript | Application logic |

## Installation

From the repository root:

```bash
cd frontend/city-tour-ui
npm install
```

This installs the dependencies defined in `package.json`.

## Running the Application

### 1. Start the Backend

Open a terminal at the repository root:

```bash
cd backend
mvn spring-boot:run
```

Wait until the Spring Boot application starts.

Backend URL:

```text
http://localhost:8080
```

Verify:

```bash
curl http://localhost:8080/api/cities
```

### 2. Start the Frontend

Open another terminal at the repository root:

```bash
cd frontend/city-tour-ui
npm run dev
```

Open the URL shown by Vite, normally:

```text
http://localhost:5173
```

The backend and frontend must run simultaneously.

### API Proxy

The Vite development server forwards requests beginning with `/api` to:

```text
http://localhost:8080
```

The proxy is configured in `vite.config.js`.

Therefore, frontend requests such as:

```javascript
fetch('/api/cities')
```

are forwarded to the Spring Boot backend.

No separate frontend API key is required.

## Main Features

### Planning Form

Users can:

- Select Tampere, Helsinki, Vaasa, or Oulu.
- Choose a date and start time.
- Choose a tour duration of 2, 4, or 6 hours.
- Select English or Finnish.
- Enable or disable WEATHER and TRAFFIC planning factors.
- Generate a tour using the backend.

### Map-Based Dashboard

The application provides:

- A large interactive OpenStreetMap.
- Numbered markers for generated tour stops.
- Suggested visit-order lines.
- Traffic markers and line geometries.
- Additional markers for places in the selected city.
- Map zoom controls.
- Collapsible planning and tour details panels.

Suggested visit-order lines are not actual walking routes.

### Tour Results

The frontend displays:

- Ordered tour stops.
- Start and end times.
- Place names and categories.
- Indoor/outdoor information.
- Visit durations and external links.
- Weather forecasts.
- Traffic announcements.
- The backend's explanation of the generated tour.

### All Places

The places catalogue supports:

- Loading places from the backend.
- Searching by place name.
- Filtering by category.
- Filtering indoor and outdoor places.
- Showing places on the map.
- Selecting a place to focus the map.
- Opening external place information links.

### Localization

The interface supports:

- English (`en`)
- Finnish (`fi`)

The backend supplies localized place names and explanations.

The frontend translates interface labels, controls, and supported error messages.

Digitraffic content may remain in Finnish because it is supplied by the external source.

### Planning Factors and Display Layers

Planning factors affect backend tour generation.

- `WEATHER`: Allows weather to influence stop selection.
- `TRAFFIC`: Allows traffic announcements to influence stop selection.

Changing planning factors after a tour has been generated sends a new request to the backend.

Display layers only control what is visible in the frontend. Toggling a layer does not regenerate the tour.

## Backend API Integration

API functions are located in:

```text
src/api/tourApi.js
```

API response transformation is handled in:

```text
src/api/tourAdapter.js
```

### API Endpoints

| Method | Endpoint | Usage |
|---|---|---|
| GET | `/api/cities` | Load supported cities |
| GET | `/api/places?city=...&language=...` | Load places |
| POST | `/api/tour` | Generate or regenerate a tour |

Example tour request:

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

The frontend handles the following backend statuses:

- `AVAILABLE`: Data is available.
- `EMPTY`: The source returned no data.
- `UNAVAILABLE`: Data could not be retrieved.

The interface also handles structured API validation errors.

For the complete API specification, see:

`../../backend/docs/api-contract.md`

## Frontend Structure

```text
src/
├── api/
│   ├── tourApi.js
│   └── tourAdapter.js
├── components/
│   ├── AllPlaces.jsx
│   ├── LayerToggles.jsx
│   ├── MapView.jsx
│   ├── PlacePhoto.jsx
│   ├── PlanningForm.jsx
│   ├── ResultControls.jsx
│   ├── StopList.jsx
│   ├── TrafficPanel.jsx
│   ├── WeatherChart.jsx
│   └── WeatherIcon.jsx
├── data/
│   └── mockTour.js
├── i18n/
│   └── translations.js
├── App.jsx
├── dashboard.css
├── index.css
└── main.jsx
```

### Component Responsibilities

| Component | Responsibility |
|---|---|
| `App.jsx` | Main layout, state management, API integration |
| `PlanningForm.jsx` | Tour input form |
| `MapView.jsx` | Map, tour markers, traffic geometry, places |
| `AllPlaces.jsx` | Places catalogue, search, and filtering |
| `WeatherChart.jsx` | Weather visualization |
| `WeatherIcon.jsx` | Weather condition icons |
| `TrafficPanel.jsx` | Traffic announcement details |
| `StopList.jsx` | Tour stop information |
| `PlacePhoto.jsx` | Place thumbnail presentation |
| `LayerToggles.jsx` | Visualization layer controls |
| `ResultControls.jsx` | Planning option controls |
| `tourApi.js` | Backend REST API requests |
| `tourAdapter.js` | API response transformation |
| `translations.js` | English and Finnish UI text |
| `dashboard.css` | Dashboard layout and responsive styling |

The `mockTour.js` file remains in the repository as legacy sample data but is not used by the active tour generation flow.

## Available Commands

Run from `frontend/city-tour-ui`:

| Command | Purpose |
|---|---|
| `npm install` | Install dependencies |
| `npm run dev` | Start the development server |
| `npm run lint` | Check ESLint rules |
| `npm run build` | Generate a production build |
| `npm run preview` | Preview the production build |

## Build and Validation

To check the frontend:

```bash
npm run lint
npm run build
```

The production build is saved in:

```text
dist/
```

To preview the build:

```bash
npm run preview
```

Note that the `/api` proxy is configured for the development server. A separate production deployment may require a reverse proxy or an appropriately configured backend URL.

## Manual Testing

The frontend has been manually tested for:

1. Generating tours in all four supported cities.
2. Loading real API data.
3. Displaying tour stops on the map.
4. Weather and traffic visualization.
5. English/Finnish switching.
6. WEATHER and TRAFFIC planning factors.
7. Place searching and filtering.
8. Map marker interaction.
9. Date validation.
10. Backend connection errors and recovery.
11. Empty or unavailable weather data.
12. Loading states and repeated requests.
13. Responsive layouts on desktop, tablet, and mobile.
14. Collapsing and expanding dashboard panels.

The frontend does not currently include an automated test suite.

## Known Limitations

- The frontend requires the backend to be available for live tour generation.
- External weather and traffic services may sometimes return empty or unavailable data.
- FMI forecasts are generally limited to approximately two days ahead.
- Some traffic announcement text remains in Finnish.
- Tour-stop lines show visit order rather than actual walking directions.
- Places are loaded from a curated backend repository, not a live places API.
- The displayed place photographs are illustrative and are not guaranteed to depict the exact attraction.
- Saved tours, authentication, bookings, and payments are outside the current scope.

## Additional Documentation

- Root README: `../../README.md`
- Backend README: `../../backend/README.md`
- API contract: `../../backend/docs/api-contract.md`

## Course

COMP.SE.110 Software Design  
Tampere University, Academic Year 2026–2027
