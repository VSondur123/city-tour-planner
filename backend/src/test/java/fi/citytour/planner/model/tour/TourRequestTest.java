package fi.citytour.planner.model.tour;

import fi.citytour.planner.model.common.Language;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TourRequestTest {

    private static TourRequest request(Language language, Set<Factor> factors) {
        return new TourRequest(
                "tampere", LocalDate.of(2026, 11, 21), LocalTime.of(10, 0), 4, language, factors);
    }

    @Test
    void defaultsToEnglishAndBothFactors() {
        TourRequest request = request(null, null);

        assertThat(request.language()).isEqualTo(Language.EN);
        assertThat(request.factors()).containsExactlyInAnyOrder(Factor.WEATHER, Factor.TRAFFIC);
    }

    @Test
    void keepsAnEmptyFactorSetEmpty() {
        assertThat(request(Language.FI, Set.of()).factors()).isEmpty();
    }

    @Test
    void keepsTheGivenFactorsAndLanguage() {
        TourRequest request = request(Language.FI, EnumSet.of(Factor.TRAFFIC));

        assertThat(request.language()).isEqualTo(Language.FI);
        assertThat(request.factors()).containsExactly(Factor.TRAFFIC);
    }

    @Test
    void factorsCannotBeModified() {
        TourRequest request = request(null, null);

        assertThatThrownBy(() -> request.factors().add(Factor.WEATHER))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
