package fi.citytour.planner.demo;

import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;
import fi.citytour.planner.model.weather.HourlyForecast;
import fi.citytour.planner.provider.TrafficProvider;
import fi.citytour.planner.provider.WeatherProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/** Demo replacements for the FMI and Digitraffic adapters. */
public final class DemoProviders {

    private DemoProviders() {}

    /** Returns the fixed forecast whatever the city and date. */
    @Component
    @Primary
    @Profile("demo")
    @RequiredArgsConstructor
    public static class Weather implements WeatherProvider {

        private final DemoData data;

        @Override
        public List<HourlyForecast> getForecast(String city, LocalDate date) {
            return data.forecast();
        }
    }

    /** Returns the fixed announcements whatever the city. */
    @Component
    @Primary
    @Profile("demo")
    @RequiredArgsConstructor
    public static class Traffic implements TrafficProvider {

        private final DemoData data;

        @Override
        public List<TrafficAnnouncement> getAnnouncements(String city, Language preferredLanguage) {
            return data.announcements();
        }
    }
}
