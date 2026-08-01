package com.platform.assessment.semesterresult.domain;

import com.platform.academicregistration.semesterregistration.domain.SemesterRegistration;
import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "semester_result")
public class SemesterResult {

    @Id
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "semester_registration_id", nullable = false, unique = true)
    private SemesterRegistration semesterRegistration;

    @ManyToOne(optional = false)
    @JoinColumn(name = "academic_rule_profile_id", nullable = false)
    private AcademicRuleProfile academicRuleProfile;

    @Column(name = "semester_average", nullable = false, precision = 5, scale = 2)
    private BigDecimal semesterAverage;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_status", nullable = false)
    private SemesterResultStatus resultStatus;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public SemesterResult() {
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

    public SemesterRegistration getSemesterRegistration() {
        return semesterRegistration;
    }

    public void setSemesterRegistration(SemesterRegistration semesterRegistration) {
        this.semesterRegistration = semesterRegistration;
    }

    public AcademicRuleProfile getAcademicRuleProfile() {
        return academicRuleProfile;
    }

    public void setAcademicRuleProfile(AcademicRuleProfile academicRuleProfile) {
        this.academicRuleProfile = academicRuleProfile;
    }

    public BigDecimal getSemesterAverage() {
        return semesterAverage;
    }

    public void setSemesterAverage(BigDecimal semesterAverage) {
        this.semesterAverage = semesterAverage;
    }

    public SemesterResultStatus getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(SemesterResultStatus resultStatus) {
        this.resultStatus = resultStatus;
    }

    public Instant getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(Instant evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
