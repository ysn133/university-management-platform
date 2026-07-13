package com.platform.universitygovernance.academiclevel.domain;

import com.platform.universitygovernance.programfiliere.domain.ProgramFiliere;
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
@Table(name = "academic_level")
public class AcademicLevel {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "program_filiere_id", nullable = false)
    private ProgramFiliere programFiliere;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "level_order", nullable = false)
    private int levelOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AcademicLevel() {
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

    public ProgramFiliere getProgramFiliere() {
        return programFiliere;
    }

    public void setProgramFiliere(ProgramFiliere programFiliere) {
        this.programFiliere = programFiliere;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevelOrder() {
        return levelOrder;
    }

    public void setLevelOrder(int levelOrder) {
        this.levelOrder = levelOrder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
