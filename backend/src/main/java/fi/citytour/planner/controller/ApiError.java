package fi.citytour.planner.controller;

import java.util.List;

/**
 * The body of every error response. It holds codes only; the frontend decides what message to
 * show.
 */
public record ApiError(int status, String code, List<FieldIssue> errors) {

    /** One problem with one request field. */
    public record FieldIssue(String field, String code) {}
}
