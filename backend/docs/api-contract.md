# Backend API contract (for the frontend)

The requests the frontend sends and the responses it receives, so the UI can build its own model. Example bodies were captured from the running backend. A full response is in `samples/tour-result.sample.json`.

- **Base URL (local):** `http://localhost:8080`
- **Format:** JSON, `Content-Type: application/json`
- **CORS:** `http://localhost:5173` (the Vite dev server) is allowed. For another origin, change `planner.cors.allowed-origins` in the backend's `application.yml`.

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/cities` | Cities the user can choose from (fills the city dropdown) |
| GET | `/api/places` | Places of a city, in one language |
| POST | `/api/tour` | Generate a tour |

## How the screens use the API

| UI action | Call |
|---|---|
| Input screen opens | `GET /api/cities` |
| User presses Generate Tour | `POST /api/tour` (one request) |
| Language toggle changed on the results screen | `POST /api/tour` again with the new `language` |
| Planning mode (weather or traffic) switched on the results screen | `POST /api/tour` again with the new `factors` |
| Display layer (show or hide weather, traffic, places) switched | **No call.** Weather and traffic are always in the response, so hide or show them locally |

The loading screen shows three sources, but there is only **one** request. The per-source outcome arrives together in the final response (`weatherStatus`, `trafficStatus`). The UI therefore cannot show real per-source progress, only a single waiting state or a simulated one.

---

## GET /api/cities

Request: no parameters.

Response `200`:

```json
[
  { "id": "tampere", "name": "Tampere" }
]
```

| Field | Type | Meaning |
|---|---|---|
| `id` | string | Identifier to send as `city` in other requests |
| `name` | string | Name to display |

---

## GET /api/places

Query parameters:

| Parameter | Required | Meaning |
|---|---|---|
| `city` | yes | City `id` from `/api/cities` |
| `language` | no | `en` or `fi`, case-insensitive. Default `en` |

Example: `GET /api/places?city=tampere&language=fi`

Response `200`: a list of places, with text already in the requested language.

```json
[
  {
    "id": "vt-10715",
    "name": "Museokeskus Vapriikki",
    "category": "MUSEUM",
    "indoor": true,
    "lat": 61.50300215,
    "lon": 23.760054,
    "visitMinutes": 90,
    "link": "https://visittampere.fi/kayntikohde/museokeskus-vapriikki/"
  }
]
```

| Field | Type | Meaning |
|---|---|---|
| `id` | string | Unique place identifier |
| `name` | string | Name in the requested language |
| `category` | string | `MUSEUM`, `LANDMARK`, `NATURE` or `OTHER`. A code, so the UI translates the label |
| `indoor` | boolean | Whether the visit takes place indoors |
| `lat`, `lon` | number | Position on the map |
| `visitMinutes` | number | Planned length of a visit, in minutes |
| `link` | string | Web page of the place, in the requested language |

---

## POST /api/tour

### Request body

```json
{
  "city": "tampere",
  "date": "2026-11-03",
  "startTime": "13:00",
  "durationHours": 4,
  "language": "en",
  "factors": ["WEATHER", "TRAFFIC"]
}
```

| Field | Required | Type | Meaning |
|---|---|---|---|
| `city` | yes | string, not blank | City `id` from `/api/cities` |
| `date` | yes | string `YYYY-MM-DD` | Date of the tour |
| `startTime` | yes | string `HH:mm` | Local start time |
| `durationHours` | yes | integer 1 to 12 | Length of the tour in hours (the form offers 2, 4 and 6) |
| `language` | no | `"en"` or `"fi"` | Language of the response text. Default `"en"` |
| `factors` | no | list of `"WEATHER"`, `"TRAFFIC"` | Which data the planner may use. Default: both. `[]` means neither |

Notes:
- `language` is lowercase; `factors` values are **uppercase and case-sensitive**.
- `factors` controls only what the planner uses. Weather and traffic are returned whatever it contains.
- FMI forecasts only about two days ahead. For a later date the forecast is empty (`weatherStatus: "EMPTY"`), and the weather chart needs an empty state.

### Response `200`

Shortened example (one stop, one forecast hour, one announcement):

```json
{
  "language": "en",
  "factors": ["WEATHER", "TRAFFIC"],
  "stops": [
    {
      "place": {
        "id": "vt-10715",
        "name": "Museum Centre Vapriikki",
        "category": "MUSEUM",
        "indoor": true,
        "lat": 61.50300215,
        "lon": 23.760054,
        "visitMinutes": 90,
        "link": "https://visittampere.fi/en/destination/museum-centre-vapriikki/"
      },
      "start": "2026-11-03T13:00:00+02:00",
      "end": "2026-11-03T14:30:00+02:00"
    }
  ],
  "forecast": [
    {
      "time": "2026-11-03T13:00:00+02:00",
      "temperature": 6.2,
      "precipitationAmount": 0.09,
      "cloudCover": 96.2,
      "wind": 6.35
    }
  ],
  "announcements": [
    {
      "type": "Tie on suljettu liikenteeltä",
      "description": "Itsenäisyydenkatu, Tampere. Liikennetiedote. ",
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
  ],
  "weatherStatus": "AVAILABLE",
  "trafficStatus": "AVAILABLE",
  "trafficTextLanguage": "fi",
  "explanation": "..."
}
```

Top-level fields:

| Field | Type | Meaning |
|---|---|---|
| `language` | `"en"` or `"fi"` | Language of the text in this response |
| `factors` | list of strings | The factors the planner used |
| `stops` | list of Stop | Ordered stops of the tour. **Empty until stop selection is implemented** |
| `forecast` | list of Forecast | Hourly forecast for the requested date. Empty if unavailable, and **empty until the FMI adapter is implemented** |
| `announcements` | list of Announcement | Active road traffic announcements in the city. **Empty until the Digitraffic adapter is implemented** |
| `weatherStatus` | Status | Outcome of getting the weather |
| `trafficStatus` | Status | Outcome of getting the traffic announcements |
| `trafficTextLanguage` | string or `null` | Language of the announcement text. `null` when there are no announcements |
| `explanation` | string | Text for the "Why this tour" box, in `language`. May be empty for now |

**Status** (`weatherStatus`, `trafficStatus`):

| Value | Meaning | What the UI should do |
|---|---|---|
| `AVAILABLE` | Data was returned | Show it |
| `EMPTY` | The provider answered but had nothing | Show an empty state, **not** a warning |
| `UNAVAILABLE` | The provider could not be reached or read | Show the warning banner ("…unavailable. Showing the tour without … information") |

**Stop**

| Field | Type | Meaning |
|---|---|---|
| `place` | Place | Same shape as in `GET /api/places` |
| `start`, `end` | string, ISO 8601 with offset | Planned visit times, in Finnish time |

**Forecast**

| Field | Type | Meaning |
|---|---|---|
| `time` | string, ISO 8601 with offset | The hour the values apply to, in Finnish time |
| `temperature` | number or `null` | Air temperature, °C |
| `precipitationAmount` | number or `null` | Forecast precipitation amount. **There is no rain probability** in the data |
| `cloudCover` | number or `null` | Cloud cover, percent |
| `wind` | number or `null` | Wind speed, m/s |

Any forecast value can be `null`.

**Announcement**

| Field | Type | Meaning |
|---|---|---|
| `type` | string | Kind of announcement, such as "road closed". In the language of the text, mostly Finnish |
| `description` | string | Title of the announcement |
| `area` | string | Municipality |
| `language` | string | Language of `type` and `description`, such as `"fi"` |
| `startTime` | string (UTC, ends in `Z`) or `null` | When the situation starts |
| `endTime` | string (UTC) or `null` | When it ends. Often `null` |
| `geometry.paths` | list of lists of `{lat, lon}` | Shape on the map. Draw each inner list as one line. A single point is a path with one coordinate |

The announcement text is mostly Finnish whatever `language` was requested. The UI can use `trafficTextLanguage` to tell the user.

### Error responses

Every error has the same shape. The backend sends **codes only**, never sentences, so the UI decides what to show and in which language.

```json
{
  "status": 400,
  "code": "VALIDATION_FAILED",
  "errors": [
    { "field": "durationHours", "code": "MIN" },
    { "field": "city", "code": "REQUIRED" }
  ]
}
```

| Field | Meaning |
|---|---|
| `status` | The HTTP status code |
| `code` | The kind of error (table below) |
| `errors` | The fields concerned, each with a `field` name and a `code`. Empty when no field applies |

| Status | `code` | When | `errors` |
|---|---|---|---|
| `400` | `VALIDATION_FAILED` | A required field is missing or a value is out of range | One entry per field. Codes: `REQUIRED`, `MIN`, `MAX` |
| `400` | `UNSUPPORTED_LANGUAGE` | `language` is not `en` or `fi` (body or query) | `field: "language"`, `code: "UNSUPPORTED"` |
| `400` | `MALFORMED_REQUEST` | The body is not valid JSON, a date or time has the wrong format, or a factor is unknown | Empty |
| `404` | `CITY_NOT_FOUND` | `city` is not one returned by `/api/cities` | `field: "city"`, `code: "UNKNOWN"` |

`GET /api/places` with the `city` parameter missing returns `VALIDATION_FAILED` with `field: "city"`, `code: "REQUIRED"`.

If a provider (FMI or Digitraffic) fails, the request does **not** fail. It returns `200` with `weatherStatus` or `trafficStatus` set to `UNAVAILABLE`.
