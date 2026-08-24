package com.platform.attendance.absencerecord;

import static org.mockito.Mockito.*;

import com.platform.attendance.absencerecord.application.AbsenceJustificationService;
import com.platform.attendance.absencerecord.domain.*;
import com.platform.attendance.absencerecord.infrastructure.*;
import com.platform.attendance.absencerecord.presentation.dto.ReviewAbsenceJustificationRequest;
import com.platform.documents.application.DocumentService;
import com.platform.documents.domain.UploadedDocument;
import com.platform.identityaccess.domain.*;
import com.platform.identityaccess.infrastructure.*;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.teachingassignment.domain.*;
import com.platform.teachingassignment.infrastructure.TeachingAssignmentRepository;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AbsenceJustificationServiceTest {
    @Mock AbsenceJustificationRepository repository;
    @Mock AbsenceRecordRepository absenceRepository;
    @Mock ProfessorRepository professorRepository;
    @Mock UserProfileRepository profileRepository;
    @Mock DocumentService documentService;
    @Mock TeachingAssignmentRepository teachingAssignmentRepository;

    @Test
    void responsibleProfessorAcceptanceMarksAbsenceJustified() {
        UUID professorId = UUID.randomUUID(); UUID assignmentId = UUID.randomUUID(); UUID justificationId = UUID.randomUUID();
        AbsenceJustification justification = mock(AbsenceJustification.class);
        AbsenceRecord absence = mock(AbsenceRecord.class, Answers.RETURNS_DEEP_STUBS);
        TeachingAssignment assignment = mock(TeachingAssignment.class);
        Professor professor = mock(Professor.class);
        Student student = mock(Student.class, Answers.RETURNS_DEEP_STUBS);
        UserProfile profile = mock(UserProfile.class);
        UploadedDocument document = mock(UploadedDocument.class);
        when(repository.findById(justificationId)).thenReturn(Optional.of(justification));
        when(justification.getAbsenceRecord()).thenReturn(absence);
        when(justification.getStatus()).thenReturn(AbsenceJustificationStatus.PENDING);
        when(justification.getSubmittedBy()).thenReturn(student);
        when(justification.getDocument()).thenReturn(document);
        when(absence.getTeachingAssignment().getId()).thenReturn(assignmentId);
        when(teachingAssignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(assignment.getProfessor()).thenReturn(professor);
        when(professor.getId()).thenReturn(professorId);
        when(assignment.getStatus()).thenReturn(TeachingAssignmentStatus.ACTIVE);
        when(professorRepository.findById(professorId)).thenReturn(Optional.of(professor));
        when(profileRepository.findByUserAccountId(any())).thenReturn(Optional.of(profile));
        when(repository.save(justification)).thenReturn(justification);

        var service = new AbsenceJustificationService(repository, absenceRepository, professorRepository, profileRepository, documentService, teachingAssignmentRepository);
        service.review(new AuthenticatedUserPrincipal(UUID.randomUUID(), AccountRoleType.PROFESSOR, professorId, UUID.randomUUID(), null), justificationId,
            new ReviewAbsenceJustificationRequest(AbsenceJustificationStatus.ACCEPTED, "Valid certificate"));

        verify(absence).setJustified(true);
        verify(absenceRepository).save(absence);
        verify(justification).setReviewedBy(professor);
        verify(justification).setStatus(AbsenceJustificationStatus.ACCEPTED);
    }
}
