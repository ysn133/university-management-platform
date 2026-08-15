package com.platform.universitygovernance.academicruleprofile.presentation.dto;

import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfileStatus;
import com.platform.universitygovernance.academicruleprofile.domain.AbsenceExclusionPolicy;
import com.platform.universitygovernance.academicruleprofile.domain.SessionGradePolicy;
import com.platform.universitygovernance.academicruleprofile.domain.rules.AcademicRuleSet;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AcademicRuleProfileResponse(
    UUID id,
    UUID establishmentId,
    String name,
    int version,
    BigDecimal moduleValidationThreshold,
    BigDecimal compensationMinimumThreshold,
    BigDecimal semesterValidationAverage,
    BigDecimal annualValidationAverage,
    int minimumIndividuallyValidatedModulesPerSemester,
    int maximumNonValidatedModulesPerSemester,
    boolean allowInterSemesterCompensation,
    int minimumIndividuallyValidatedModulesPerAcademicLevel,
    int maximumModuleInscriptions,
    SessionGradePolicy sessionGradePolicy,
    boolean allowProgressionWithDebt,
    int maximumCarriedModules,
    int maximumUnjustifiedAbsences,
    AbsenceExclusionPolicy absenceExclusionPolicy,
    AcademicRuleProfileStatus status,
    Instant createdAt,
    Instant updatedAt,
    AcademicRuleSet ruleDefinition
) {
}
