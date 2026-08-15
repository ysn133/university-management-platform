package com.platform.universitygovernance.academicruleprofile.domain.rules;

import java.math.BigDecimal;

public record ComparisonExpression(
    AcademicMetric left,
    ComparisonOperator operator,
    ProfileVariable rightProfileVariable,
    BigDecimal literalValue
) implements RuleExpression {
}
