package com.platform.ai.navigation.domain;

public record NavigationDebugModelCall(
    String label,
    long durationMs,
    NavigationPlan plan
) {
}
