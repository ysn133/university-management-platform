package com.platform.universitygovernance.academicruleprofile.domain.rules;

import java.util.List;
import com.platform.universitygovernance.semester.domain.SemesterTermType;

public record AcademicRuleSet(
    List<AcademicDecisionRule> moduleRules,
    List<AcademicDecisionRule> semesterRules,
    List<AcademicDecisionRule> academicLevelRules,
    List<AcademicDecisionRule> progressionRules,
    Boolean useSharedSemesterRules,
    List<AcademicDecisionRule> autumnSemesterRules,
    List<AcademicDecisionRule> springSemesterRules
) {
    public AcademicRuleSet {
        useSharedSemesterRules = useSharedSemesterRules == null ? true : useSharedSemesterRules;
        autumnSemesterRules = autumnSemesterRules == null ? semesterRules : autumnSemesterRules;
        springSemesterRules = springSemesterRules == null ? semesterRules : springSemesterRules;
    }

    public boolean hasSharedSemesterRules() {
        return !Boolean.FALSE.equals(useSharedSemesterRules);
    }

    public List<AcademicDecisionRule> semesterRulesFor(SemesterTermType termType) {
        if (hasSharedSemesterRules()) {
            return semesterRules;
        }
        return termType == SemesterTermType.AUTUMN
            ? autumnSemesterRules
            : springSemesterRules;
    }
}
