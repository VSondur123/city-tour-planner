package fi.citytour.planner.client;

/** An HTTP response from an external API, with its body decoded to text. */
public record ApiResponse(
        int statusCode,
        String contentType,
        String body
) {}
