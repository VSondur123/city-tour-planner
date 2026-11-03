package fi.citytour.planner.controller;

import fi.citytour.planner.controller.ApiError.FieldIssue;
import fi.citytour.planner.model.common.UnsupportedLanguageException;
import fi.citytour.planner.service.CityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/** Turns exceptions into {@link ApiError} responses. */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Unknown city: 404 {@code CITY_NOT_FOUND}. */
    @ExceptionHandler(CityNotFoundException.class)
    public ResponseEntity<ApiError> handleCityNotFound() {
        return error(HttpStatus.NOT_FOUND, "CITY_NOT_FOUND",
                new FieldIssue("city", "UNKNOWN"));
    }

    /** Unsupported language in a query parameter: 400 {@code UNSUPPORTED_LANGUAGE}. */
    @ExceptionHandler(UnsupportedLanguageException.class)
    public ResponseEntity<ApiError> handleUnsupportedLanguage() {
        return error(HttpStatus.BAD_REQUEST, "UNSUPPORTED_LANGUAGE",
                new FieldIssue("language", "UNSUPPORTED"));
    }

    /** Invalid fields in the request body: 400 {@code VALIDATION_FAILED}. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception) {

        FieldIssue[] issues = exception.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> new FieldIssue(
                        error.getField(), constraintCode(error.getCode())))
                .toArray(FieldIssue[]::new);

        return error(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", issues);
    }

    /** Missing query parameter: 400 {@code VALIDATION_FAILED}. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParameter(
            MissingServletRequestParameterException exception) {

        return error(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                new FieldIssue(exception.getParameterName(), "REQUIRED"));
    }

    /**
     * Unreadable body: 400 {@code UNSUPPORTED_LANGUAGE} if the cause is an unsupported
     * language, otherwise {@code MALFORMED_REQUEST}.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(
            HttpMessageNotReadableException exception) {

        for (Throwable cause = exception; cause != null;
                cause = cause.getCause()) {
            if (cause instanceof UnsupportedLanguageException) {
                return handleUnsupportedLanguage();
            }
        }

        return error(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST");
    }

    private ResponseEntity<ApiError> error(
            HttpStatus status, String code, FieldIssue... issues) {

        return ResponseEntity.status(status)
                .body(new ApiError(status.value(), code, List.of(issues)));
    }

    private String constraintCode(String constraint) {
        return switch (constraint == null ? "" : constraint) {
            case "NotBlank", "NotNull" -> "REQUIRED";
            case "Min" -> "MIN";
            case "Max" -> "MAX";
            default -> "INVALID";
        };
    }
}
