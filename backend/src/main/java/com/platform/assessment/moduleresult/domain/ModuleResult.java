package com.platform.assessment.moduleresult.domain;

import com.platform.academicregistration.moduleregistration.domain.ModuleRegistration;
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
@Table(name = "module_result")
public class ModuleResult {

    @Id
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "module_registration_id", nullable = false, unique = true)
    private ModuleRegistration moduleRegistration;

    @ManyToOne(optional = false)
    @JoinColumn(name = "academic_rule_profile_id", nullable = false)
    private AcademicRuleProfile academicRuleProfile;

    @Column(name = "final_grade_value", nullable = false, precision = 5, scale = 2)
    private BigDecimal finalGradeValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_status", nullable = false)
    private ModuleResultStatus resultStatus;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ModuleResult() {
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

    public ModuleRegistration getModuleRegistration() {
        return moduleRegistration;
    }

    public void setModuleRegistration(ModuleRegistration moduleRegistration) {
        this.moduleRegistration = moduleRegistration;
    }

    public AcademicRuleProfile getAcademicRuleProfile() {
        return academicRuleProfile;
    }

    public void setAcademicRuleProfile(AcademicRuleProfile academicRuleProfile) {
        this.academicRuleProfile = academicRuleProfile;
    }

    public BigDecimal getFinalGradeValue() {
        return finalGradeValue;
    }

    public void setFinalGradeValue(BigDecimal finalGradeValue) {
        this.finalGradeValue = finalGradeValue;
    }

    public ModuleResultStatus getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(ModuleResultStatus resultStatus) {
        this.resultStatus = resultStatus;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(Instant calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
