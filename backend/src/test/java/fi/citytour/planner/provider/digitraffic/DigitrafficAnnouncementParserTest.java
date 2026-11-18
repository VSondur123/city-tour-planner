package fi.citytour.planner.provider.digitraffic;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.citytour.planner.model.common.Language;
import fi.citytour.planner.model.traffic.Coordinate;
import fi.citytour.planner.model.traffic.TrafficAnnouncement;
import fi.citytour.planner.provider.ProviderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DigitrafficAnnouncementParserTest {

    private DigitrafficAnnouncementParser parser;

    @BeforeEach
    void setUp() {
        parser = new DigitrafficAnnouncementParser(new ObjectMapper());
    }

    @Test
    void returnsEmptyListWhenInputNullOrBlank() {
        assertThat(parser.parse(null, "tampere", Language.EN)).isEmpty();
        assertThat(parser.parse("", "tampere", Language.EN)).isEmpty();
        assertThat(parser.parse("   ", "tampere", Language.EN)).isEmpty();
        assertThat(parser.parse("{}", null, Language.EN)).isEmpty();
        assertThat(parser.parse("{}", "   ", Language.EN)).isEmpty();
    }

    @Test
    void parsesAndFiltersAnnouncementsByMunicipality() {
        String json = """
                {
                  "type": "FeatureCollection",
                  "features": [
                    {
                      "type": "Feature",
                      "geometry": {
                        "type": "LineString",
                        "coordinates": [
                          [23.771694, 61.498527],
                          [23.775850, 61.498877]
                        ]
                      },
                      "properties": {
                        "situationId": "GUID1",
                        "situationType": "traffic announcement",
                        "trafficAnnouncementType": "general",
                        "announcements": [
                          {
                            "language": "fi",
                            "title": "Itsenäisyydenkatu, Tampere. Liikennetiedote.",
                            "locationDetails": {
                              "roadAddressLocation": {
                                "primaryPoint": {
                                  "municipality": "Tampere",
                                  "roadName": "Itsenäisyydenkatu"
                                },
                                "secondaryPoint": {
                                  "municipality": "Tampere"
                                }
                              }
                            },
                            "features": [
                              { "name": "Tie on suljettu liikenteeltä" }
                            ],
                            "timeAndDuration": {
                              "startTime": "2026-06-01T03:00:00Z",
                              "endTime": "2026-06-02T12:00:00Z"
                            }
                          }
                        ]
                      }
                    },
                    {
                      "type": "Feature",
                      "geometry": {
                        "type": "Point",
                        "coordinates": [24.9384, 60.1699]
                      },
                      "properties": {
                        "situationId": "GUID2",
                        "announcements": [
                          {
                            "language": "fi",
                            "title": "Mannerheimintie, Helsinki.",
                            "locationDetails": {
                              "roadAddressLocation": {
                                "primaryPoint": {
                                  "municipality": "Helsinki"
                                }
                              }
                            }
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        List<TrafficAnnouncement> announcements = parser.parse(json, "tampere", Language.FI);

        assertThat(announcements).hasSize(1);
        TrafficAnnouncement item = announcements.get(0);
        assertThat(item.type()).isEqualTo("Tie on suljettu liikenteeltä");
        assertThat(item.description()).isEqualTo("Itsenäisyydenkatu, Tampere. Liikennetiedote.");
        assertThat(item.area()).isEqualTo("Tampere");
        assertThat(item.language()).isEqualTo("fi");
        assertThat(item.startTime()).isEqualTo(OffsetDateTime.parse("2026-06-01T03:00:00Z"));
        assertThat(item.endTime()).isEqualTo(OffsetDateTime.parse("2026-06-02T12:00:00Z"));

        // Coordinates check: GeoJSON is [lon, lat], Coordinate record is (lat, lon)
        assertThat(item.geometry().paths()).hasSize(1);
        List<Coordinate> path = item.geometry().paths().get(0);
        assertThat(path).hasSize(2);
        assertThat(path.get(0).lat()).isEqualTo(61.498527);
        assertThat(path.get(0).lon()).isEqualTo(23.771694);
        assertThat(path.get(1).lat()).isEqualTo(61.498877);
        assertThat(path.get(1).lon()).isEqualTo(23.775850);
    }

    @Test
    void prefersRequestedLanguageWhenMultipleLanguagesAvailable() {
        String json = """
                {
                  "features": [
                    {
                      "geometry": { "type": "Point", "coordinates": [23.7, 61.5] },
                      "properties": {
                        "announcements": [
                          {
                            "language": "fi",
                            "title": "Tietyö Tampereella",
                            "locationDetails": {
                              "roadAddressLocation": {
                                "primaryPoint": { "municipality": "Tampere" }
                              }
                            }
                          },
                          {
                            "language": "en",
                            "title": "Road work in Tampere",
                            "locationDetails": {
                              "roadAddressLocation": {
                                "primaryPoint": { "municipality": "Tampere" }
                              }
                            }
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        List<TrafficAnnouncement> announcements = parser.parse(json, "tampere", Language.EN);

        assertThat(announcements).hasSize(1);
        assertThat(announcements.get(0).language()).isEqualTo("en");
        assertThat(announcements.get(0).description()).isEqualTo("Road work in Tampere");
    }

    @Test
    void fallsBackToTrafficAnnouncementTypeWhenFeatureNameMissing() {
        String json = """
                {
                  "features": [
                    {
                      "geometry": null,
                      "properties": {
                        "trafficAnnouncementType": "accident report",
                        "announcements": [
                          {
                            "language": "fi",
                            "title": "Onnettomuus Tampereella",
                            "locationDetails": {
                              "roadAddressLocation": {
                                "primaryPoint": { "municipality": "Tampere" }
                              }
                            }
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        List<TrafficAnnouncement> announcements = parser.parse(json, "tampere", null);

        assertThat(announcements).hasSize(1);
        assertThat(announcements.get(0).type()).isEqualTo("accident report");
        assertThat(announcements.get(0).geometry().paths()).isEmpty();
    }

    @Test
    void returnsEmptyListWhenNoFeaturesMatchTargetCity() {
        String json = """
                {
                  "features": [
                    {
                      "properties": {
                        "announcements": [
                          {
                            "language": "fi",
                            "title": "Turku tiedote",
                            "locationDetails": {
                              "roadAddressLocation": {
                                "primaryPoint": { "municipality": "Turku" }
                              }
                            }
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        List<TrafficAnnouncement> announcements = parser.parse(json, "tampere", Language.FI);
        assertThat(announcements).isEmpty();
    }

    @Test
    void throwsProviderExceptionOnMalformedJson() {
        assertThatThrownBy(() -> parser.parse("invalid-json-text", "tampere", Language.FI))
                .isInstanceOf(ProviderException.class)
                .hasMessageContaining("Failed to parse Digitraffic JSON")
                .satisfies(e -> assertThat(((ProviderException) e).getSource()).isEqualTo("Digitraffic"));
    }
}
