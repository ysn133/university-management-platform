package com.platform.assessment.progressiondecision.domain;

import com.platform.academicregistration.registration.domain.AcademicRegistration;
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
@Table(name = "progression_decision")
public class ProgressionDecision {

    @Id
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "academic_registration_id", nullable = false, unique = true)
    private AcademicRegistration academicRegistration;

    @ManyToOne(optional = false)
    @JoinColumn(name = "academic_rule_profile_id", nullable = false)
    private AcademicRuleProfile academicRuleProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_status", nullable = false)
    private ProgressionDecisionStatus decisionStatus;

    @Column(name = "annual_average", nullable = false, precision = 5, scale = 2)
    private BigDecimal annualAverage;

    @Column(name = "outstanding_module_count", nullable = false)
    private int outstandingModuleCount;

    @Column(name = "decided_at", nullable = false)
    private Instant decidedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ProgressionDecision() {
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

    public AcademicRegistration getAcademicRegistration() {
        return academicRegistration;
    }

    public void setAcademicRegistration(AcademicRegistration academicRegistration) {
        this.academicRegistration = academicRegistration;
    }

    public AcademicRuleProfile getAcademicRuleProfile() {
        return academicRuleProfile;
    }

    public void setAcademicRuleProfile(AcademicRuleProfile academicRuleProfile) {
        this.academicRuleProfile = academicRuleProfile;
    }

    public ProgressionDecisionStatus getDecisionStatus() {
        return decisionStatus;
    }

    public void setDecisionStatus(ProgressionDecisionStatus decisionStatus) {
        this.decisionStatus = decisionStatus;
    }

    public BigDecimal getAnnualAverage() {
        return annualAverage;
    }

    public void setAnnualAverage(BigDecimal annualAverage) {
        this.annualAverage = annualAverage;
    }

    public int getOutstandingModuleCount() {
        return outstandingModuleCount;
    }

    public void setOutstandingModuleCount(int outstandingModuleCount) {
        this.outstandingModuleCount = outstandingModuleCount;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(Instant decidedAt) {
        this.decidedAt = decidedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
