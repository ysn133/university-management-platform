package com.platform.universitygovernance.academicruleprofile.domain.rules;

import java.util.List;

public record LogicalExpression(
    LogicalOperator operator,
    List<RuleExpression> children
) implements RuleExpression {
}
