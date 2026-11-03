package fi.citytour.planner.provider;

/** Signals that an external data provider could not supply usable data. */
public class ProviderException extends RuntimeException {

    private final String source;

    /** Creates an exception that names the failing provider. */
    public ProviderException(String source, String message) {
        super(source + ": " + message);
        this.source = source;
    }

    /** Creates an exception that names the failing provider and keeps the original cause. */
    public ProviderException(String source, String message, Throwable cause) {
        super(source + ": " + message, cause);
        this.source = source;
    }

    /** Returns the name of the provider that failed. */
    public String getSource() {
        return source;
    }
}
