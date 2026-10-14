
package fi.citytour.research.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds configuration properties for the API research runner.
 *
 * <p>Properties are loaded from the {@code research} section of
 * {@code application.yml} and mapped to Java fields by Spring Boot.</p>
 *
 * <p>The configuration includes HTTP timeout settings, report output
 * settings, and the API sources and endpoints to test.</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "research")
public class ResearchProperties {

    /** HTTP client configuration, including connection and request timeouts. */
    private Http http = new Http();

    /** Settings that control the generated test report. */
    private Report report = new Report();

    /**
     * Configured API sources, indexed by source name.
     *
     * <p>A {@link LinkedHashMap} preserves insertion order, allowing sources
     * to be processed in their configured order.</p>
     */
    private Map<String, Source> sources = new LinkedHashMap<>();

    /**
     * Contains HTTP client timeout settings.
     */
    @Getter
    @Setter
    public static class Http {

        /** Maximum time, in seconds, to establish a connection. */
        private int connectTimeoutSeconds = 10;

        /** Maximum time, in seconds, allowed for an individual request. */
        private int requestTimeoutSeconds = 20;
    }

    /**
     * Contains settings for generating the API research report.
     */
    @Getter
    @Setter
    public static class Report {

        /** Directory where the generated report is stored. */
        private String outputDirectory = "reports";

        /** Maximum number of characters retained in each response preview. */
        private int responsePreviewMaxLength = 500;
    }

    /**
     * Represents an external API source and its configured endpoints.
     *
     * <p>Each source has an enabled flag, a base URL, and a map of endpoints
     * to test.</p>
     */
    @Getter
    @Setter
    public static class Source {

        /** Indicates whether this API source should be tested. */
        private boolean enabled;

        /** Base URL shared by the source's endpoints. */
        private String baseUrl = "";

        /** Endpoints belonging to this API source, indexed by test name. */
        private Map<String, Endpoint> endpoints = new LinkedHashMap<>();
    }

    /**
     * Represents an individual endpoint configured for API testing.
     */
    @Getter
    @Setter
    public static class Endpoint {

        /** Relative path appended to the source's base URL. */
        private String path;

        /** HTTP method used to invoke the endpoint, such as GET or POST. */
        private String method;

        /** Query parameters appended to the endpoint URI. */
        private Map<String, String> parameters = new LinkedHashMap<>();
    }
}
