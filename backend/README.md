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
[{"id":"tampere","name":"Tampere"}]
```

## Smoke test with demo data

The weather and traffic adapters and the tour strategies are not implemented yet, so a normal run returns an empty tour. To see the full response shape, start the backend with the `demo` profile. It replaces the adapters and the strategy with the fixed output in `src/main/resources/demo/adapter-and-strategy-output.json`:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

In a second terminal:

```bash
curl -X POST http://localhost:8080/api/tour \
  -H "Content-Type: application/json" \
  -d '{"city":"tampere","date":"2026-11-03","startTime":"13:00","durationHours":4,"language":"en","factors":["WEATHER","TRAFFIC"]}'
```

Expected output: three stops (Vapriikki, Moomin Museum, Tampere Cathedral), four forecast hours and one announcement, with `weatherStatus` and `trafficStatus` both `AVAILABLE`. The full response is in `docs/samples/tour-result.demo.json`. The demo data ignores the requested date, so the times are always 3 Nov 2026.
