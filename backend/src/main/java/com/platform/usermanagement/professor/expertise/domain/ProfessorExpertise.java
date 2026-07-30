package com.platform.usermanagement.professor.expertise.domain;

import com.platform.identityaccess.domain.Professor;
import com.platform.universitygovernance.academicdomain.domain.AcademicDomain;
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
@Table(name = "professor_expertise")
public class ProfessorExpertise {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "academic_domain_id", nullable = false)
    private AcademicDomain academicDomain;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ProfessorExpertise() {
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

    public Professor getProfessor() {
        return professor;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }

    public AcademicDomain getAcademicDomain() {
        return academicDomain;
    }

    public void setAcademicDomain(AcademicDomain academicDomain) {
        this.academicDomain = academicDomain;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
