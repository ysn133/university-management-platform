package com.platform.ai.navigation.domain;

import java.util.List;
import tools.jackson.databind.JsonNode;

public record NavigationPlanStep(
    String id,
    String path,
    JsonNode queryParameters,
    String forEach,
    List<NavigationPlanMatch> matches
) {
    public NavigationPlanStep {
        forEach = forEach == null ? "" : forEach.trim();
        matches = matches == null ? List.of() : List.copyOf(matches);
    }
}
