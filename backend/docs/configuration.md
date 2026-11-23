# Configuration

The backend reads `src/main/resources/application.yml` (prefix `planner`). The values are read at startup, so a change needs a restart; no recompiling is needed. The `planning` values are validated at startup, and a wrong value stops the application with a clear error.

| Setting | Default | Meaning |
|---|---|---|
| `planner.zone` | `Europe/Helsinki` | Time zone in which tour dates and times are read, and in which forecast hours are compared with the tour |
| `planner.places-file` | `classpath:data/places.json` | The places, as a map of city name to places. The city name is the key of the map; its lower-case form is the city `id` used by the API |
| `planner.http.connect-timeout-seconds` | `10` | Longest wait to connect to an external API |
| `planner.http.request-timeout-seconds` | `20` | Longest wait for one external API response |
| `planner.cors.allowed-origins` | `http://localhost:5173` | Origins that may call the API |
| `planner.planning.rain-threshold-mm` | `0.5` | Precipitation in mm per hour above which an hour counts as rainy. Must be 0 or more |
| `planner.planning.walking-speed-kmh` | `5.0` | Walking speed used to turn a distance into travel time. Must be above 0 |
| `planner.planning.traffic-distance-meters` | `200` | A place within this distance of an announcement is skipped for a visit that overlaps it. Must be above 0 |
| `planner.planning.max-open-ended-age-days` | `30` | An announcement with no end time that started more than this many days before the tour is ignored when planning (it is still returned). Must be above 0 |
| `planner.providers.fmi`, `planner.providers.digitraffic` | see the file | Base URL, path and fixed query parameters of each provider |

## Adding a city

Add a key to `places.json` with the places of that city (see an existing city for the fields). The city appears in `GET /api/cities` after a restart. The city name must be the name FMI and Digitraffic use for the municipality (for example `Helsinki`), because it is sent to FMI and matched against Digitraffic's municipality field. `indoor` and `visitMinutes` are curated by the team.
