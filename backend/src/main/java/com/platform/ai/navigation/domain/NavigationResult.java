package com.platform.ai.navigation.domain;

import java.util.List;

public record NavigationResult(
    AiInteractionMode mode,
    String route,
    String message,
    String answerContext,
    List<NavigationApiCall> apiCalls,
    NavigationDebugTrace diagnostics
) {

    public NavigationResult {
        apiCalls = List.copyOf(apiCalls);
    }

    public NavigationResult(String route, String message, List<NavigationApiCall> apiCalls) {
        this(AiInteractionMode.NAVIGATE, route, message, "", apiCalls, null);
    }

    public NavigationResult(
        AiInteractionMode mode,
        String route,
        String message,
        String answerContext,
        List<NavigationApiCall> apiCalls
    ) {
        this(mode, route, message, answerContext, apiCalls, null);
    }

    public NavigationResult(String route, String message, int ignoredApiCallCount) {
        this(route, message, List.of());
    }

    public int apiCallCount() {
        return apiCalls.size();
    }
}
