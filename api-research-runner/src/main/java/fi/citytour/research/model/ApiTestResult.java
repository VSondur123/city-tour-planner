
package fi.citytour.research.model;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Represents the result of testing a single API endpoint.
 *
 * <p>Captures endpoint identification, success status, HTTP response
 * information, execution time, potentially useful response fields,
 * a limited response preview, error details, and the test timestamp.</p>
 *
 * @param source the API source and endpoint identifier
 * @param endpoint the complete endpoint URI used for the test
 * @param successful whether the endpoint test was successful
 * @param statusCode the HTTP status code, or {@code null} if no response
 *                   status was obtained
 * @param contentType the response content type, or {@code null} if unavailable
 * @param responseTime the time taken to execute the endpoint test
 * @param usefulFields field names identified as potentially useful
 *                     for the City Tour Planner
 * @param responsePreview a limited preview of the response body
 * @param error the error description, or {@code null} if no error occurred
 * @param testedAt the timestamp recorded when the test started
 */
public record ApiTestResult(
        String source,
        String endpoint,
        boolean successful,
        Integer statusCode,
        String contentType,
        Duration responseTime,
        List<String> usefulFields,
        String responsePreview,
        String error,
        Instant testedAt
) {}
