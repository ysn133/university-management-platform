package com.platform.teachingassignment.rankpreference.domain;

import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentType;
import com.platform.usermanagement.professor.rank.domain.AcademicRank;
import com.platform.usermanagement.professor.rank.domain.AcademicRankStatus;
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
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "teaching_assignment_rank_preference")
public class TeachingAssignmentRankPreference {
    @Id private UUID id;
    @ManyToOne(optional = false) @JoinColumn(name = "establishment_id", nullable = false)
    private Establishment establishment;
    @Enumerated(EnumType.STRING) @Column(name = "component_type", nullable = false)
    private TeachingComponentType componentType;
    @ManyToOne(optional = false) @JoinColumn(name = "academic_rank_id", nullable = false)
    private AcademicRank academicRank;
    @Column(name = "priority", nullable = false) private Integer priority;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false)
    private AcademicRankStatus status;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @PrePersist void onCreate() { Instant now = Instant.now(); if (id == null) id = UUID.randomUUID(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public Establishment getEstablishment() { return establishment; }
    public void setEstablishment(Establishment value) { establishment = value; }
    public TeachingComponentType getComponentType() { return componentType; }
    public void setComponentType(TeachingComponentType value) { componentType = value; }
    public AcademicRank getAcademicRank() { return academicRank; }
    public void setAcademicRank(AcademicRank value) { academicRank = value; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer value) { priority = value; }
    public AcademicRankStatus getStatus() { return status; }
    public void setStatus(AcademicRankStatus value) { status = value; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
