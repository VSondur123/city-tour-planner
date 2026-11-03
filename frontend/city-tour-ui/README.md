# City Tour Planner — Frontend

React + Vite user interface for the City Tour Planner.

## Requirements

- **Node.js** 18 or newer
- **npm** (comes with Node.js)

## Install

```bash
cd frontend/city-tour-ui
npm install
```

## Run (development)

```bash
npm run dev
```

The app opens at [http://localhost:5173](http://localhost:5173).

Vite is configured to proxy `/api` requests to the Java backend at `http://localhost:8080`.  
Start the backend first if you want real API calls (see the root `README.md` or `backend/README.md`).

## Build

```bash
npm run build
```

Production files are written to `dist/`.

## Preview the production build

```bash
npm run preview
```

## Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start the Vite development server |
| `npm run build` | Build for production |
| `npm run preview` | Serve the production build locally |
| `npm run lint` | Run ESLint |

## Stack

| Library | Purpose |
|---------|---------|
| React 19 | UI |
| Vite | Build tool & dev server |
| Tailwind CSS 4 | Styling |
| Leaflet + react-leaflet | Interactive map |
| Recharts | Weather chart |

## Project structure

```
src/
├── App.jsx                 # Main layout and state
├── main.jsx                # Entry point
├── index.css               # Global styles (Tailwind)
├── components/
│   ├── PlanningForm.jsx    # City / date / time / duration form
│   ├── MapView.jsx         # Leaflet map with stops and traffic lines
│   ├── WeatherChart.jsx    # Hourly temperature / rain chart
│   ├── StopList.jsx        # Ordered list of tour stops
│   ├── TrafficPanel.jsx    # Traffic announcements
│   └── LayerToggles.jsx    # Show / hide weather, traffic, places
└── data/
    └── mockTour.js         # Temporary sample data (replace with API)
```

## Current status

- Planning form, map, weather chart, traffic panel, stop list and layer toggles are implemented.
- The UI currently uses **mock data** (`src/data/mockTour.js`).
- Integration with the backend (`POST /api/tour`, `GET /api/cities`, …) is the next step.
- A temporary “toggle traffic error” button is present for UI testing; remove it before the final submission.

## Connecting to the backend

1. Start the backend on port 8080.
2. Keep the Vite proxy (already in `vite.config.js`):

```js
server: {
  proxy: { '/api': 'http://localhost:8080' },
}
```

3. Replace the mock `setTimeout` in `App.jsx` with a real `fetch('/api/tour', …)` call.

CORS on the backend already allows `http://localhost:5173`.
