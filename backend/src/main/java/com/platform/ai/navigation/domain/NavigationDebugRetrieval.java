package com.platform.ai.navigation.domain;

import java.util.List;

public record NavigationDebugRetrieval(
    String query,
    long durationMs,
    int matchCount,
    int contextCharacters,
    List<NavigationDebugKnowledgeMatch> matches
) {
    public NavigationDebugRetrieval {
        matches = List.copyOf(matches);
    }
}
