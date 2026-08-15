package com.platform.universitygovernance.academicruleprofile.application;

import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfile;
import com.platform.universitygovernance.academicruleprofile.domain.rules.ProfileVariable;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class AcademicProfileVariableResolver {

    public BigDecimal resolve(ProfileVariable variable, AcademicRuleProfile profile) {
        return switch (variable) {
            case MODULE_VALIDATION_THRESHOLD -> profile.getModuleValidationThreshold();
            case COMPENSATION_MINIMUM_THRESHOLD -> profile.getCompensationMinimumThreshold();
            case SEMESTER_VALIDATION_AVERAGE -> profile.getSemesterValidationAverage();
            case ANNUAL_VALIDATION_AVERAGE -> profile.getAnnualValidationAverage();
            case MINIMUM_INDIVIDUALLY_VALIDATED_MODULES_PER_SEMESTER ->
                BigDecimal.valueOf(profile.getMinimumIndividuallyValidatedModulesPerSemester());
            case MAXIMUM_NON_VALIDATED_MODULES_PER_SEMESTER ->
                BigDecimal.valueOf(profile.getMaximumNonValidatedModulesPerSemester());
            case MINIMUM_INDIVIDUALLY_VALIDATED_MODULES_PER_ACADEMIC_LEVEL ->
                BigDecimal.valueOf(profile.getMinimumIndividuallyValidatedModulesPerAcademicLevel());
            case MAXIMUM_MODULE_INSCRIPTIONS ->
                BigDecimal.valueOf(profile.getMaximumModuleInscriptions());
            case MAXIMUM_CARRIED_MODULES ->
                BigDecimal.valueOf(profile.getMaximumCarriedModules());
        };
    }
}
