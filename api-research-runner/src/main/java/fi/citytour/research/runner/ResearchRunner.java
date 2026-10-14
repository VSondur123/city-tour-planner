
package fi.citytour.research.runner;

import fi.citytour.research.client.ApiHttpClient;
import fi.citytour.research.client.ApiRequestFactory;
import fi.citytour.research.config.ResearchProperties;
import fi.citytour.research.model.ApiTestReport;
import fi.citytour.research.model.ApiTestResult;
import fi.citytour.research.report.ApiTestReportWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Executes the configured API research test suite for the City Tour Planner.
 *
 * <p>The runner reads API source and endpoint configurations from
 * {@link ResearchProperties}, tests each endpoint, collects the results,
 * calculates a summary, and writes the completed report as a JSON file.</p>
 *
 * <p>Disabled API sources are skipped. Individual endpoint failures are
 * recorded as test results so that the remaining tests can continue.</p>
 *
 * <p>This component implements {@link ApplicationRunner}, so Spring Boot
 * executes the test suite after the application context has started.</p>
 */
@Component
@RequiredArgsConstructor
public class ResearchRunner implements ApplicationRunner {

    private final ApiHttpClient apiHttpClient;
    private final ApiRequestFactory apiRequestFactory;
    private final ResearchProperties researchProperties;
    private final ApiTestReportWriter apiTestReportWriter;

    /**
     * Executes all enabled endpoint tests and generates the JSON test report.
     *
     * <p>For each enabled source, this method invokes {@link #testEndpoint}
     * for every configured endpoint. It then calculates the total number
     * of tests, successful tests, and failed tests before passing the report
     * to {@link ApiTestReportWriter}.</p>
     *
     * <p>The report-writing failure is caught and logged without preventing
     * the final test summary from being printed.</p>
     *
     * @param args command-line arguments provided by Spring Boot
     */
    @Override
    public void run(ApplicationArguments args) {
        System.out.println("\n=== CITY TOUR PLANNER: API RESEARCH ===\n");

        List<ApiTestResult> results = new ArrayList<>();

        for (Map.Entry<String, ResearchProperties.Source> sourceEntry
                : researchProperties.getSources().entrySet()) {

            String sourceName = sourceEntry.getKey();
            ResearchProperties.Source source = sourceEntry.getValue();

            if (!source.isEnabled()) {
                System.out.println("Skipping disabled source: " + sourceName);
                continue;
            }

            for (Map.Entry<String, ResearchProperties.Endpoint> endpointEntry
                    : source.getEndpoints().entrySet()) {

                ApiTestResult result = testEndpoint(
                        sourceName,
                        endpointEntry.getKey(),
                        source,
                        endpointEntry.getValue()
                );

                results.add(result);
            }
        }

        int totalTests = results.size();

        int passed = (int) results.stream()
                .filter(ApiTestResult::successful)
                .count();

        int failed = totalTests - passed;

        ApiTestReport report = new ApiTestReport(
                "City Tour Planner API Research",
                OffsetDateTime.now(ZoneId.systemDefault()),
                new ApiTestReport.Summary(totalTests, passed, failed),
                results
        );

        try {
            var reportPath = apiTestReportWriter.write(report);
            System.out.println("Report saved to: " + reportPath.toAbsolutePath());
        } catch (IOException exception) {
            System.err.println("Failed to write JSON report: "
                    + exception.getMessage());
        }

        System.out.println("\n=== RESEARCH SUMMARY ===");
        System.out.println("Total tests: " + totalTests);
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("\n=== RESEARCH RUN COMPLETE ===");
    }

    /**
     * Tests a single configured API endpoint and creates its result record.
     *
     * <p>The method builds the endpoint URI, sends the HTTP request, measures
     * the elapsed time, and captures the HTTP status, content type, and a
     * bounded response preview. An HTTP status in the 200–299 range is
     * considered successful.</p>
     *
     * <p>If an exception occurs while constructing the URI or executing the
     * request, the exception details are stored in a failed result rather
     * than propagated. This allows the test suite to continue.</p>
     *
     * @param sourceName name of the configured API source
     * @param testName name identifying the endpoint test
     * @param source configuration of the API source, including its base URL
     * @param endpoint configuration of the endpoint, including its path,
     *                 HTTP method, and query parameters
     * @return the result of the endpoint test, including status, timing,
     *         response preview, and any error details
     */
    private ApiTestResult testEndpoint(
            String sourceName,
            String testName,
            ResearchProperties.Source source,
            ResearchProperties.Endpoint endpoint) {

        Instant start = Instant.now();
        URI uri = null;

        try {
            uri = apiRequestFactory.buildUri(source, endpoint);

            var response = apiHttpClient.send(uri, endpoint.getMethod());

            Duration responseTime = Duration.between(start, Instant.now());

            String body = response.body();

            int previewLength = researchProperties.getReport()
                    .getResponsePreviewMaxLength();

            String preview = body.length() > previewLength
                    ? body.substring(0, previewLength)
                    : body;

            return new ApiTestResult(
                    sourceName + "/" + testName,
                    uri.toString(),
                    response.statusCode() >= 200
                            && response.statusCode() < 300,
                    response.statusCode(),
                    response.contentType(),
                    responseTime,
                    List.of(),
                    preview,
                    null,
                    start
            );

        } catch (Exception exception) {
            return new ApiTestResult(
                    sourceName + "/" + testName,
                    uri != null
                            ? uri.toString()
                            : source.getBaseUrl() + endpoint.getPath(),
                    false,
                    null,
                    null,
                    Duration.between(start, Instant.now()),
                    List.of(),
                    null,
                    exception.getClass().getSimpleName()
                            + ": " + exception.getMessage(),
                    start
            );
        }
    }
}
