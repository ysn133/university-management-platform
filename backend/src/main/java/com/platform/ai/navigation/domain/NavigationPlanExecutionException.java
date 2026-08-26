package com.platform.ai.navigation.domain;

import java.util.List;

public class NavigationPlanExecutionException extends RuntimeException {

    private final int status;
    private final List<NavigationApiCall> apiCalls;

    public NavigationPlanExecutionException(
        int status,
        String message,
        List<NavigationApiCall> apiCalls
    ) {
        super(message);
        this.status = status;
        this.apiCalls = List.copyOf(apiCalls);
    }

    public int status() {
        return status;
    }

    public List<NavigationApiCall> apiCalls() {
        return apiCalls;
    }
}
