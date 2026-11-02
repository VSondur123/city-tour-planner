package fi.citytour.planner;

import fi.citytour.planner.config.PlannerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PlannerProperties.class)
public class CityTourPlannerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CityTourPlannerApplication.class, args);
    }
}
