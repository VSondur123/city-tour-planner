package fi.citytour.planner.model.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LanguageTest {

    @Test
    void findsALanguageByCodeIgnoringCase() {
        assertThat(Language.fromCode("en")).isEqualTo(Language.EN);
        assertThat(Language.fromCode("FI")).isEqualTo(Language.FI);
    }

    @Test
    void rejectsAnUnknownCode() {
        assertThatThrownBy(() -> Language.fromCode("sv"))
                .isInstanceOf(UnsupportedLanguageException.class);
    }

    @Test
    void rejectsANullCode() {
        assertThatThrownBy(() -> Language.fromCode(null))
                .isInstanceOf(UnsupportedLanguageException.class);
    }

    @Test
    void exposesTheLowercaseCode() {
        assertThat(Language.FI.code()).isEqualTo("fi");
    }
}
