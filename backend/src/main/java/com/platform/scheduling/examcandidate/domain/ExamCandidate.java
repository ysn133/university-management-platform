package com.platform.scheduling.examcandidate.domain;

import com.platform.academicregistration.moduleregistration.domain.ModuleRegistration;
import com.platform.scheduling.moduleexam.domain.ModuleExam;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "exam_candidate")
public class ExamCandidate {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "module_exam_id", nullable = false)
    private ModuleExam moduleExam;

    @ManyToOne(optional = false)
    @JoinColumn(name = "module_registration_id", nullable = false)
    private ModuleRegistration moduleRegistration;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ExamCandidate() {
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

    public ModuleExam getModuleExam() {
        return moduleExam;
    }

    public void setModuleExam(ModuleExam moduleExam) {
        this.moduleExam = moduleExam;
    }

    public ModuleRegistration getModuleRegistration() {
        return moduleRegistration;
    }

    public void setModuleRegistration(ModuleRegistration moduleRegistration) {
        this.moduleRegistration = moduleRegistration;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
