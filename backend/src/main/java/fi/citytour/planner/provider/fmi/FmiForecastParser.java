package fi.citytour.planner.provider.fmi;

import fi.citytour.planner.model.weather.HourlyForecast;
import fi.citytour.planner.provider.ProviderException;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Parses XML responses from the FMI (Finnish Meteorological Institute) WFS multipointcoverage
 * stored query into domain {@link HourlyForecast} records.
 */
@Component
public class FmiForecastParser {

    /**
     * Parses the FMI WFS XML string for the specified target date and time zone.
     *
     * @param xml        the XML response body from FMI
     * @param targetDate the local date of the tour
     * @param zone       the local time zone (e.g. Europe/Helsinki)
     * @return an ordered list of hourly forecasts for the target date, or an empty list if no data
     *         or only NaNs (beyond forecast horizon) were returned
     * @throws ProviderException if FMI returned an ExceptionReport or the XML is malformed
     */
    public List<HourlyForecast> parse(String xml, LocalDate targetDate, ZoneId zone) {
        if (xml == null || xml.isBlank()) {
            return List.of();
        }

        Document document = parseXml(xml);
        checkForExceptionReport(document);

        NodeList positionNodes = document.getElementsByTagNameNS("*", "positions");
        NodeList tupleNodes = document.getElementsByTagNameNS("*", "doubleOrNilReasonTupleList");

        if (positionNodes.getLength() == 0 || tupleNodes.getLength() == 0) {
            return List.of();
        }

        String positionsText = positionNodes.item(0).getTextContent();
        String tuplesText = tupleNodes.item(0).getTextContent();

        if (positionsText == null || positionsText.isBlank() || tuplesText == null || tuplesText.isBlank()) {
            return List.of();
        }

        Map<String, Integer> fieldIndices = extractFieldIndices(document);
        Integer tempIndex = fieldIndices.get("temperature");
        if (tempIndex == null) {
            throw new ProviderException("FMI", "Temperature field missing from FMI coverage definition");
        }
        Integer precipIndex = fieldIndices.get("precipitationamount");
        Integer cloudIndex = fieldIndices.get("totalcloudcover");
        Integer windIndex = fieldIndices.get("windspeedms");

        String[] posTokens = positionsText.trim().split("\\s+");
        String[] tupleTokens = tuplesText.trim().split("\\s+");

        int numFields = fieldIndices.size();
        if (numFields == 0) {
            return List.of();
        }

        // Each position is: lat lon epochSeconds (3 tokens per position)
        int numPositions = posTokens.length / 3;
        int numTupleRows = tupleTokens.length / numFields;
        int rowCount = Math.min(numPositions, numTupleRows);

        List<HourlyForecast> result = new ArrayList<>();

        for (int r = 0; r < rowCount; r++) {
            long epochSeconds;
            try {
                epochSeconds = Long.parseLong(posTokens[r * 3 + 2]);
            } catch (NumberFormatException e) {
                continue;
            }

            OffsetDateTime time = Instant.ofEpochSecond(epochSeconds).atZone(zone).toOffsetDateTime();
            if (!time.toLocalDate().equals(targetDate)) {
                continue;
            }

            int rowOffset = r * numFields;
            String tempToken = tupleTokens[rowOffset + tempIndex];
            if (isMissingOrNan(tempToken)) {
                continue;
            }

            Double temp = parseDouble(tempToken);
            if (temp == null || temp.isNaN()) {
                continue;
            }

            Double precip = 0.0;
            if (precipIndex != null) {
                String precipToken = tupleTokens[rowOffset + precipIndex];
                if (!isMissingOrNan(precipToken)) {
                    Double parsed = parseDouble(precipToken);
                    if (parsed != null && !parsed.isNaN()) {
                        precip = parsed;
                    }
                }
            }

            Double cloud = 0.0;
            if (cloudIndex != null) {
                String cloudToken = tupleTokens[rowOffset + cloudIndex];
                if (!isMissingOrNan(cloudToken)) {
                    Double parsed = parseDouble(cloudToken);
                    if (parsed != null && !parsed.isNaN()) {
                        cloud = parsed;
                    }
                }
            }

            Double wind = 0.0;
            if (windIndex != null) {
                String windToken = tupleTokens[rowOffset + windIndex];
                if (!isMissingOrNan(windToken)) {
                    Double parsed = parseDouble(windToken);
                    if (parsed != null && !parsed.isNaN()) {
                        wind = parsed;
                    }
                }
            }

            result.add(new HourlyForecast(
                    time,
                    round2(temp),
                    round2(precip),
                    round2(cloud),
                    round2(wind)
            ));
        }

        return List.copyOf(result);
    }

    private Document parseXml(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xml)));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ProviderException("FMI", "Failed to parse FMI XML: " + e.getMessage(), e);
        }
    }

    private void checkForExceptionReport(Document document) {
        NodeList exceptions = document.getElementsByTagNameNS("*", "ExceptionReport");
        if (exceptions.getLength() == 0) {
            exceptions = document.getElementsByTagName("ExceptionReport");
        }
        if (exceptions.getLength() > 0) {
            NodeList textNodes = document.getElementsByTagNameNS("*", "ExceptionText");
            if (textNodes.getLength() == 0) {
                textNodes = document.getElementsByTagName("ExceptionText");
            }
            List<String> messages = new ArrayList<>();
            for (int i = 0; i < textNodes.getLength(); i++) {
                String text = textNodes.item(i).getTextContent();
                if (text != null && !text.isBlank()) {
                    messages.add(text.trim());
                }
            }
            String detail = messages.isEmpty() ? "Unknown FMI service error" : String.join("; ", messages);
            throw new ProviderException("FMI", detail);
        }
    }

    private Map<String, Integer> extractFieldIndices(Document document) {
        Map<String, Integer> indices = new LinkedHashMap<>();
        NodeList fields = document.getElementsByTagNameNS("*", "field");
        for (int i = 0; i < fields.getLength(); i++) {
            Element field = (Element) fields.item(i);
            String name = field.getAttribute("name");
            if (name != null && !name.isBlank()) {
                indices.put(name.toLowerCase(Locale.ROOT), i);
            }
        }
        return indices;
    }

    private boolean isMissingOrNan(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.equals("nan") || lower.equals("nil") || lower.equals("missing");
    }

    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double round2(Double value) {
        if (value == null || Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return Math.round(value * 100.0) / 100.0;
    }
}
