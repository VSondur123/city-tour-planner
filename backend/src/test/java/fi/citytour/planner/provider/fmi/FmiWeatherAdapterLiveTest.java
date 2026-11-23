package fi.citytour.planner.provider.fmi;

import fi.citytour.planner.client.ApiHttpClient;
import fi.citytour.planner.client.ApiRequestFactory;
import fi.citytour.planner.config.PlannerProperties;
import fi.citytour.planner.model.weather.HourlyForecast;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Tag("integration")
class FmiWeatherAdapterLiveTest {

    @Autowired
    private FmiWeatherAdapter fmiWeatherAdapter;

    @Test
    void retrievesRealForecastFromFmi() {
        // Query today's date (or tomorrow) for Tampere
        LocalDate today = LocalDate.now();
        List<HourlyForecast> forecasts = fmiWeatherAdapter.getForecast("tampere", today);

        // Harmonine forecast should provide forecasts for today if within horizon
        System.out.println("Live FMI forecast count for " + today + ": " + forecasts.size());
        for (HourlyForecast hf : forecasts) {
            System.out.println("  " + hf.time() + " -> Temp: " + hf.temperature() + "°C, Rain: " + hf.precipitationAmount() + "mm, Wind: " + hf.wind() + "m/s, Cloud: " + hf.cloudCover() + "%");
        }

        // Verify that if forecasts are present, temperature and fields are populated
        if (!forecasts.isEmpty()) {
            assertThat(forecasts.get(0).temperature()).isNotNull();
            assertThat(forecasts.get(0).time()).isNotNull();
        }
    }
}
