package com.platform.ai.navigation.domain;

public record NavigationPlanMatch(
    String field,
    MatchOperator operator,
    String value
) {
    public enum MatchOperator {
        EQUALS,
        EQUALS_IGNORE_CASE,
        CONTAINS_IGNORE_CASE
    }
}
