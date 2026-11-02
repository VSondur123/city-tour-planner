package fi.citytour.planner.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

/** Languages supported by the application. */
public enum Language {

    EN("en"),
    FI("fi");

    private final String code;

    Language(String code) {
        this.code = code;
    }

    /** Returns the lowercase language code used in JSON and in provider data. */
    @JsonValue
    public String code() {
        return code;
    }

    /** Finds a language by its code, ignoring case. */
    @JsonCreator
    public static Language fromCode(String code) {
        for (Language language : values()) {
            if (language.code.equals(code == null
                    ? null
                    : code.toLowerCase(Locale.ROOT))) {
                return language;
            }
        }
        throw new UnsupportedLanguageException(code);
    }
}
