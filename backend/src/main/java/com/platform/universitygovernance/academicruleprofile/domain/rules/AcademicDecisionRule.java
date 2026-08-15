package com.platform.universitygovernance.academicruleprofile.domain.rules;

public record AcademicDecisionRule(
    String name,
    int priority,
    AcademicRuleOutcome outcome,
    boolean enabled,
    RuleExpression expression
) {
}
