package fi.citytour.research;

import fi.citytour.research.config.ResearchProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ResearchProperties.class)
public class ApiResearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiResearchApplication.class, args);
    }
}