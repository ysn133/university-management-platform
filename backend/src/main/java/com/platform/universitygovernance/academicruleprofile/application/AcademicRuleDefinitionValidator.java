package com.platform.universitygovernance.academicruleprofile.application;

import com.platform.universitygovernance.academicruleprofile.domain.rules.AcademicDecisionRule;
import com.platform.universitygovernance.academicruleprofile.domain.rules.AcademicMetric;
import com.platform.universitygovernance.academicruleprofile.domain.rules.AcademicRuleOutcome;
import com.platform.universitygovernance.academicruleprofile.domain.rules.AcademicRuleSet;
import com.platform.universitygovernance.academicruleprofile.domain.rules.ComparisonExpression;
import com.platform.universitygovernance.academicruleprofile.domain.rules.LogicalExpression;
import com.platform.universitygovernance.academicruleprofile.domain.rules.RuleExpression;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AcademicRuleDefinitionValidator {

    private static final int MAXIMUM_EXPRESSION_DEPTH = 8;
    private static final int MAXIMUM_RULES_PER_CATEGORY = 30;

    public void validate(AcademicRuleSet ruleSet) {
        if (ruleSet == null) {
            throw badRequest("Academic rule definition is required");
        }
        validateCategory(ruleSet.moduleRules(), Set.of(
            AcademicRuleOutcome.MODULE_VALIDATED,
            AcademicRuleOutcome.MODULE_NON_VALIDATED
        ), Set.of(
            AcademicMetric.MODULE_FINAL_GRADE,
            AcademicMetric.MODULE_INSCRIPTION_NUMBER
        ), "module");
        if (ruleSet.hasSharedSemesterRules()) {
            validateSemesterRules(ruleSet.semesterRules(), "semester");
        } else {
            validateSemesterRules(ruleSet.autumnSemesterRules(), "Autumn semester");
            validateSemesterRules(ruleSet.springSemesterRules(), "Spring semester");
        }
        validateCategory(ruleSet.academicLevelRules(), Set.of(
            AcademicRuleOutcome.ACADEMIC_LEVEL_VALIDATED,
            AcademicRuleOutcome.ACADEMIC_LEVEL_VALIDATED_BY_COMPENSATION,
            AcademicRuleOutcome.ACADEMIC_LEVEL_NON_VALIDATED
        ), Set.of(
            AcademicMetric.ANNUAL_AVERAGE,
            AcademicMetric.INDIVIDUALLY_VALIDATED_MODULE_COUNT,
            AcademicMetric.MINIMUM_NON_VALIDATED_MODULE_GRADE,
            AcademicMetric.NON_VALIDATED_SEMESTER_COUNT
        ), "academic level");
        validateCategory(ruleSet.progressionRules(), Set.of(
            AcademicRuleOutcome.PROMOTED,
            AcademicRuleOutcome.PROMOTED_WITH_DEBT,
            AcademicRuleOutcome.REPEAT,
            AcademicRuleOutcome.FAILED
        ), Set.of(
            AcademicMetric.ACADEMIC_LEVEL_VALIDATED,
            AcademicMetric.OUTSTANDING_MODULE_COUNT,
            AcademicMetric.EXHAUSTED_MODULE_INSCRIPTION_COUNT,
            AcademicMetric.NON_VALIDATED_SEMESTER_COUNT,
            AcademicMetric.ANNUAL_AVERAGE
        ), "progression");
    }

    private void validateSemesterRules(List<AcademicDecisionRule> rules, String category) {
        validateCategory(rules, Set.of(
            AcademicRuleOutcome.SEMESTER_VALIDATED,
            AcademicRuleOutcome.SEMESTER_VALIDATED_BY_COMPENSATION,
            AcademicRuleOutcome.SEMESTER_NON_VALIDATED
        ), Set.of(
            AcademicMetric.SEMESTER_AVERAGE,
            AcademicMetric.INDIVIDUALLY_VALIDATED_MODULE_COUNT,
            AcademicMetric.NON_VALIDATED_MODULE_COUNT,
            AcademicMetric.MINIMUM_NON_VALIDATED_MODULE_GRADE
        ), category);
    }

    private void validateCategory(
        List<AcademicDecisionRule> rules,
        Set<AcademicRuleOutcome> allowedOutcomes,
        Set<AcademicMetric> allowedMetrics,
        String category
    ) {
        if (rules == null || rules.isEmpty()) {
            throw badRequest("At least one " + category + " rule is required");
        }
        if (rules.size() > MAXIMUM_RULES_PER_CATEGORY) {
            throw badRequest("Too many " + category + " rules");
        }
        Set<Integer> priorities = new HashSet<>();
        for (AcademicDecisionRule rule : rules) {
            if (rule == null || rule.name() == null || rule.name().isBlank()) {
                throw badRequest("Every " + category + " rule requires a name");
            }
            if (rule.priority() < 0 || !priorities.add(rule.priority())) {
                throw badRequest("Rule priorities must be non-negative and unique within " + category);
            }
            if (!allowedOutcomes.contains(rule.outcome())) {
                throw badRequest("Invalid outcome for " + category + " rule " + rule.name());
            }
            validateExpression(rule.expression(), allowedMetrics, 1);
        }
    }

    private void validateExpression(
        RuleExpression expression,
        Set<AcademicMetric> allowedMetrics,
        int depth
    ) {
        if (expression == null || depth > MAXIMUM_EXPRESSION_DEPTH) {
            throw badRequest("Rule expression is empty or nested too deeply");
        }
        if (expression instanceof ComparisonExpression comparison) {
            if (comparison.left() == null || comparison.operator() == null) {
                throw badRequest("Every comparison requires a metric and operator");
            }
            if (!allowedMetrics.contains(comparison.left())) {
                throw badRequest(
                    "Metric " + comparison.left() + " is not available in this rule category"
                );
            }
            boolean hasVariable = comparison.rightProfileVariable() != null;
            boolean hasLiteral = comparison.literalValue() != null;
            if (hasVariable == hasLiteral) {
                throw badRequest("A comparison requires either one profile variable or one literal value");
            }
            return;
        }
        LogicalExpression logical = (LogicalExpression) expression;
        if (logical.operator() == null
            || logical.children() == null
            || logical.children().isEmpty()) {
            throw badRequest("Logical expressions require an operator and child expressions");
        }
        logical.children().forEach(child -> validateExpression(
            child,
            allowedMetrics,
            depth + 1
        ));
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
