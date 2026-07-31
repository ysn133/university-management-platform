package com.platform.teachingrequirement.domain;

import com.platform.scheduling.teachinggroup.domain.TeachingGroup;
import com.platform.universitygovernance.moduleteachingcomponent.domain.ModuleTeachingComponent;
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
@Table(name = "teaching_requirement")
public class TeachingRequirement {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "module_teaching_component_id", nullable = false)
    private ModuleTeachingComponent moduleTeachingComponent;

    @ManyToOne(optional = false)
    @JoinColumn(name = "teaching_group_id", nullable = false)
    private TeachingGroup teachingGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TeachingRequirementStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public TeachingRequirement() {
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

    public ModuleTeachingComponent getModuleTeachingComponent() {
        return moduleTeachingComponent;
    }

    public void setModuleTeachingComponent(ModuleTeachingComponent moduleTeachingComponent) {
        this.moduleTeachingComponent = moduleTeachingComponent;
    }

    public TeachingGroup getTeachingGroup() {
        return teachingGroup;
    }

    public void setTeachingGroup(TeachingGroup teachingGroup) {
        this.teachingGroup = teachingGroup;
    }

    public TeachingRequirementStatus getStatus() {
        return status;
    }

    public void setStatus(TeachingRequirementStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
