package com.platform.universitygovernance.academicruleprofile;

import static org.assertj.core.api.Assertions.assertThat;

import com.platform.universitygovernance.academicruleprofile.application.AcademicProfileVariableResolver;
import com.platform.universitygovernance.academicruleprofile.application.AcademicRuleEvaluator;
import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfile;
import com.platform.universitygovernance.academicruleprofile.domain.rules.AcademicDecisionRule;
import com.platform.universitygovernance.academicruleprofile.domain.rules.AcademicMetric;
import com.platform.universitygovernance.academicruleprofile.domain.rules.AcademicRuleOutcome;
import com.platform.universitygovernance.academicruleprofile.domain.rules.ComparisonExpression;
import com.platform.universitygovernance.academicruleprofile.domain.rules.ComparisonOperator;
import com.platform.universitygovernance.academicruleprofile.domain.rules.LogicalExpression;
import com.platform.universitygovernance.academicruleprofile.domain.rules.LogicalOperator;
import com.platform.universitygovernance.academicruleprofile.domain.rules.ProfileVariable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AcademicRuleEvaluatorTest {

    private final AcademicRuleEvaluator evaluator = new AcademicRuleEvaluator(
        new AcademicProfileVariableResolver()
    );

    @Test
    void evaluatesNestedExpressionsAgainstProfileVariables() {
        AcademicRuleProfile profile = profile();
        LogicalExpression expression = new LogicalExpression(
            LogicalOperator.OR,
            List.of(
                new ComparisonExpression(
                    AcademicMetric.NON_VALIDATED_MODULE_COUNT,
                    ComparisonOperator.EQUAL,
                    null,
                    BigDecimal.ZERO
                ),
                new LogicalExpression(LogicalOperator.AND, List.of(
                    new ComparisonExpression(
                        AcademicMetric.SEMESTER_AVERAGE,
                        ComparisonOperator.GREATER_THAN_OR_EQUAL,
                        ProfileVariable.SEMESTER_VALIDATION_AVERAGE,
                        null
                    ),
                    new ComparisonExpression(
                        AcademicMetric.MINIMUM_NON_VALIDATED_MODULE_GRADE,
                        ComparisonOperator.GREATER_THAN_OR_EQUAL,
                        ProfileVariable.COMPENSATION_MINIMUM_THRESHOLD,
                        null
                    )
                ))
            )
        );
        AcademicDecisionRule rule = new AcademicDecisionRule(
            "Configurable semester validation",
            10,
            AcademicRuleOutcome.SEMESTER_VALIDATED_BY_COMPENSATION,
            true,
            expression
        );

        assertThat(evaluator.evaluate(
            List.of(rule),
            Map.of(
                AcademicMetric.NON_VALIDATED_MODULE_COUNT, BigDecimal.ONE,
                AcademicMetric.SEMESTER_AVERAGE, new BigDecimal("10.25"),
                AcademicMetric.MINIMUM_NON_VALIDATED_MODULE_GRADE,
                    new BigDecimal("7.50")
            ),
            profile
        )).contains(AcademicRuleOutcome.SEMESTER_VALIDATED_BY_COMPENSATION);
    }

    @Test
    void returnsTheFirstMatchingRuleByPriority() {
        AcademicDecisionRule later = alwaysMatching(
            "Later",
            20,
            AcademicRuleOutcome.REPEAT
        );
        AcademicDecisionRule earlier = alwaysMatching(
            "Earlier",
            10,
            AcademicRuleOutcome.PROMOTED
        );

        assertThat(evaluator.evaluate(
            List.of(later, earlier),
            Map.of(AcademicMetric.OUTSTANDING_MODULE_COUNT, BigDecimal.ZERO),
            profile()
        )).contains(AcademicRuleOutcome.PROMOTED);
    }

    private AcademicDecisionRule alwaysMatching(
        String name,
        int priority,
        AcademicRuleOutcome outcome
    ) {
        return new AcademicDecisionRule(
            name,
            priority,
            outcome,
            true,
            new ComparisonExpression(
                AcademicMetric.OUTSTANDING_MODULE_COUNT,
                ComparisonOperator.EQUAL,
                null,
                BigDecimal.ZERO
            )
        );
    }

    private AcademicRuleProfile profile() {
        AcademicRuleProfile profile = new AcademicRuleProfile();
        profile.setModuleValidationThreshold(new BigDecimal("10.00"));
        profile.setCompensationMinimumThreshold(new BigDecimal("7.00"));
        profile.setSemesterValidationAverage(new BigDecimal("10.00"));
        profile.setAnnualValidationAverage(new BigDecimal("10.00"));
        profile.setMinimumIndividuallyValidatedModulesPerSemester(5);
        profile.setMaximumNonValidatedModulesPerSemester(2);
        profile.setMinimumIndividuallyValidatedModulesPerAcademicLevel(10);
        profile.setMaximumModuleInscriptions(2);
        profile.setMaximumCarriedModules(2);
        return profile;
    }
}
