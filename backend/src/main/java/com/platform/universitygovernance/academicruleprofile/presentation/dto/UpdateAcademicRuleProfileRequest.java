package com.platform.universitygovernance.academicruleprofile.presentation.dto;

import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfileStatus;
import com.platform.universitygovernance.academicruleprofile.domain.AbsenceExclusionPolicy;
import com.platform.universitygovernance.academicruleprofile.domain.SessionGradePolicy;
import com.platform.universitygovernance.academicruleprofile.domain.rules.AcademicRuleSet;
import com.platform.universitygovernance.academicruleprofile.domain.rules.DefaultAcademicRuleSets;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateAcademicRuleProfileRequest(
    @NotBlank @Size(max = 255) String name,
    @NotNull @DecimalMin("0.00") @DecimalMax("20.00") @Digits(integer = 2, fraction = 2)
    BigDecimal moduleValidationThreshold,
    @NotNull @DecimalMin("0.00") @DecimalMax("20.00") @Digits(integer = 2, fraction = 2)
    BigDecimal compensationMinimumThreshold,
    @NotNull @DecimalMin("0.00") @DecimalMax("20.00") @Digits(integer = 2, fraction = 2)
    BigDecimal semesterValidationAverage,
    @DecimalMin("0.00") @DecimalMax("20.00") @Digits(integer = 2, fraction = 2)
    BigDecimal annualValidationAverage,
    @NotNull @PositiveOrZero Integer minimumIndividuallyValidatedModulesPerSemester,
    @NotNull @PositiveOrZero Integer maximumNonValidatedModulesPerSemester,
    @NotNull Boolean allowInterSemesterCompensation,
    @NotNull @PositiveOrZero Integer minimumIndividuallyValidatedModulesPerAcademicLevel,
    @NotNull @Positive Integer maximumModuleInscriptions,
    @NotNull SessionGradePolicy sessionGradePolicy,
    @NotNull Boolean allowProgressionWithDebt,
    @NotNull @PositiveOrZero Integer maximumCarriedModules,
    @NotNull @PositiveOrZero Integer maximumUnjustifiedAbsences,
    @NotNull AbsenceExclusionPolicy absenceExclusionPolicy,
    @NotNull AcademicRuleProfileStatus status,
    AcademicRuleSet ruleDefinition
) {
    public UpdateAcademicRuleProfileRequest(
        String name,
        BigDecimal moduleValidationThreshold,
        BigDecimal compensationMinimumThreshold,
        BigDecimal semesterValidationAverage,
        BigDecimal annualValidationAverage,
        Integer minimumIndividuallyValidatedModulesPerSemester,
        Integer maximumNonValidatedModulesPerSemester,
        Boolean allowInterSemesterCompensation,
        Integer minimumIndividuallyValidatedModulesPerAcademicLevel,
        Integer maximumModuleInscriptions,
        SessionGradePolicy sessionGradePolicy,
        Boolean allowProgressionWithDebt,
        Integer maximumCarriedModules,
        Integer maximumUnjustifiedAbsences,
        AbsenceExclusionPolicy absenceExclusionPolicy,
        AcademicRuleProfileStatus status
    ) {
        this(
            name,
            moduleValidationThreshold,
            compensationMinimumThreshold,
            semesterValidationAverage,
            annualValidationAverage,
            minimumIndividuallyValidatedModulesPerSemester,
            maximumNonValidatedModulesPerSemester,
            allowInterSemesterCompensation,
            minimumIndividuallyValidatedModulesPerAcademicLevel,
            maximumModuleInscriptions,
            sessionGradePolicy,
            allowProgressionWithDebt,
            maximumCarriedModules,
            maximumUnjustifiedAbsences,
            absenceExclusionPolicy,
            status,
            DefaultAcademicRuleSets.standard()
        );
    }

    public UpdateAcademicRuleProfileRequest(
        String name,
        BigDecimal moduleValidationThreshold,
        BigDecimal compensationMinimumThreshold,
        BigDecimal semesterValidationAverage,
        BigDecimal annualValidationAverage,
        Integer maximumModuleInscriptions,
        SessionGradePolicy sessionGradePolicy,
        Boolean allowProgressionWithDebt,
        Integer maximumCarriedModules,
        AcademicRuleProfileStatus status
    ) {
        this(
            name,
            moduleValidationThreshold,
            compensationMinimumThreshold,
            semesterValidationAverage,
            annualValidationAverage,
            0,
            999,
            false,
            0,
            maximumModuleInscriptions,
            sessionGradePolicy,
            allowProgressionWithDebt,
            maximumCarriedModules,
            0,
            AbsenceExclusionPolicy.NORMAL_AND_RATTRAPAGE,
            status,
            DefaultAcademicRuleSets.standard()
        );
    }
}
