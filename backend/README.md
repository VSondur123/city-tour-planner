# City Tour Planner: Backend

How to run the backend.

## Requirements

- **Java 22** (JDK). Check with `java -version`.
- **Maven 3.9 or newer**. Check with `mvn -version`.
- **Internet connection.** Maven downloads dependencies on the first build, and the service calls FMI and Digitraffic.
- Port **8080** must be free.

If `mvn -version` shows a Java version other than 22, point `JAVA_HOME` at a JDK 22 first. On macOS:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 22)
```

## Run

From this `backend/` directory, either run directly with Maven:

```bash
mvn spring-boot:run
```

or build a jar and run it:

```bash
mvn clean package -DskipTests
java -jar target/city-tour-planner-backend-0.0.1-SNAPSHOT.jar
```

The backend has started when the log shows `Started CityTourPlannerApplication`. Stop it with `Ctrl+C`.

## Check that it is running

In a second terminal:

```bash
curl http://localhost:8080/api/cities
```

Expected output:

```json
[{"id":"tampere","name":"Tampere"},{"id":"helsinki","name":"Helsinki"},{"id":"vaasa","name":"Vaasa"},{"id":"oulu","name":"Oulu"}]
```

## Test the API

With the backend running, try each endpoint. The full request and response description is in `docs/api-contract.md`.

A tour uses live FMI and Digitraffic data, so the result depends on the date and on the current traffic. FMI forecasts reach about two days ahead; replace `<YYYY-MM-DD>` with a date within that range.

**Cities** (Tampere, Helsinki, Vaasa and Oulu):

```bash
curl http://localhost:8080/api/cities
```

**Places of a city**, in English (default) or Finnish (`language=fi`):

```bash
curl "http://localhost:8080/api/places?city=tampere"
curl "http://localhost:8080/api/places?city=helsinki&language=fi"
```

Expected: a list of places with `id`, `name`, `category`, `indoor`, `lat`, `lon`, `visitMinutes` and `link`.

**Tour**:

```bash
curl -X POST http://localhost:8080/api/tour \
  -H "Content-Type: application/json" \
  -d '{"city":"tampere","date":"<YYYY-MM-DD>","startTime":"13:00","durationHours":4,"language":"en","factors":["WEATHER","TRAFFIC"]}'
```

Expected: `stops`, `forecast`, `announcements`, `weatherStatus`, `trafficStatus` and an `explanation`. Things to try:

- `"language":"fi"` gives the Finnish place names and explanation.
- `"factors":[]` plans without weather and traffic; the forecast and announcements are still returned.
- A `date` more than two days ahead gives `"weatherStatus":"EMPTY"` and an empty `forecast`; the tour is still planned. Any future date is accepted.
- `"city":"helsinki"`, `"vaasa"` or `"oulu"` plan a tour in that city.

The response shape is in `docs/samples/tour-result.sample.json`.

**Errors.** Every error has the shape `{"status", "code", "errors"}` with codes only:

```bash
# 404 CITY_NOT_FOUND
curl -i -X POST http://localhost:8080/api/tour -H "Content-Type: application/json" \
  -d '{"city":"atlantis","date":"<YYYY-MM-DD>","startTime":"13:00","durationHours":4}'

# 400 VALIDATION_FAILED (durationHours above 12)
curl -i -X POST http://localhost:8080/api/tour -H "Content-Type: application/json" \
  -d '{"city":"tampere","date":"<YYYY-MM-DD>","startTime":"13:00","durationHours":13}'

# 400 VALIDATION_FAILED (date before today, error code PAST)
curl -i -X POST http://localhost:8080/api/tour -H "Content-Type: application/json" \
  -d '{"city":"tampere","date":"2020-01-01","startTime":"13:00","durationHours":4}'

# 400 UNSUPPORTED_LANGUAGE
curl -i "http://localhost:8080/api/places?city=tampere&language=sv"

# 400 MALFORMED_REQUEST
curl -i -X POST http://localhost:8080/api/tour -H "Content-Type: application/json" -d '{not json'

# 400 VALIDATION_FAILED (city missing)
curl -i "http://localhost:8080/api/places"
```

## Run the automated tests

```bash
mvn test
```

This runs the unit tests. They need no network. To check the real FMI and Digitraffic services as well (they are left out of `mvn test`):

```bash
mvn test -Dtest='*LiveTest'
```

What the tests cover is listed in `docs/testing.md`. The settings you can change in `src/main/resources/application.yml` are listed in `docs/configuration.md`.
