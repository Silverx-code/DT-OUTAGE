package com.gridline.dtoutage.exception;

/**
 * Thrown when a report would violate the one-active-outage-per-DT rule.
 * In the happy path the UI already warns the user before they submit (see
 * DtOutageService#hasActiveOutage), so reaching this exception in practice
 * means two people raced each other — exactly the case the DB partial
 * unique index exists to catch atomically.
 */
public class DuplicateActiveOutageException extends RuntimeException {

    private final String existingOutageRef;

    public DuplicateActiveOutageException(String dtCode, String existingOutageRef) {
        super("DT " + dtCode + " already has an active outage: " + existingOutageRef);
        this.existingOutageRef = existingOutageRef;
    }

    public String getExistingOutageRef() {
        return existingOutageRef;
    }
}
