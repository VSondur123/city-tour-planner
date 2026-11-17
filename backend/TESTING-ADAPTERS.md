# Testing External Data Adapters (FMI & Digitraffic)

This document provides testing instructions, `curl` commands, and expected outputs for testing the **FMI Weather Adapter** and **Digitraffic Traffic Adapter** implemented by Abdullah.

---

## 1. Running the Application

From the `backend` directory, start the Spring Boot server:

```bash
cd backend
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

---

## 2. Dedicated Test Endpoints

The following dedicated test endpoints allow direct inspection of each adapter's live data without needing a full tour planning request:

### A. FMI Weather Adapter Test

Retrieves live hourly weather forecasts for a city from FMI (Finnish Meteorological Institute).

```bash
# Query today's forecast for Tampere
curl -s "http://localhost:8080/api/test/weather?city=tampere" | jq .

# Query a specific date
curl -s "http://localhost:8080/api/test/weather?city=tampere&date=2026-11-17" | jq .
```

**Sample Response (`200 OK`):**
```json
[
  {
    "time": "2026-11-17T14:00:00+02:00",
    "temperature": 5.8,
    "precipitationAmount": 0.0,
    "cloudCover": 100.0,
    "wind": 2.53
  },
  {
    "time": "2026-11-17T15:00:00+02:00",
    "temperature": 5.7,
    "precipitationAmount": 0.16,
    "cloudCover": 100.0,
    "wind": 2.39
  }
]
```

---

### B. Digitraffic Traffic Adapter Test

Retrieves active road announcements from Digitraffic filtered for the specified municipality.

```bash
# Query active announcements in Tampere (default language)
curl -s "http://localhost:8080/api/test/traffic?city=tampere" | jq .

# Query with language preference
curl -s "http://localhost:8080/api/test/traffic?city=tampere&language=en" | jq .
```

**Sample Response (`200 OK`):**
```json
[
  {
    "type": "Tie on suljettu liikenteeltä",
    "description": "Itsenäisyydenkatu, Tampere. Liikennetiedote.",
    "area": "Tampere",
    "language": "fi",
    "startTime": "2026-06-01T03:00:00Z",
    "endTime": null,
    "geometry": {
      "paths": [
        [
          { "lat": 61.498527, "lon": 23.771694 },
          { "lat": 61.498877, "lon": 23.77585 }
        ]
      ]
    }
  }
]
```

---

### C. Combined Adapter Overview Test

Queries both FMI weather and Digitraffic traffic in one call:

```bash
curl -s "http://localhost:8080/api/test/overview?city=tampere" | jq .
```

**Sample Response (`200 OK`):**
```json
{
  "city": "tampere",
  "date": "2026-11-17",
  "weatherCount": 10,
  "weather": [ ... ],
  "trafficCount": 3,
  "traffic": [ ... ]
}
```

---

## 3. End-to-End Tour Planning Test

Tests the full `TourService` integration via `POST /api/tour`:

```bash
curl -X POST "http://localhost:8080/api/tour" \
  -H "Content-Type: application/json" \
  -d '{
    "city": "tampere",
    "date": "2026-11-17",
    "startTime": "13:00",
    "durationHours": 4,
    "language": "en",
    "factors": ["WEATHER", "TRAFFIC"]
  }' | jq .
```

**Verification:**
* `weatherStatus`: Should return `"AVAILABLE"` (or `"EMPTY"` if querying a date beyond FMI's ~66h forecast horizon).
* `trafficStatus`: Should return `"AVAILABLE"` (or `"EMPTY"` if no active announcements exist for the city).
* `forecast`: Contains hourly weather entries for the requested local day.
* `announcements`: Contains active road disruption notices for the city.

---

## 4. Running Automated Tests

### Offline Unit Tests (Fast, no network required):
```bash
cd backend
mvn test
```
* Runs all unit tests (FmiForecastParserTest, FmiWeatherAdapterTest, DigitrafficAnnouncementParserTest, DigitrafficAdapterTest, AdapterTestControllerTest, etc.).
* All 24 tests pass with 0 failures and 0 errors.

### Live Integration Tests (Requires internet access):
```bash
cd backend
mvn test -Dtest=*LiveTest
```
* `FmiWeatherAdapterLiveTest`: Tests live connection to FMI WFS.
* `DigitrafficAdapterLiveTest`: Tests live connection to Digitraffic REST.
* `TourServiceLiveTest`: Tests live end-to-end integration through `TourService`.
