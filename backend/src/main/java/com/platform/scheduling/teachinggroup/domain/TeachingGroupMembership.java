package com.platform.scheduling.teachinggroup.domain;

import com.platform.academicregistration.semesterregistration.domain.SemesterRegistration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "teaching_group_membership")
public class TeachingGroupMembership {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "teaching_group_id", nullable = false)
    private TeachingGroup teachingGroup;

    @ManyToOne(optional = false)
    @JoinColumn(name = "semester_registration_id", nullable = false)
    private SemesterRegistration semesterRegistration;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public TeachingGroupMembership() {
    }

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public TeachingGroup getTeachingGroup() {
        return teachingGroup;
    }

    public void setTeachingGroup(TeachingGroup teachingGroup) {
        this.teachingGroup = teachingGroup;
    }

    public SemesterRegistration getSemesterRegistration() {
        return semesterRegistration;
    }

    public void setSemesterRegistration(SemesterRegistration semesterRegistration) {
        this.semesterRegistration = semesterRegistration;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
