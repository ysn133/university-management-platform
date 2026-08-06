package com.platform.universitygovernance.moduleteachingcomponent.domain;

import com.platform.scheduling.domain.RoomType;
import com.platform.universitygovernance.subjectmodules.domain.SubjectModule;
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
@Table(name = "module_teaching_component")
public class ModuleTeachingComponent {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_module_id", nullable = false)
    private SubjectModule subjectModule;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false)
    private TeachingComponentType componentType;

    @Column(name = "sessions_per_week", nullable = false)
    private int sessionsPerWeek;

    @Column(name = "session_duration_minutes", nullable = false)
    private int sessionDurationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "audience_mode", nullable = false)
    private TeachingAudienceMode audienceMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_room_type", nullable = false)
    private RoomType requiredRoomType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ModuleTeachingComponent() {
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

    public SubjectModule getSubjectModule() {
        return subjectModule;
    }

    public void setSubjectModule(SubjectModule subjectModule) {
        this.subjectModule = subjectModule;
    }

    public TeachingComponentType getComponentType() {
        return componentType;
    }

    public void setComponentType(TeachingComponentType componentType) {
        this.componentType = componentType;
    }

    public int getSessionsPerWeek() {
        return sessionsPerWeek;
    }

    public void setSessionsPerWeek(int sessionsPerWeek) {
        this.sessionsPerWeek = sessionsPerWeek;
    }

    public int getSessionDurationMinutes() {
        return sessionDurationMinutes;
    }

    public void setSessionDurationMinutes(int sessionDurationMinutes) {
        this.sessionDurationMinutes = sessionDurationMinutes;
    }

    public TeachingAudienceMode getAudienceMode() {
        return audienceMode;
    }

    public void setAudienceMode(TeachingAudienceMode audienceMode) {
        this.audienceMode = audienceMode;
    }

    public RoomType getRequiredRoomType() {
        return requiredRoomType;
    }

    public void setRequiredRoomType(RoomType requiredRoomType) {
        this.requiredRoomType = requiredRoomType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
