package fi.citytour.planner.model.common;

/** Thrown when a request names a language the application does not support. */
public class UnsupportedLanguageException extends IllegalArgumentException {

    public UnsupportedLanguageException(String code) {
        super("Unsupported language: " + code);
    }
}
