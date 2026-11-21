package fi.citytour.planner.model.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocalizedTextTest {

    @Test
    void returnsTheTextInTheRequestedLanguage() {
        LocalizedText text = new LocalizedText("Hello", "Hei");

        assertThat(text.resolve(Language.EN)).isEqualTo("Hello");
        assertThat(text.resolve(Language.FI)).isEqualTo("Hei");
    }

    @Test
    void fallsBackToTheOtherLanguageWhenTheRequestedOneIsMissing() {
        assertThat(new LocalizedText("Hello", null).resolve(Language.FI)).isEqualTo("Hello");
        assertThat(new LocalizedText(null, "Hei").resolve(Language.EN)).isEqualTo("Hei");
    }

    @Test
    void treatsBlankTextAsMissing() {
        assertThat(new LocalizedText("Hello", "  ").resolve(Language.FI)).isEqualTo("Hello");
    }
}
