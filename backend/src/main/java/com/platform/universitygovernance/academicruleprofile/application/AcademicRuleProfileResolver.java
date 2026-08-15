package com.platform.universitygovernance.academicruleprofile.application;

import com.platform.universitygovernance.academiclevelruleassignment.infrastructure.AcademicLevelRuleAssignmentRepository;
import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfile;
import com.platform.universitygovernance.semester.domain.Semester;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AcademicRuleProfileResolver {

    private final AcademicLevelRuleAssignmentRepository levelAssignmentRepository;

    public AcademicRuleProfileResolver(AcademicLevelRuleAssignmentRepository levelAssignmentRepository) {
        this.levelAssignmentRepository = levelAssignmentRepository;
    }

    public AcademicRuleProfile resolveForSemester(Semester semester) {
        return resolveForAcademicLevel(
            semester.getAcademicLevel().getId(),
            semester.getAcademicYear().getId()
        );
    }

    public AcademicRuleProfile resolveForAcademicLevel(UUID academicLevelId, UUID academicYearId) {
        return levelAssignmentRepository
            .findByAcademicLevelIdAndAcademicYearId(academicLevelId, academicYearId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No academic rule profile is assigned to this level and academic year"
            ))
            .getAcademicRuleProfile();
    }
}
