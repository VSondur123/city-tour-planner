
package fi.citytour.research.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fi.citytour.research.config.ResearchProperties;
import fi.citytour.research.model.ApiTestReport;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Writes API research test reports to JSON files.
 *
 * <p>Uses Jackson to serialize an {@link ApiTestReport} into formatted JSON.
 * The output directory is obtained from {@link ResearchProperties},
 * allowing the report location to be configured in application.yml.</p>
 *
 * <p>The report is written to {@code api-test-suite-results.json}.
 * If that file already exists, its contents are overwritten.</p>
 */
@Component
public class ApiTestReportWriter {

    /** Jackson mapper configured for formatted JSON output. */
    private final ObjectMapper objectMapper;

    /** Application configuration containing report output settings. */
    private final ResearchProperties researchProperties;

    /**
     * Creates a report writer using the configured Jackson mapper and
     * application properties.
     *
     * <p>A copy of the supplied mapper is used so that enabling
     * pretty-printing does not modify the shared mapper configuration.</p>
     *
     * @param objectMapper the Jackson mapper provided by Spring
     * @param researchProperties configuration containing the output directory
     */
    public ApiTestReportWriter(
            ObjectMapper objectMapper,
            ResearchProperties researchProperties) {

        this.objectMapper = objectMapper.copy()
                .enable(SerializationFeature.INDENT_OUTPUT);

        this.researchProperties = researchProperties;
    }

    /**
     * Serializes a test report and writes it to the configured output directory.
     *
     * <p>Creates the output directory if it does not already exist.
     * The report is written to {@code api-test-suite-results.json},
     * replacing any existing file with the same name.</p>
     *
     * @param report the test report to serialize
     * @return the path to the generated JSON report
     * @throws IOException if the directory cannot be created or the report
     *                     cannot be written
     */
    public Path write(ApiTestReport report) throws IOException {

        Path outputDirectory = Paths.get(
                researchProperties.getReport().getOutputDirectory()
        );

        Files.createDirectories(outputDirectory);

        Path reportFile = outputDirectory.resolve(
                "api-test-suite-results.json"
        );

        objectMapper.writeValue(reportFile.toFile(), report);

        return reportFile;
    }
}
