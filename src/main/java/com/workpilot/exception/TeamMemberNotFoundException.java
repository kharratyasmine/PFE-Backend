package com.workpilot.exception;

public class TeamMemberNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public TeamMemberNotFoundException(String message) {
        super(message);
    }
}
