package com.platform.universitygovernance.academicruleprofile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.platform.universitygovernance.academiclevel.domain.AcademicLevel;
import com.platform.universitygovernance.academiclevelruleassignment.domain.AcademicLevelRuleAssignment;
import com.platform.universitygovernance.academiclevelruleassignment.infrastructure.AcademicLevelRuleAssignmentRepository;
import com.platform.universitygovernance.academicyear.domain.AcademicYear;
import com.platform.universitygovernance.academicruleprofile.application.AcademicRuleProfileResolver;
import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfile;
import com.platform.universitygovernance.semester.domain.Semester;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AcademicRuleProfileResolverTest {

    private final AcademicLevelRuleAssignmentRepository levelRepository = mock(
        AcademicLevelRuleAssignmentRepository.class
    );
    private final AcademicRuleProfileResolver resolver = new AcademicRuleProfileResolver(levelRepository);

    @Test
    void semesterFallsBackToAcademicLevelAssignment() {
        Semester semester = mock(Semester.class);
        AcademicLevel level = mock(AcademicLevel.class);
        AcademicYear year = mock(AcademicYear.class);
        AcademicLevelRuleAssignment assignment = mock(AcademicLevelRuleAssignment.class);
        AcademicRuleProfile profile = mock(AcademicRuleProfile.class);
        UUID levelId = UUID.randomUUID();
        UUID yearId = UUID.randomUUID();
        when(semester.getAcademicLevel()).thenReturn(level);
        when(semester.getAcademicYear()).thenReturn(year);
        when(level.getId()).thenReturn(levelId);
        when(year.getId()).thenReturn(yearId);
        when(levelRepository.findByAcademicLevelIdAndAcademicYearId(levelId, yearId))
            .thenReturn(Optional.of(assignment));
        when(assignment.getAcademicRuleProfile()).thenReturn(profile);

        assertThat(resolver.resolveForSemester(semester)).isSameAs(profile);
    }
}
