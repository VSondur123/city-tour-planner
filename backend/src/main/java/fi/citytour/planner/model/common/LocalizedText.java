package fi.citytour.planner.model.common;

/** Text available in each supported language. */
public record LocalizedText(String en, String fi) {

    /** Returns the text in the requested language. */
    public String resolve(Language language) {
        String preferred = language == Language.FI ? fi : en;
        String fallback = language == Language.FI ? en : fi;

        return preferred != null && !preferred.isBlank() ? preferred : fallback;
    }
}
