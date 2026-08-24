package com.platform.attendance.absencerecord.application;

import com.platform.attendance.absencerecord.domain.*;
import com.platform.attendance.absencerecord.infrastructure.*;
import com.platform.attendance.absencerecord.presentation.dto.*;
import com.platform.documents.application.DocumentService;
import com.platform.documents.domain.DocumentPurpose;
import com.platform.identityaccess.domain.*;
import com.platform.identityaccess.infrastructure.*;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.teachingassignment.domain.TeachingAssignmentStatus;
import com.platform.teachingassignment.infrastructure.TeachingAssignmentRepository;
import java.time.Instant;
import java.util.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AbsenceJustificationService {
    private final AbsenceJustificationRepository repository;
    private final AbsenceRecordRepository absenceRepository;
    private final ProfessorRepository professorRepository;
    private final UserProfileRepository profileRepository;
    private final DocumentService documentService;
    private final TeachingAssignmentRepository teachingAssignmentRepository;

    public AbsenceJustificationService(AbsenceJustificationRepository repository, AbsenceRecordRepository absenceRepository,
        ProfessorRepository professorRepository, UserProfileRepository profileRepository,
        DocumentService documentService, TeachingAssignmentRepository teachingAssignmentRepository) {
        this.repository = repository; this.absenceRepository = absenceRepository;
        this.professorRepository = professorRepository; this.profileRepository = profileRepository; this.documentService = documentService;
        this.teachingAssignmentRepository = teachingAssignmentRepository;
    }

    @Transactional
    public AbsenceJustificationResponse submit(AuthenticatedUserPrincipal principal, UUID absenceId, SubmitAbsenceJustificationRequest request) {
        requireRole(principal, AccountRoleType.STUDENT);
        AbsenceRecord absence = findAbsence(absenceId);
        Student student = absence.getModuleRegistration().getSemesterRegistration().getAcademicRegistration().getStudent();
        if (!student.getId().equals(principal.roleEntityId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This absence does not belong to the authenticated student");
        if (absence.isJustified()) throw new ResponseStatusException(HttpStatus.CONFLICT, "This absence is already justified");
        if (repository.existsByAbsenceRecordIdAndStatus(absenceId, AbsenceJustificationStatus.PENDING)) throw new ResponseStatusException(HttpStatus.CONFLICT, "A justification is already pending");
        var document = documentService.requireOwnedTemporary(request.documentId(), principal, DocumentPurpose.ABSENCE_JUSTIFICATION);
        AbsenceJustification justification = new AbsenceJustification();
        justification.setAbsenceRecord(absence); justification.setSubmittedBy(student); justification.setDocument(document);
        justification.setReason(request.reason().trim()); justification.setStatus(AbsenceJustificationStatus.PENDING);
        AbsenceJustification saved = repository.save(justification);
        documentService.markAttached(document);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AbsenceJustificationResponse> getMine(AuthenticatedUserPrincipal principal) {
        requireRole(principal, AccountRoleType.STUDENT);
        return repository.findBySubmittedByIdOrderBySubmittedAtDesc(principal.roleEntityId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AbsenceJustificationResponse> getForAssignment(AuthenticatedUserPrincipal principal, UUID assignmentId) {
        requireAssignedProfessor(principal, assignmentId);
        return repository.findByAbsenceRecordTeachingAssignmentIdOrderBySubmittedAtDesc(assignmentId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public AbsenceJustificationResponse review(AuthenticatedUserPrincipal principal, UUID justificationId, ReviewAbsenceJustificationRequest request) {
        AbsenceJustification justification = find(justificationId);
        requireAssignedProfessor(principal, justification.getAbsenceRecord().getTeachingAssignment().getId());
        if (justification.getStatus() != AbsenceJustificationStatus.PENDING) throw new ResponseStatusException(HttpStatus.CONFLICT, "This justification has already been reviewed");
        if (request.decision() == AbsenceJustificationStatus.PENDING) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decision must be accepted or rejected");
        Professor professor = professorRepository.findById(principal.roleEntityId()).orElseThrow();
        justification.setStatus(request.decision()); justification.setReviewedBy(professor);
        justification.setDecisionNote(normalize(request.note())); justification.setReviewedAt(Instant.now());
        AbsenceRecord absence = justification.getAbsenceRecord();
        boolean accepted = request.decision() == AbsenceJustificationStatus.ACCEPTED;
        absence.setJustified(accepted); absence.setJustificationNote(accepted ? justification.getReason() : null);
        absenceRepository.save(absence);
        return toResponse(repository.save(justification));
    }

    @Transactional(readOnly = true)
    public DocumentDownload download(AuthenticatedUserPrincipal principal, UUID justificationId) {
        AbsenceJustification justification = find(justificationId);
        boolean studentOwner = principal != null && principal.role() == AccountRoleType.STUDENT && justification.getSubmittedBy().getId().equals(principal.roleEntityId());
        if (!studentOwner) requireAssignedProfessor(principal, justification.getAbsenceRecord().getTeachingAssignment().getId());
        var document = justification.getDocument();
        return new DocumentDownload(documentService.load(document), document.getContentType(), document.getOriginalFilename());
    }

    private void requireAssignedProfessor(AuthenticatedUserPrincipal principal, UUID assignmentId) {
        requireRole(principal, AccountRoleType.PROFESSOR);
        var assignment = teachingAssignmentRepository.findById(assignmentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teaching assignment not found"));
        if (!assignment.getProfessor().getId().equals(principal.roleEntityId()) || assignment.getStatus() != TeachingAssignmentStatus.ACTIVE)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Assigned professor access required");
    }

    private void requireRole(AuthenticatedUserPrincipal principal, AccountRoleType role) { if (principal == null || principal.role() != role) throw new ResponseStatusException(HttpStatus.FORBIDDEN, role + " access required"); }
    private AbsenceRecord findAbsence(UUID id) { return absenceRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Absence not found")); }
    private AbsenceJustification find(UUID id) { if (id == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Justification not found"); return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Justification not found")); }
    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private AbsenceJustificationResponse toResponse(AbsenceJustification j) {
        Student student = j.getSubmittedBy(); var profile = profileRepository.findByUserAccountId(student.getUserAccount().getId()).orElseThrow();
        var absence = j.getAbsenceRecord(); var module = absence.getModuleRegistration().getSubjectModule(); var document = j.getDocument();
        return new AbsenceJustificationResponse(j.getId(), absence.getId(), absence.getTeachingAssignment().getId(), student.getId(), student.getApogeeCode(), profile.getFirstName(), profile.getLastName(), module.getId(), module.getCode(), module.getTitle(), absence.getAbsenceDate(), j.getReason(), j.getStatus(), document.getId(), document.getOriginalFilename(), document.getContentType(), j.getDecisionNote(), j.getSubmittedAt(), j.getReviewedAt());
    }
    public record DocumentDownload(byte[] content, String contentType, String filename) {}
}
