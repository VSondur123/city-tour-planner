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

            int rowOffset = r * numFields;

            // Temperature is mandatory for a valid forecast row; drop row if missing/NaN
            Double temp = readDouble(tupleTokens, rowOffset, tempIndex);
            if (temp == null || temp.isNaN()) {
                continue;
            }

            // FMI Harmonine PrecipitationAmount is cumulative across the model run.
            // Convert to per-hour precipitation by taking the difference from the previous row.
            Double currentPrecip = readDouble(tupleTokens, rowOffset, precipIndex);
            Double hourlyPrecip = null;
            if (currentPrecip != null && !currentPrecip.isNaN()) {
                if (r == 0) {
                    hourlyPrecip = Math.max(0.0, currentPrecip);
                } else {
                    Double prevPrecip = readDouble(tupleTokens, (r - 1) * numFields, precipIndex);
                    if (prevPrecip == null || prevPrecip.isNaN() || currentPrecip < prevPrecip) {
                        hourlyPrecip = Math.max(0.0, currentPrecip);
                    } else {
                        hourlyPrecip = Math.max(0.0, currentPrecip - prevPrecip);
                    }
                }
            }

            // Cloud cover and wind return null if missing/NaN (not 0.0)
            Double cloud = readDouble(tupleTokens, rowOffset, cloudIndex);
            Double wind = readDouble(tupleTokens, rowOffset, windIndex);

            OffsetDateTime time = Instant.ofEpochSecond(epochSeconds).atZone(zone).toOffsetDateTime();
            if (!time.toLocalDate().equals(targetDate)) {
                continue;
            }

            result.add(new HourlyForecast(
                    time,
                    round2(temp),
                    round2(hourlyPrecip),
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

    /**
     * Reads and parses a numeric value from the tuple tokens, or returns defaultValue if missing, NaN, or out of range.
     */
    private Double readOrDefault(String[] tupleTokens, int rowOffset, Integer fieldIndex, Double defaultValue) {
        if (fieldIndex == null) {
            return defaultValue;
        }
        int index = rowOffset + fieldIndex;
        if (index >= tupleTokens.length) {
            return defaultValue;
        }
        String token = tupleTokens[index];
        if (isMissingOrNan(token)) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(token);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Reads and parses a numeric value from the tuple tokens, or returns null if missing, NaN, or out of range.
     */
    private Double readDouble(String[] tupleTokens, int rowOffset, Integer fieldIndex) {
        return readOrDefault(tupleTokens, rowOffset, fieldIndex, null);
    }

    private boolean isMissingOrNan(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.equals("nan") || lower.equals("nil") || lower.equals("missing");
    }

    private Double round2(Double value) {
        if (value == null || Double.isNaN(value) || Double.isInfinite(value)) {
            return null;
        }
        return Math.round(value * 100.0) / 100.0;
    }
}
