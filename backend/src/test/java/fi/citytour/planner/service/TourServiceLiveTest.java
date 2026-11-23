package fi.citytour.planner.service;

import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.tour.DataStatus;
import fi.citytour.planner.model.tour.Factor;
import fi.citytour.planner.model.tour.TourRequest;
import fi.citytour.planner.model.tour.TourResult;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Tag("integration")
class TourServiceLiveTest {

    @Autowired
    private TourService tourService;

    @Test
    void testEndToEndTourGenerationWithLiveAdapters() {
        LocalDate today = LocalDate.now();
        TourRequest request = new TourRequest(
                "tampere",
                today,
                LocalTime.of(13, 0),
                4,
                Language.EN,
                Set.of(Factor.WEATHER, Factor.TRAFFIC)
        );

        TourResult result = tourService.generateTour(request);

        assertThat(result).isNotNull();
        assertThat(result.language()).isEqualTo(Language.EN);

        // Verify live weather integration through FmiWeatherAdapter
        System.out.println("End-to-end weather status: " + result.weatherStatus());
        System.out.println("Forecast items count: " + result.forecast().size());
        assertThat(result.weatherStatus()).isIn(DataStatus.AVAILABLE, DataStatus.EMPTY);

        // Verify live traffic integration through DigitrafficAdapter
        System.out.println("End-to-end traffic status: " + result.trafficStatus());
        System.out.println("Announcements items count: " + result.announcements().size());
        assertThat(result.trafficStatus()).isIn(DataStatus.AVAILABLE, DataStatus.EMPTY);
    }
}
