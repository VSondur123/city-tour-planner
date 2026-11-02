package fi.citytour.planner.client;

import fi.citytour.planner.config.PlannerProperties;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiRequestFactoryTest {

    private final ApiRequestFactory factory = new ApiRequestFactory();

    @Test
    void combinesFixedAndRequestParameters() {
        PlannerProperties.Provider provider = new PlannerProperties.Provider();
        provider.setBaseUrl("https://example.org");
        provider.setPath("/wfs");
        provider.getParameters().put("service", "WFS");

        URI uri = factory.buildUri(provider, Map.of("place", "tampere"));

        assertThat(uri.toString())
                .startsWith("https://example.org/wfs?")
                .contains("service=WFS")
                .contains("place=tampere");
    }

    @Test
    void letsARequestParameterReplaceAFixedOne() {
        PlannerProperties.Provider provider = new PlannerProperties.Provider();
        provider.setBaseUrl("https://example.org");
        provider.setPath("/wfs");
        provider.getParameters().put("place", "helsinki");

        URI uri = factory.buildUri(provider, Map.of("place", "tampere"));

        assertThat(uri.getQuery()).isEqualTo("place=tampere");
    }
}
