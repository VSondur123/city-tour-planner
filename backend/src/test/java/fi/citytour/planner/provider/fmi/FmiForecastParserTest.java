package fi.citytour.planner.provider.fmi;

import fi.citytour.planner.model.weather.HourlyForecast;
import fi.citytour.planner.provider.ProviderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FmiForecastParserTest {

    private FmiForecastParser parser;
    private final ZoneId zone = ZoneId.of("Europe/Helsinki");
    private final LocalDate testDate = LocalDate.of(2026, 11, 16);

    @BeforeEach
    void setUp() {
        parser = new FmiForecastParser();
    }

    @Test
    void returnsEmptyListForNullOrBlankXml() {
        assertThat(parser.parse(null, testDate, zone)).isEmpty();
        assertThat(parser.parse("", testDate, zone)).isEmpty();
        assertThat(parser.parse("   ", testDate, zone)).isEmpty();
    }

    @Test
    void parsesValidFmiCoverageResponse() {
        // 1794852000 is 2026-11-16T18:00:00Z -> 2026-11-16T20:00:00+02:00 in Europe/Helsinki
        // 1794855600 is 2026-11-16T19:00:00Z -> 2026-11-16T21:00:00+02:00 in Europe/Helsinki
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <wfs:FeatureCollection xmlns:wfs="http://www.opengis.net/wfs/2.0"
                                       xmlns:gmlcov="http://www.opengis.net/gmlcov/1.0"
                                       xmlns:swe="http://www.opengis.net/swe/2.0"
                                       xmlns:gml="http://www.opengis.net/gml/3.2">
                    <wfs:member>
                        <gmlcov:MultiPointCoverage>
                            <gmlcov:rangeType>
                                <swe:DataRecord>
                                    <swe:field name="Pressure"/>
                                    <swe:field name="Temperature"/>
                                    <swe:field name="WindSpeedMS"/>
                                    <swe:field name="PrecipitationAmount"/>
                                    <swe:field name="TotalCloudCover"/>
                                </swe:DataRecord>
                            </gmlcov:rangeType>
                            <gml:rangeSet>
                                <gml:DataBlock>
                                    <gml:doubleOrNilReasonTupleList>
                                        1013.2 5.34 4.5 0.25 85.0
                                        1012.8 4.89 3.8 0.0 40.0
                                    </gml:doubleOrNilReasonTupleList>
                                </gml:DataBlock>
                            </gml:rangeSet>
                            <gmlcov:positions>
                                61.49 23.78 1794852000
                                61.49 23.78 1794855600
                            </gmlcov:positions>
                        </gmlcov:MultiPointCoverage>
                    </wfs:member>
                </wfs:FeatureCollection>
                """;

        List<HourlyForecast> forecasts = parser.parse(xml, testDate, zone);

        assertThat(forecasts).hasSize(2);

        HourlyForecast first = forecasts.get(0);
        assertThat(first.time()).isEqualTo(OffsetDateTime.parse("2026-11-16T20:00:00+02:00"));
        assertThat(first.temperature()).isEqualTo(5.34);
        assertThat(first.wind()).isEqualTo(4.5);
        assertThat(first.precipitationAmount()).isEqualTo(0.25);
        assertThat(first.cloudCover()).isEqualTo(85.0);

        HourlyForecast second = forecasts.get(1);
        assertThat(second.time()).isEqualTo(OffsetDateTime.parse("2026-11-16T21:00:00+02:00"));
        assertThat(second.temperature()).isEqualTo(4.89);
        assertThat(second.wind()).isEqualTo(3.8);
        assertThat(second.precipitationAmount()).isEqualTo(0.0);
        assertThat(second.cloudCover()).isEqualTo(40.0);
    }

    @Test
    void filtersOutReadingsFromOtherDates() {
        // Position 1: 2026-11-15T21:00:00Z -> 2026-11-15T23:00:00+02:00 (Previous day)
        // Position 2: 2026-11-16T10:00:00Z -> 2026-11-16T12:00:00+02:00 (Target day)
        // Position 3: 2026-11-16T22:30:00Z -> 2026-11-17T00:30:00+02:00 (Next day)
        long ts1 = 1794776400L;
        long ts2 = 1794823200L;
        long ts3 = 1794868200L;

        String xml = """
                <wfs:FeatureCollection xmlns:wfs="http://www.opengis.net/wfs/2.0"
                                       xmlns:gmlcov="http://www.opengis.net/gmlcov/1.0"
                                       xmlns:swe="http://www.opengis.net/swe/2.0"
                                       xmlns:gml="http://www.opengis.net/gml/3.2">
                    <wfs:member>
                        <gmlcov:MultiPointCoverage>
                            <gmlcov:rangeType>
                                <swe:DataRecord>
                                    <swe:field name="Temperature"/>
                                </swe:DataRecord>
                            </gmlcov:rangeType>
                            <gml:rangeSet>
                                <gml:DataBlock>
                                    <gml:doubleOrNilReasonTupleList>
                                        2.0
                                        5.5
                                        1.0
                                    </gml:doubleOrNilReasonTupleList>
                                </gml:DataBlock>
                            </gml:rangeSet>
                            <gmlcov:positions>
                                61.49 23.78 %d
                                61.49 23.78 %d
                                61.49 23.78 %d
                            </gmlcov:positions>
                        </gmlcov:MultiPointCoverage>
                    </wfs:member>
                </wfs:FeatureCollection>
                """.formatted(ts1, ts2, ts3);

        List<HourlyForecast> forecasts = parser.parse(xml, testDate, zone);

        assertThat(forecasts).hasSize(1);
        assertThat(forecasts.get(0).temperature()).isEqualTo(5.5);
        assertThat(forecasts.get(0).time()).isEqualTo(OffsetDateTime.parse("2026-11-16T12:00:00+02:00"));
    }

    @Test
    void returnsEmptyListWhenAllValuesAreNanBeyondHorizon() {
        String xml = """
                <wfs:FeatureCollection xmlns:wfs="http://www.opengis.net/wfs/2.0"
                                       xmlns:gmlcov="http://www.opengis.net/gmlcov/1.0"
                                       xmlns:swe="http://www.opengis.net/swe/2.0"
                                       xmlns:gml="http://www.opengis.net/gml/3.2">
                    <wfs:member>
                        <gmlcov:MultiPointCoverage>
                            <gmlcov:rangeType>
                                <swe:DataRecord>
                                    <swe:field name="Temperature"/>
                                    <swe:field name="PrecipitationAmount"/>
                                </swe:DataRecord>
                            </gmlcov:rangeType>
                            <gml:rangeSet>
                                <gml:DataBlock>
                                    <gml:doubleOrNilReasonTupleList>
                                        NaN NaN
                                        NaN NaN
                                    </gml:doubleOrNilReasonTupleList>
                                </gml:DataBlock>
                            </gml:rangeSet>
                            <gmlcov:positions>
                                61.49 23.78 1794852000
                                61.49 23.78 1794855600
                            </gmlcov:positions>
                        </gmlcov:MultiPointCoverage>
                    </wfs:member>
                </wfs:FeatureCollection>
                """;

        List<HourlyForecast> forecasts = parser.parse(xml, testDate, zone);
        assertThat(forecasts).isEmpty();
    }

    @Test
    void throwsProviderExceptionWhenExceptionReportReturned() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <ExceptionReport xmlns="http://www.opengis.net/ows/1.1" version="2.0.0">
                    <Exception exceptionCode="OperationParsingFailed">
                        <ExceptionText>No locations found for the place with the requested language!</ExceptionText>
                    </Exception>
                </ExceptionReport>
                """;

        assertThatThrownBy(() -> parser.parse(xml, testDate, zone))
                .isInstanceOf(ProviderException.class)
                .hasMessageContaining("No locations found");
    }

    @Test
    void throwsProviderExceptionOnMalformedXml() {
        String invalidXml = "<wfs:FeatureCollection><unclosed>";

        assertThatThrownBy(() -> parser.parse(invalidXml, testDate, zone))
                .isInstanceOf(ProviderException.class)
                .hasMessageContaining("Failed to parse FMI XML");
    }

    @Test
    void throwsProviderExceptionWhenTemperatureFieldMissing() {
        String xml = """
                <wfs:FeatureCollection xmlns:wfs="http://www.opengis.net/wfs/2.0"
                                       xmlns:gmlcov="http://www.opengis.net/gmlcov/1.0"
                                       xmlns:swe="http://www.opengis.net/swe/2.0"
                                       xmlns:gml="http://www.opengis.net/gml/3.2">
                    <wfs:member>
                        <gmlcov:MultiPointCoverage>
                            <gmlcov:rangeType>
                                <swe:DataRecord>
                                    <swe:field name="Pressure"/>
                                </swe:DataRecord>
                            </gmlcov:rangeType>
                            <gml:rangeSet>
                                <gml:DataBlock>
                                    <gml:doubleOrNilReasonTupleList>1013.2</gml:doubleOrNilReasonTupleList>
                                </gml:DataBlock>
                            </gml:rangeSet>
                            <gmlcov:positions>61.49 23.78 1794852000</gmlcov:positions>
                        </gmlcov:MultiPointCoverage>
                    </wfs:member>
                </wfs:FeatureCollection>
                """;

        assertThatThrownBy(() -> parser.parse(xml, testDate, zone))
                .isInstanceOf(ProviderException.class)
                .hasMessageContaining("Temperature field missing");
    }

    @Test
    void returnsNullForMissingOrNanValuesAndDropsMissingTemperatureRows() {
        // Position 1 (18:00 UTC -> 20:00 local): Valid temperature, but wind, precip, cloud are NaN
        // Position 2 (19:00 UTC -> 21:00 local): Missing/NaN temperature -> should be dropped
        // Position 3 (20:00 UTC -> 22:00 local): Valid complete row
        String xml = """
                <wfs:FeatureCollection xmlns:wfs="http://www.opengis.net/wfs/2.0"
                                       xmlns:gmlcov="http://www.opengis.net/gmlcov/1.0"
                                       xmlns:swe="http://www.opengis.net/swe/2.0"
                                       xmlns:gml="http://www.opengis.net/gml/3.2">
                    <wfs:member>
                        <gmlcov:MultiPointCoverage>
                            <gmlcov:rangeType>
                                <swe:DataRecord>
                                    <swe:field name="Temperature"/>
                                    <swe:field name="WindSpeedMS"/>
                                    <swe:field name="PrecipitationAmount"/>
                                    <swe:field name="TotalCloudCover"/>
                                </swe:DataRecord>
                            </gmlcov:rangeType>
                            <gml:rangeSet>
                                <gml:DataBlock>
                                    <gml:doubleOrNilReasonTupleList>
                                        6.5 NaN NaN NaN
                                        NaN 3.0 1.0 50.0
                                        4.2 2.1 0.5 25.0
                                    </gml:doubleOrNilReasonTupleList>
                                </gml:DataBlock>
                            </gml:rangeSet>
                            <gmlcov:positions>
                                61.49 23.78 1794852000
                                61.49 23.78 1794855600
                                61.49 23.78 1794859200
                            </gmlcov:positions>
                        </gmlcov:MultiPointCoverage>
                    </wfs:member>
                </wfs:FeatureCollection>
                """;

        List<HourlyForecast> forecasts = parser.parse(xml, testDate, zone);

        // Row 2 is dropped, so only 2 rows returned
        assertThat(forecasts).hasSize(2);

        HourlyForecast row1 = forecasts.get(0);
        assertThat(row1.time()).isEqualTo(OffsetDateTime.parse("2026-11-16T20:00:00+02:00"));
        assertThat(row1.temperature()).isEqualTo(6.5);
        assertThat(row1.wind()).isNull();
        assertThat(row1.precipitationAmount()).isNull();
        assertThat(row1.cloudCover()).isNull();

        HourlyForecast row3 = forecasts.get(1);
        assertThat(row3.time()).isEqualTo(OffsetDateTime.parse("2026-11-16T22:00:00+02:00"));
        assertThat(row3.temperature()).isEqualTo(4.2);
        assertThat(row3.wind()).isEqualTo(2.1);
        assertThat(row3.precipitationAmount()).isEqualTo(0.5);
        assertThat(row3.cloudCover()).isEqualTo(25.0);
    }

    @Test
    void convertsCumulativePrecipitationToHourlyDifferences() {
        // Consecutive rows with cumulative precipitation: 1.00, 3.50, 5.25, then reset to 0.50
        String xml = """
                <wfs:FeatureCollection xmlns:wfs="http://www.opengis.net/wfs/2.0"
                                       xmlns:gmlcov="http://www.opengis.net/gmlcov/1.0"
                                       xmlns:swe="http://www.opengis.net/swe/2.0"
                                       xmlns:gml="http://www.opengis.net/gml/3.2">
                    <wfs:member>
                        <gmlcov:MultiPointCoverage>
                            <gmlcov:rangeType>
                                <swe:DataRecord>
                                    <swe:field name="Temperature"/>
                                    <swe:field name="PrecipitationAmount"/>
                                </swe:DataRecord>
                            </gmlcov:rangeType>
                            <gml:rangeSet>
                                <gml:DataBlock>
                                    <gml:doubleOrNilReasonTupleList>
                                        5.0 1.00
                                        5.0 3.50
                                        5.0 5.25
                                        5.0 0.50
                                    </gml:doubleOrNilReasonTupleList>
                                </gml:DataBlock>
                            </gml:rangeSet>
                            <gmlcov:positions>
                                61.49 23.78 1794852000
                                61.49 23.78 1794855600
                                61.49 23.78 1794859200
                                61.49 23.78 1794862800
                            </gmlcov:positions>
                        </gmlcov:MultiPointCoverage>
                    </wfs:member>
                </wfs:FeatureCollection>
                """;

        List<HourlyForecast> forecasts = parser.parse(xml, testDate, zone);

        assertThat(forecasts).hasSize(4);
        assertThat(forecasts.get(0).precipitationAmount()).isEqualTo(1.00); // First row: 1.00
        assertThat(forecasts.get(1).precipitationAmount()).isEqualTo(2.50); // 3.50 - 1.00 = 2.50
        assertThat(forecasts.get(2).precipitationAmount()).isEqualTo(1.75); // 5.25 - 3.50 = 1.75
        assertThat(forecasts.get(3).precipitationAmount()).isEqualTo(0.50); // Reset to 0.50
    }
}
