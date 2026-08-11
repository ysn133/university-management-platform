package com.platform.scheduling.examgroup.domain;

import com.platform.academicregistration.semesterregistration.domain.SemesterRegistration;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "exam_group_membership")
public class ExamGroupMembership {
    @Id private UUID id;
    @ManyToOne(optional = false) @JoinColumn(name = "exam_group_id", nullable = false) private ExamGroup examGroup;
    @ManyToOne(optional = false) @JoinColumn(name = "semester_registration_id", nullable = false) private SemesterRegistration semesterRegistration;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    public ExamGroupMembership() {}
    @PrePersist void onCreate() { if (id == null) id = UUID.randomUUID(); createdAt = Instant.now(); }
    public UUID getId() { return id; }
    public ExamGroup getExamGroup() { return examGroup; }
    public void setExamGroup(ExamGroup examGroup) { this.examGroup = examGroup; }
    public SemesterRegistration getSemesterRegistration() { return semesterRegistration; }
    public void setSemesterRegistration(SemesterRegistration semesterRegistration) { this.semesterRegistration = semesterRegistration; }
}
