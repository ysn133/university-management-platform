package com.platform.attendance.absencerecord.domain;

import com.platform.documents.domain.UploadedDocument;
import com.platform.identityaccess.domain.Professor;
import com.platform.identityaccess.domain.Student;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "absence_justification")
public class AbsenceJustification {
    @Id private UUID id;
    @ManyToOne(optional = false) @JoinColumn(name = "absence_record_id", nullable = false) private AbsenceRecord absenceRecord;
    @ManyToOne(optional = false) @JoinColumn(name = "submitted_by_student_id", nullable = false) private Student submittedBy;
    @OneToOne(optional = false) @JoinColumn(name = "document_id", nullable = false, unique = true) private UploadedDocument document;
    @Column(name = "reason", nullable = false, length = 1500) private String reason;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 30) private AbsenceJustificationStatus status;
    @ManyToOne @JoinColumn(name = "reviewed_by_professor_id") private Professor reviewedBy;
    @Column(name = "decision_note", length = 1000) private String decisionNote;
    @Column(name = "submitted_at", nullable = false) private Instant submittedAt;
    @Column(name = "reviewed_at") private Instant reviewedAt;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @PrePersist void onCreate() { Instant now = Instant.now(); if (id == null) id = UUID.randomUUID(); if (submittedAt == null) submittedAt = now; createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public AbsenceRecord getAbsenceRecord() { return absenceRecord; }
    public void setAbsenceRecord(AbsenceRecord value) { absenceRecord = value; }
    public Student getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(Student value) { submittedBy = value; }
    public UploadedDocument getDocument() { return document; }
    public void setDocument(UploadedDocument value) { document = value; }
    public String getReason() { return reason; }
    public void setReason(String value) { reason = value; }
    public AbsenceJustificationStatus getStatus() { return status; }
    public void setStatus(AbsenceJustificationStatus value) { status = value; }
    public Professor getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Professor value) { reviewedBy = value; }
    public String getDecisionNote() { return decisionNote; }
    public void setDecisionNote(String value) { decisionNote = value; }
    public Instant getSubmittedAt() { return submittedAt; }
    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant value) { reviewedAt = value; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
