package fi.citytour.planner.service;

/** Signals that a tour was requested for a date that has already passed. */
public class PastDateException extends RuntimeException {

    /** Creates the exception for a date before today. */
    public PastDateException() {
        super("Tour date is in the past");
    }
}
