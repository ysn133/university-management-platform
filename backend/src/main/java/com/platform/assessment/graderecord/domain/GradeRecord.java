package com.platform.assessment.graderecord.domain;

import com.platform.academicregistration.subjectmoduleregestration.domain.SubjectModuleRegestration;
import com.platform.scheduling.moduleexam.domain.ModuleExam;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "grade_record")
public class GradeRecord {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "module_registration_id", nullable = false)
    private SubjectModuleRegestration moduleRegistration;

    @ManyToOne(optional = false)
    @JoinColumn(name = "module_exam_id", nullable = false)
    private ModuleExam moduleExam;

    @Column(name = "grade_value", nullable = false, precision = 5, scale = 2)
    private BigDecimal gradeValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "zero_grade_reason")
    private ZeroGradeReason zeroGradeReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "workflow_status", nullable = false)
    private GradeWorkflowStatus workflowStatus;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public GradeRecord() {
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public SubjectModuleRegestration getModuleRegistration() {
        return moduleRegistration;
    }

    public void setModuleRegistration(
        SubjectModuleRegestration moduleRegistration
    ) {
        this.moduleRegistration = moduleRegistration;
    }

    public ModuleExam getModuleExam() {
        return moduleExam;
    }

    public void setModuleExam(ModuleExam moduleExam) {
        this.moduleExam = moduleExam;
    }

    public BigDecimal getGradeValue() {
        return gradeValue;
    }

    public void setGradeValue(BigDecimal gradeValue) {
        this.gradeValue = gradeValue;
    }

    public ZeroGradeReason getZeroGradeReason() {
        return zeroGradeReason;
    }

    public void setZeroGradeReason(ZeroGradeReason zeroGradeReason) {
        this.zeroGradeReason = zeroGradeReason;
    }

    public GradeWorkflowStatus getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(GradeWorkflowStatus workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
