package fi.citytour.planner.model.tour;

import fi.citytour.planner.model.common.Language;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * A request to generate a tour. Language defaults to English and factors to both weather and
 * traffic; an empty factor set means the planner uses neither.
 */
public record TourRequest(
        @NotBlank String city,
        @NotNull LocalDate date,
        @NotNull LocalTime startTime,
        @Min(1) @Max(12) int durationHours,
        Language language,
        Set<Factor> factors
) {

    public TourRequest {
        language = language == null ? Language.EN : language;

        factors = factors == null
                ? Collections.unmodifiableSet(EnumSet.allOf(Factor.class))
                : Collections.unmodifiableSet(factors.isEmpty()
                        ? EnumSet.noneOf(Factor.class)
                        : EnumSet.copyOf(factors));
    }
}
