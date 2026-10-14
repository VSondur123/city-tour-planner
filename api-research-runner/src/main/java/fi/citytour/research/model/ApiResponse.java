
package fi.citytour.research.model;

/**
 * Represents the HTTP response received from an external API.
 *
 * <p>Stores the response status code, content type, and decoded
 * response body for further processing by the API research runner.</p>
 *
 * @param statusCode the HTTP status code returned by the API
 * @param contentType the content type of the response body
 * @param body the response body converted to a string
 */
public record ApiResponse(
        int statusCode,
        String contentType,
        String body
) {}
