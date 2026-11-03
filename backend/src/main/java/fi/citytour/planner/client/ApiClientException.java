package fi.citytour.planner.client;

/**
 * A request to an external API failed: an I/O error, an interruption, or a status outside
 * 200-299. Adapters turn it into a provider failure.
 */
public class ApiClientException extends RuntimeException {

    public ApiClientException(String message) {
        super(message);
    }

    public ApiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
