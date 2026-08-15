package com.platform.universitygovernance.academicruleprofile;

import static org.assertj.core.api.Assertions.assertThat;

import com.platform.universitygovernance.academicruleprofile.domain.rules.AcademicDecisionRule;
import com.platform.universitygovernance.academicruleprofile.domain.rules.AcademicRuleSet;
import com.platform.universitygovernance.academicruleprofile.domain.rules.DefaultAcademicRuleSets;
import com.platform.universitygovernance.semester.domain.SemesterTermType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class AcademicRuleSetTest {

    @Test
    void sharedConfigurationUsesTheSameRulesForBothTerms() {
        AcademicRuleSet rules = DefaultAcademicRuleSets.standard();

        assertThat(rules.semesterRulesFor(SemesterTermType.AUTUMN))
            .isSameAs(rules.semesterRules());
        assertThat(rules.semesterRulesFor(SemesterTermType.SPRING))
            .isSameAs(rules.semesterRules());
    }

    @Test
    void separateConfigurationSelectsRulesByTerm() {
        AcademicRuleSet base = DefaultAcademicRuleSets.standard();
        List<AcademicDecisionRule> autumnRules = base.semesterRules();
        List<AcademicDecisionRule> springRules = new ArrayList<>(base.semesterRules());
        Collections.reverse(springRules);
        AcademicRuleSet rules = new AcademicRuleSet(
            base.moduleRules(),
            base.semesterRules(),
            base.academicLevelRules(),
            base.progressionRules(),
            false,
            autumnRules,
            springRules
        );

        assertThat(rules.semesterRulesFor(SemesterTermType.AUTUMN)).isSameAs(autumnRules);
        assertThat(rules.semesterRulesFor(SemesterTermType.SPRING)).isSameAs(springRules);
    }
}
