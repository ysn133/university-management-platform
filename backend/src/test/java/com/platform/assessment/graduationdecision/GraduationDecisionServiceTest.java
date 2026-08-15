package com.platform.assessment.graduationdecision;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.platform.academicregistration.registration.domain.AcademicRegistration;
import com.platform.academicregistration.registration.domain.AcademicRegistrationStatus;
import com.platform.academicregistration.registration.infrastructure.AcademicRegistrationRepository;
import com.platform.assessment.graduationdecision.application.GraduationDecisionService;
import com.platform.assessment.graduationdecision.domain.GraduationDecision;
import com.platform.assessment.graduationdecision.infrastructure.GraduationDecisionRepository;
import com.platform.assessment.progressiondecision.domain.ProgressionDecision;
import com.platform.assessment.progressiondecision.domain.ProgressionDecisionStatus;
import com.platform.assessment.progressiondecision.infrastructure.ProgressionDecisionRepository;
import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.academiclevel.domain.AcademicLevel;
import com.platform.universitygovernance.academiclevel.infrastructure.AcademicLevelRepository;
import com.platform.universitygovernance.department.domain.Department;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.programfiliere.domain.ProgramFiliere;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

class GraduationDecisionServiceTest {

    private final AcademicRegistrationRepository registrationRepository = mock(AcademicRegistrationRepository.class);
    private final AcademicLevelRepository academicLevelRepository = mock(AcademicLevelRepository.class);
    private final ProgressionDecisionRepository progressionDecisionRepository = mock(ProgressionDecisionRepository.class);
    private final GraduationDecisionRepository graduationDecisionRepository = mock(GraduationDecisionRepository.class);
    private final UserProfileRepository profileRepository = mock(UserProfileRepository.class);
    private final AdminPermissionAuthorizationService authorizationService = mock(AdminPermissionAuthorizationService.class);
    private final AuthenticatedUserPrincipal principal = mock(AuthenticatedUserPrincipal.class);
    private GraduationDecisionService service;

    @BeforeEach
    void setUp() {
        service = new GraduationDecisionService(
            registrationRepository,
            academicLevelRepository,
            progressionDecisionRepository,
            graduationDecisionRepository,
            profileRepository,
            authorizationService
        );
    }

    @Test
    void generatesGraduationFromEveryCompletedProgramLevel() {
        UUID levelId = UUID.randomUUID();
        UUID yearId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID firstRegistrationId = UUID.randomUUID();
        UUID terminalRegistrationId = UUID.randomUUID();

        AcademicLevel firstLevel = mock(AcademicLevel.class);
        AcademicLevel terminalLevel = level(levelId, programId, true);
        when(firstLevel.getId()).thenReturn(UUID.randomUUID());
        AcademicRegistration firstRegistration = registration(
            firstRegistrationId, studentId, programId, firstLevel
        );
        AcademicRegistration terminalRegistration = registration(
            terminalRegistrationId, studentId, programId, terminalLevel
        );
        ProgressionDecision firstDecision = decision(
            ProgressionDecisionStatus.PROMOTED,
            "12.00"
        );
        ProgressionDecision terminalDecision = decision(
            ProgressionDecisionStatus.LEVEL_VALIDATED,
            "14.00"
        );

        when(academicLevelRepository.findById(levelId)).thenReturn(Optional.of(terminalLevel));
        when(registrationRepository.findByAcademicLevelIdAndAcademicYearIdAndStatusOrderByStudentApogeeCodeAsc(
            levelId, yearId, AcademicRegistrationStatus.ACTIVE
        )).thenReturn(List.of(terminalRegistration));
        when(academicLevelRepository.findByProgramFiliereIdOrderByLevelOrderAsc(programId))
            .thenReturn(List.of(firstLevel, terminalLevel));
        when(registrationRepository.findByStudentIdAndProgramFiliereIdOrderByAcademicYearStartYearDesc(
            studentId, programId
        )).thenReturn(List.of(terminalRegistration, firstRegistration));
        when(progressionDecisionRepository.findByAcademicRegistrationId(firstRegistrationId))
            .thenReturn(Optional.of(firstDecision));
        when(progressionDecisionRepository.findByAcademicRegistrationId(terminalRegistrationId))
            .thenReturn(Optional.of(terminalDecision));
        when(graduationDecisionRepository.findByTerminalAcademicRegistrationId(terminalRegistrationId))
            .thenReturn(Optional.empty());
        when(graduationDecisionRepository.findByTerminalAcademicRegistrationIdIn(any()))
            .thenReturn(List.of());

        service.generate(principal, levelId, yearId);

        ArgumentCaptor<GraduationDecision> captor = ArgumentCaptor.forClass(GraduationDecision.class);
        verify(graduationDecisionRepository).save(captor.capture());
        assertThat(captor.getValue().getGraduationAverage()).isEqualByComparingTo("13.00");
        assertThat(captor.getValue().getDecisionStatus().name()).isEqualTo("GRADUATED");
    }

    @Test
    void rejectsGraduationForNonTerminalLevel() {
        UUID levelId = UUID.randomUUID();
        AcademicLevel level = level(levelId, UUID.randomUUID(), false);
        when(academicLevelRepository.findById(levelId)).thenReturn(Optional.of(level));

        assertThatThrownBy(() -> service.generate(principal, levelId, UUID.randomUUID()))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("terminal academic level");
    }

    private AcademicLevel level(UUID levelId, UUID programId, boolean terminal) {
        Establishment establishment = mock(Establishment.class);
        Department department = mock(Department.class);
        ProgramFiliere program = mock(ProgramFiliere.class);
        AcademicLevel level = mock(AcademicLevel.class);
        when(establishment.getId()).thenReturn(UUID.randomUUID());
        when(department.getEstablishment()).thenReturn(establishment);
        when(program.getId()).thenReturn(programId);
        when(program.getDepartment()).thenReturn(department);
        when(level.getId()).thenReturn(levelId);
        when(level.getProgramFiliere()).thenReturn(program);
        when(level.isTerminalLevel()).thenReturn(terminal);
        return level;
    }

    private AcademicRegistration registration(
        UUID registrationId,
        UUID studentId,
        UUID programId,
        AcademicLevel level
    ) {
        var student = mock(com.platform.identityaccess.domain.Student.class);
        ProgramFiliere program = mock(ProgramFiliere.class);
        AcademicRegistration registration = mock(AcademicRegistration.class);
        when(student.getId()).thenReturn(studentId);
        when(program.getId()).thenReturn(programId);
        when(registration.getId()).thenReturn(registrationId);
        when(registration.getStudent()).thenReturn(student);
        when(registration.getProgramFiliere()).thenReturn(program);
        when(registration.getAcademicLevel()).thenReturn(level);
        return registration;
    }

    private ProgressionDecision decision(ProgressionDecisionStatus status, String average) {
        ProgressionDecision decision = mock(ProgressionDecision.class);
        when(decision.getDecisionStatus()).thenReturn(status);
        when(decision.getAnnualAverage()).thenReturn(new BigDecimal(average));
        return decision;
    }
}
