package com.platform.ai.navigation.domain;

import java.util.List;

public record NavigationDebugExecution(
    String label,
    long durationMs,
    int status,
    String outcome,
    List<NavigationApiCall> apiCalls
) {
    public NavigationDebugExecution {
        apiCalls = List.copyOf(apiCalls);
    }
}
