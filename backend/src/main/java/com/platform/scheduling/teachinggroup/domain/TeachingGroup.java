package com.platform.scheduling.teachinggroup.domain;

import com.platform.universitygovernance.classgroup.domain.ClassGroup;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingAudienceMode;
import com.platform.universitygovernance.semester.domain.Semester;
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
@Table(name = "teaching_group")
public class TeachingGroup {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @ManyToOne
    @JoinColumn(name = "source_class_group_id")
    private ClassGroup sourceClassGroup;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "audience_type", nullable = false)
    private TeachingAudienceMode audienceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_type")
    private TeachingGroupType groupType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public TeachingGroup() {
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

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public ClassGroup getSourceClassGroup() {
        return sourceClassGroup;
    }

    public void setSourceClassGroup(ClassGroup sourceClassGroup) {
        this.sourceClassGroup = sourceClassGroup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TeachingAudienceMode getAudienceType() {
        return audienceType;
    }

    public void setAudienceType(TeachingAudienceMode audienceType) {
        this.audienceType = audienceType;
    }

    public TeachingGroupType getGroupType() {
        return groupType;
    }

    public void setGroupType(TeachingGroupType groupType) {
        this.groupType = groupType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
