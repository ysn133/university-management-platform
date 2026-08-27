package com.platform.ai.navigation.domain;

import java.util.List;

public record NavigationDebugTrace(
    String query,
    String currentRoute,
    String startedAt,
    long serverTotalMs,
    List<NavigationDebugRetrieval> retrievals,
    List<NavigationDebugModelCall> modelCalls,
    List<NavigationDebugExecution> executions
) {
    public NavigationDebugTrace {
        retrievals = List.copyOf(retrievals);
        modelCalls = List.copyOf(modelCalls);
        executions = List.copyOf(executions);
    }
}
