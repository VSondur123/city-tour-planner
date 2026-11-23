package fi.citytour.planner.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class PlannerPropertiesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Set<ConstraintViolation<PlannerProperties>> validate(Consumer<PlannerProperties.Planning> change) {
        PlannerProperties properties = new PlannerProperties();
        change.accept(properties.getPlanning());
        return validator.validate(properties);
    }

    private static Set<String> invalidFields(Set<ConstraintViolation<PlannerProperties>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());
    }

    @Test
    void defaultsAreValid() {
        assertThat(validate(planning -> { })).isEmpty();
    }

    @Test
    void defaultsAreTheAgreedValues() {
        PlannerProperties.Planning planning = new PlannerProperties().getPlanning();

        assertThat(planning.getRainThresholdMm()).isEqualTo(0.5);
        assertThat(planning.getWalkingSpeedKmh()).isEqualTo(5.0);
        assertThat(planning.getTrafficDistanceMeters()).isEqualTo(200.0);
        assertThat(planning.getMaxOpenEndedAgeDays()).isEqualTo(30);
    }

    @Test
    void aWalkingSpeedOfZeroIsRejected() {
        assertThat(invalidFields(validate(planning -> planning.setWalkingSpeedKmh(0))))
                .containsExactly("planning.walkingSpeedKmh");
    }

    @Test
    void aNegativeWalkingSpeedIsRejected() {
        assertThat(invalidFields(validate(planning -> planning.setWalkingSpeedKmh(-3))))
                .containsExactly("planning.walkingSpeedKmh");
    }

    @Test
    void aNegativeRainThresholdIsRejected() {
        assertThat(invalidFields(validate(planning -> planning.setRainThresholdMm(-1))))
                .containsExactly("planning.rainThresholdMm");
    }

    @Test
    void aRainThresholdOfZeroIsAllowed() {
        assertThat(validate(planning -> planning.setRainThresholdMm(0))).isEmpty();
    }

    @Test
    void aTrafficDistanceOfZeroOrLessIsRejected() {
        assertThat(invalidFields(validate(planning -> planning.setTrafficDistanceMeters(0))))
                .containsExactly("planning.trafficDistanceMeters");
        assertThat(invalidFields(validate(planning -> planning.setTrafficDistanceMeters(-5))))
                .containsExactly("planning.trafficDistanceMeters");
    }

    @Test
    void anAgeLimitOfZeroOrLessIsRejected() {
        assertThat(invalidFields(validate(planning -> planning.setMaxOpenEndedAgeDays(0))))
                .containsExactly("planning.maxOpenEndedAgeDays");
    }
}
