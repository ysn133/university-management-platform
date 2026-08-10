package com.platform.teachingassignment.domain;

import com.platform.identityaccess.domain.Professor;
import com.platform.teachingrequirement.domain.TeachingRequirement;
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
@Table(name = "teaching_assignment")
public class TeachingAssignment {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "teaching_requirement_id", nullable = false)
    private TeachingRequirement teachingRequirement;

    @ManyToOne(optional = false)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TeachingAssignmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_source", nullable = false)
    private TeachingAssignmentSource assignmentSource;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public TeachingAssignment() {
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (assignmentSource == null) {
            assignmentSource = TeachingAssignmentSource.MANUAL;
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

    public TeachingRequirement getTeachingRequirement() {
        return teachingRequirement;
    }

    public void setTeachingRequirement(TeachingRequirement teachingRequirement) {
        this.teachingRequirement = teachingRequirement;
    }

    public Professor getProfessor() {
        return professor;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }

    public TeachingAssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(TeachingAssignmentStatus status) {
        this.status = status;
    }

    public TeachingAssignmentSource getAssignmentSource() {
        return assignmentSource;
    }

    public void setAssignmentSource(TeachingAssignmentSource assignmentSource) {
        this.assignmentSource = assignmentSource;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
