package fi.citytour.planner.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Holds configuration properties for the City Tour Planner backend. */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "planner")
public class PlannerProperties {

    /** Time zone in which tour dates and times are interpreted. */
    private String zone = "Europe/Helsinki";

    /** Location of the JSON file that contains the places of a city. */
    private String placesFile = "classpath:data/places.json";

    /** HTTP client configuration, including connection and request timeouts. */
    private Http http = new Http();

    /** Cross-origin settings for the frontend. */
    private Cors cors = new Cors();

    /** Configuration of the external data providers. */
    private Providers providers = new Providers();

    /** Settings of the tour planning logic. */
    @Valid
    private Planning planning = new Planning();

    /** Contains HTTP client timeout settings. */
    @Getter
    @Setter
    public static class Http {

        /** Maximum time, in seconds, to establish a connection. */
        private int connectTimeoutSeconds = 10;

        /** Maximum time, in seconds, allowed for an individual request. */
        private int requestTimeoutSeconds = 20;
    }

    /** Contains the browser origins that may call the API. */
    @Getter
    @Setter
    public static class Cors {

        /** Origins allowed to call the API, such as the frontend dev server. */
        private List<String> allowedOrigins = List.of("http://localhost:5173");
    }

    /** Contains the settings of the tour planning logic. */
    @Getter
    @Setter
    public static class Planning {

        /** Precipitation (mm per hour) above which a forecast hour counts as rainy. */
        @PositiveOrZero
        private double rainThresholdMm = 0.5;

        /** Walking speed in km/h, used to turn a distance into travel time. */
        @Positive
        private double walkingSpeedKmh = 5.0;

        /** Distance in metres within which a traffic announcement affects a place. */
        @Positive
        private double trafficDistanceMeters = 200.0;
    }

    /** Groups the configuration of every external provider. */
    @Getter
    @Setter
    public static class Providers {

        /** Finnish Meteorological Institute weather forecast API. */
        private Provider fmi = new Provider();

        /** Digitraffic road traffic announcements API. */
        private Provider digitraffic = new Provider();
    }

    /** Represents an external provider endpoint. */
    @Getter
    @Setter
    public static class Provider {

        /** Base URL of the provider. */
        private String baseUrl = "";

        /** Relative path appended to the base URL. */
        private String path = "";

        /** Fixed query parameters sent with every request. */
        private Map<String, String> parameters = new LinkedHashMap<>();
    }
}
