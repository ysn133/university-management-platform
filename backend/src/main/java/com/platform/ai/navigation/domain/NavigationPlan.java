package com.platform.ai.navigation.domain;

import java.util.List;

public record NavigationPlan(
    AiInteractionMode mode,
    List<NavigationPlanStep> steps,
    String route,
    String message,
    String additionalKnowledgeQuery
) {
    public NavigationPlan {
        mode = mode == null ? AiInteractionMode.NAVIGATE : mode;
        steps = steps == null ? List.of() : List.copyOf(steps);
        route = route == null ? "" : route.trim();
        message = message == null ? "" : message.trim();
        additionalKnowledgeQuery = additionalKnowledgeQuery == null
            ? ""
            : additionalKnowledgeQuery.trim();
    }

    public NavigationPlan(List<NavigationPlanStep> steps, String route, String message) {
        this(AiInteractionMode.NAVIGATE, steps, route, message, "");
    }

    public NavigationPlan(
        AiInteractionMode mode,
        List<NavigationPlanStep> steps,
        String route,
        String message
    ) {
        this(mode, steps, route, message, "");
    }

    public boolean requiresMoreKnowledge() {
        return !additionalKnowledgeQuery.isBlank();
    }
}
