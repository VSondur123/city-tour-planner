package fi.citytour.planner.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures cross-origin access so the React frontend can call the API from its own origin
 * during development.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final PlannerProperties plannerProperties;

    /** Allows the configured origins to call the {@code /api} endpoints. */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(plannerProperties.getCors()
                        .getAllowedOrigins()
                        .toArray(String[]::new))
                .allowedMethods("GET", "POST");
    }
}
