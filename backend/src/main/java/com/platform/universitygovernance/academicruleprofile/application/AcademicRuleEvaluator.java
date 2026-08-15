package com.platform.universitygovernance.academicruleprofile.application;

import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfile;
import com.platform.universitygovernance.academicruleprofile.domain.rules.AcademicDecisionRule;
import com.platform.universitygovernance.academicruleprofile.domain.rules.AcademicMetric;
import com.platform.universitygovernance.academicruleprofile.domain.rules.AcademicRuleOutcome;
import com.platform.universitygovernance.academicruleprofile.domain.rules.ComparisonExpression;
import com.platform.universitygovernance.academicruleprofile.domain.rules.LogicalExpression;
import com.platform.universitygovernance.academicruleprofile.domain.rules.RuleExpression;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AcademicRuleEvaluator {

    private final AcademicProfileVariableResolver variableResolver;

    public AcademicRuleEvaluator(AcademicProfileVariableResolver variableResolver) {
        this.variableResolver = variableResolver;
    }

    public Optional<AcademicRuleOutcome> evaluate(
        List<AcademicDecisionRule> rules,
        Map<AcademicMetric, BigDecimal> metrics,
        AcademicRuleProfile profile
    ) {
        return rules.stream()
            .filter(AcademicDecisionRule::enabled)
            .sorted(Comparator.comparingInt(AcademicDecisionRule::priority))
            .filter(rule -> evaluateExpression(rule.expression(), metrics, profile))
            .map(AcademicDecisionRule::outcome)
            .findFirst();
    }

    private boolean evaluateExpression(
        RuleExpression expression,
        Map<AcademicMetric, BigDecimal> metrics,
        AcademicRuleProfile profile
    ) {
        if (expression instanceof ComparisonExpression comparison) {
            BigDecimal left = metrics.get(comparison.left());
            if (left == null) {
                return false;
            }
            BigDecimal right = comparison.rightProfileVariable() == null
                ? comparison.literalValue()
                : variableResolver.resolve(comparison.rightProfileVariable(), profile);
            if (right == null) {
                return false;
            }
            int result = left.compareTo(right);
            return switch (comparison.operator()) {
                case GREATER_THAN -> result > 0;
                case GREATER_THAN_OR_EQUAL -> result >= 0;
                case LESS_THAN -> result < 0;
                case LESS_THAN_OR_EQUAL -> result <= 0;
                case EQUAL -> result == 0;
                case NOT_EQUAL -> result != 0;
            };
        }
        LogicalExpression logical = (LogicalExpression) expression;
        return switch (logical.operator()) {
            case AND -> logical.children().stream().allMatch(child ->
                evaluateExpression(child, metrics, profile));
            case OR -> logical.children().stream().anyMatch(child ->
                evaluateExpression(child, metrics, profile));
        };
    }
}
