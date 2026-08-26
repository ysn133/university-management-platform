package com.platform.ai.navigation.domain;

public class AiNavigationFailureException extends RuntimeException {

    private final int status;
    private final NavigationDebugTrace diagnostics;

    public AiNavigationFailureException(int status, String message) {
        this(status, message, null);
    }

    public AiNavigationFailureException(
        int status,
        String message,
        NavigationDebugTrace diagnostics
    ) {
        super(message);
        this.status = status;
        this.diagnostics = diagnostics;
    }

    public int status() {
        return status;
    }

    public NavigationDebugTrace diagnostics() {
        return diagnostics;
    }
}
