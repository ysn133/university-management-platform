package com.platform.universitygovernance.academicruleprofile.domain;

import com.platform.universitygovernance.establishment.domain.Establishment;
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
@Table(name = "academic_rule_profile")
public class AcademicRuleProfile {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "establishment_id", nullable = false)
    private Establishment establishment;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "module_validation_threshold", nullable = false, precision = 5, scale = 2)
    private BigDecimal moduleValidationThreshold;

    @Column(name = "compensation_minimum_threshold", nullable = false, precision = 5, scale = 2)
    private BigDecimal compensationMinimumThreshold;

    @Column(name = "semester_validation_average", nullable = false, precision = 5, scale = 2)
    private BigDecimal semesterValidationAverage;

    @Column(name = "annual_validation_average", precision = 5, scale = 2)
    private BigDecimal annualValidationAverage;

    @Column(name = "maximum_module_inscriptions", nullable = false)
    private int maximumModuleInscriptions;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_grade_policy", nullable = false)
    private SessionGradePolicy sessionGradePolicy;

    @Column(name = "allow_progression_with_debt", nullable = false)
    private boolean allowProgressionWithDebt;

    @Column(name = "maximum_carried_modules", nullable = false)
    private int maximumCarriedModules;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AcademicRuleProfileStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AcademicRuleProfile() {
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

    public Establishment getEstablishment() {
        return establishment;
    }

    public void setEstablishment(Establishment establishment) {
        this.establishment = establishment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public BigDecimal getModuleValidationThreshold() {
        return moduleValidationThreshold;
    }

    public void setModuleValidationThreshold(BigDecimal moduleValidationThreshold) {
        this.moduleValidationThreshold = moduleValidationThreshold;
    }

    public BigDecimal getCompensationMinimumThreshold() {
        return compensationMinimumThreshold;
    }

    public void setCompensationMinimumThreshold(BigDecimal compensationMinimumThreshold) {
        this.compensationMinimumThreshold = compensationMinimumThreshold;
    }

    public BigDecimal getSemesterValidationAverage() {
        return semesterValidationAverage;
    }

    public void setSemesterValidationAverage(BigDecimal semesterValidationAverage) {
        this.semesterValidationAverage = semesterValidationAverage;
    }

    public BigDecimal getAnnualValidationAverage() {
        return annualValidationAverage;
    }

    public void setAnnualValidationAverage(BigDecimal annualValidationAverage) {
        this.annualValidationAverage = annualValidationAverage;
    }

    public int getMaximumModuleInscriptions() {
        return maximumModuleInscriptions;
    }

    public void setMaximumModuleInscriptions(int maximumModuleInscriptions) {
        this.maximumModuleInscriptions = maximumModuleInscriptions;
    }

    public SessionGradePolicy getSessionGradePolicy() {
        return sessionGradePolicy;
    }

    public void setSessionGradePolicy(SessionGradePolicy sessionGradePolicy) {
        this.sessionGradePolicy = sessionGradePolicy;
    }

    public boolean isAllowProgressionWithDebt() {
        return allowProgressionWithDebt;
    }

    public void setAllowProgressionWithDebt(boolean allowProgressionWithDebt) {
        this.allowProgressionWithDebt = allowProgressionWithDebt;
    }

    public int getMaximumCarriedModules() {
        return maximumCarriedModules;
    }

    public void setMaximumCarriedModules(int maximumCarriedModules) {
        this.maximumCarriedModules = maximumCarriedModules;
    }

    public AcademicRuleProfileStatus getStatus() {
        return status;
    }

    public void setStatus(AcademicRuleProfileStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
