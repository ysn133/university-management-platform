package com.platform.universitygovernance.academicruleprofile.domain.rules;

import java.math.BigDecimal;
import java.util.List;

public final class DefaultAcademicRuleSets {

    private DefaultAcademicRuleSets() {
    }

    public static AcademicRuleSet standard() {
        List<AcademicDecisionRule> semesterRules = List.of(
            rule("All modules validated", 10, AcademicRuleOutcome.SEMESTER_VALIDATED,
                literal(AcademicMetric.NON_VALIDATED_MODULE_COUNT,
                    ComparisonOperator.EQUAL, BigDecimal.ZERO)),
            rule("Semester compensation", 20,
                AcademicRuleOutcome.SEMESTER_VALIDATED_BY_COMPENSATION,
                and(
                    compare(AcademicMetric.SEMESTER_AVERAGE,
                        ComparisonOperator.GREATER_THAN_OR_EQUAL,
                        ProfileVariable.SEMESTER_VALIDATION_AVERAGE),
                    compare(AcademicMetric.INDIVIDUALLY_VALIDATED_MODULE_COUNT,
                        ComparisonOperator.GREATER_THAN_OR_EQUAL,
                        ProfileVariable.MINIMUM_INDIVIDUALLY_VALIDATED_MODULES_PER_SEMESTER),
                    compare(AcademicMetric.NON_VALIDATED_MODULE_COUNT,
                        ComparisonOperator.LESS_THAN_OR_EQUAL,
                        ProfileVariable.MAXIMUM_NON_VALIDATED_MODULES_PER_SEMESTER),
                    compare(AcademicMetric.MINIMUM_NON_VALIDATED_MODULE_GRADE,
                        ComparisonOperator.GREATER_THAN_OR_EQUAL,
                        ProfileVariable.COMPENSATION_MINIMUM_THRESHOLD)
                )),
            rule("Semester not validated", 100,
                AcademicRuleOutcome.SEMESTER_NON_VALIDATED,
                literal(AcademicMetric.NON_VALIDATED_MODULE_COUNT,
                    ComparisonOperator.GREATER_THAN, BigDecimal.ZERO))
        );
        return new AcademicRuleSet(
            List.of(
                rule("Module validated", 10, AcademicRuleOutcome.MODULE_VALIDATED,
                    compare(AcademicMetric.MODULE_FINAL_GRADE,
                        ComparisonOperator.GREATER_THAN_OR_EQUAL,
                        ProfileVariable.MODULE_VALIDATION_THRESHOLD)),
                rule("Module not validated", 100, AcademicRuleOutcome.MODULE_NON_VALIDATED,
                    compare(AcademicMetric.MODULE_FINAL_GRADE,
                        ComparisonOperator.LESS_THAN,
                        ProfileVariable.MODULE_VALIDATION_THRESHOLD))
            ),
            semesterRules,
            List.of(
                rule("Both semesters validated", 10,
                    AcademicRuleOutcome.ACADEMIC_LEVEL_VALIDATED,
                    literal(AcademicMetric.NON_VALIDATED_SEMESTER_COUNT,
                        ComparisonOperator.EQUAL, BigDecimal.ZERO)),
                rule("Inter-semester compensation", 20,
                    AcademicRuleOutcome.ACADEMIC_LEVEL_VALIDATED_BY_COMPENSATION,
                    and(
                        compare(AcademicMetric.ANNUAL_AVERAGE,
                            ComparisonOperator.GREATER_THAN_OR_EQUAL,
                            ProfileVariable.ANNUAL_VALIDATION_AVERAGE),
                        compare(AcademicMetric.INDIVIDUALLY_VALIDATED_MODULE_COUNT,
                            ComparisonOperator.GREATER_THAN_OR_EQUAL,
                            ProfileVariable.MINIMUM_INDIVIDUALLY_VALIDATED_MODULES_PER_ACADEMIC_LEVEL),
                        compare(AcademicMetric.MINIMUM_NON_VALIDATED_MODULE_GRADE,
                            ComparisonOperator.GREATER_THAN_OR_EQUAL,
                            ProfileVariable.COMPENSATION_MINIMUM_THRESHOLD)
                    )),
                rule("Academic level not validated", 100,
                    AcademicRuleOutcome.ACADEMIC_LEVEL_NON_VALIDATED,
                    literal(AcademicMetric.NON_VALIDATED_SEMESTER_COUNT,
                        ComparisonOperator.GREATER_THAN, BigDecimal.ZERO))
            ),
            List.of(
                rule("Module inscription limit reached", 10, AcademicRuleOutcome.FAILED,
                    literal(AcademicMetric.EXHAUSTED_MODULE_INSCRIPTION_COUNT,
                        ComparisonOperator.GREATER_THAN, BigDecimal.ZERO)),
                rule("Academic level validated", 20, AcademicRuleOutcome.PROMOTED,
                    literal(AcademicMetric.ACADEMIC_LEVEL_VALIDATED,
                        ComparisonOperator.EQUAL, BigDecimal.ONE)),
                rule("Progression with debt", 40,
                    AcademicRuleOutcome.PROMOTED_WITH_DEBT,
                    and(
                        compare(AcademicMetric.OUTSTANDING_MODULE_COUNT,
                            ComparisonOperator.LESS_THAN_OR_EQUAL,
                            ProfileVariable.MAXIMUM_CARRIED_MODULES),
                        literal(AcademicMetric.EXHAUSTED_MODULE_INSCRIPTION_COUNT,
                            ComparisonOperator.EQUAL, BigDecimal.ZERO)
                    )),
                rule("Repeat level", 100, AcademicRuleOutcome.REPEAT,
                    literal(AcademicMetric.NON_VALIDATED_SEMESTER_COUNT,
                        ComparisonOperator.GREATER_THAN, BigDecimal.ZERO))
            ),
            true,
            semesterRules,
            semesterRules
        );
    }

    private static AcademicDecisionRule rule(
        String name,
        int priority,
        AcademicRuleOutcome outcome,
        RuleExpression expression
    ) {
        return new AcademicDecisionRule(name, priority, outcome, true, expression);
    }

    private static ComparisonExpression compare(
        AcademicMetric left,
        ComparisonOperator operator,
        ProfileVariable right
    ) {
        return new ComparisonExpression(left, operator, right, null);
    }

    private static ComparisonExpression literal(
        AcademicMetric left,
        ComparisonOperator operator,
        BigDecimal value
    ) {
        return new ComparisonExpression(left, operator, null, value);
    }

    private static LogicalExpression and(RuleExpression... expressions) {
        return new LogicalExpression(LogicalOperator.AND, List.of(expressions));
    }
}
