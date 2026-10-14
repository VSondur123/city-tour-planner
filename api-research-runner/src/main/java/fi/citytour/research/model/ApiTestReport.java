
package fi.citytour.research.model;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Represents the complete report generated after an API research test run.
 *
 * <p>Contains the test suite name, execution timestamp, summary statistics,
 * and individual endpoint test results.</p>
 *
 * @param suiteName the name identifying the test suite
 * @param runAt the date, time, and UTC offset when the report was generated
 * @param summary aggregate statistics for the test run
 * @param results the individual results of the endpoint tests
 */
public record ApiTestReport(
        String suiteName,
        OffsetDateTime runAt,
        Summary summary,
        List<ApiTestResult> results
) {

    /**
     * Summarizes the outcome of an API research test run.
     *
     * @param totalTests the total number of endpoint tests executed
     * @param passed the number of successful endpoint tests
     * @param failed the number of unsuccessful endpoint tests
     */
    public record Summary(
            int totalTests,
            int passed,
            int failed
    ) {}
}
