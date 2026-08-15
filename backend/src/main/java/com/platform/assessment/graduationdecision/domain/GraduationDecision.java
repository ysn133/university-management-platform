package com.platform.assessment.graduationdecision.domain;

import com.platform.academicregistration.registration.domain.AcademicRegistration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "graduation_decision")
public class GraduationDecision {

    @Id
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "terminal_academic_registration_id", nullable = false, unique = true)
    private AcademicRegistration terminalAcademicRegistration;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_status", nullable = false)
    private GraduationDecisionStatus decisionStatus;

    @Column(name = "graduation_average", nullable = false, precision = 5, scale = 2)
    private BigDecimal graduationAverage;

    @Column(name = "decided_at", nullable = false)
    private Instant decidedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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

    public UUID getId() { return id; }
    public AcademicRegistration getTerminalAcademicRegistration() { return terminalAcademicRegistration; }
    public void setTerminalAcademicRegistration(AcademicRegistration value) { terminalAcademicRegistration = value; }
    public GraduationDecisionStatus getDecisionStatus() { return decisionStatus; }
    public void setDecisionStatus(GraduationDecisionStatus value) { decisionStatus = value; }
    public BigDecimal getGraduationAverage() { return graduationAverage; }
    public void setGraduationAverage(BigDecimal value) { graduationAverage = value; }
    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant value) { decidedAt = value; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
