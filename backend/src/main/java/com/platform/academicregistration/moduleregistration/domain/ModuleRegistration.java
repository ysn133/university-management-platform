package com.platform.academicregistration.moduleregistration.domain;

import java.time.Instant;
import java.util.UUID;


import com.platform.academicregistration.semesterregistration.domain.SemesterRegistration;
import com.platform.universitygovernance.academiclevel.domain.AcademicLevel;
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

@Entity
@Table(name="module_registration")
public class ModuleRegistration {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name="semester_registration_id", nullable = false)
    private SemesterRegistration semesterRegistration;

    @ManyToOne(optional = false)
    @JoinColumn(name="subject_module_id", nullable = false)
    private SubjectModule subjectModule;

    @ManyToOne
    @JoinColumn(name="origin_academic_level_id")
    private AcademicLevel originAcademicLevel;

    @Column(name = "inscription_number", nullable = false)
    private int inscriptionNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ModuleRegistrationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ModuleRegistration() {
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

    public SemesterRegistration getSemesterRegistration() {
        return semesterRegistration;
    }

    public void setSemesterRegistration(SemesterRegistration semesterRegistration) {
        this.semesterRegistration = semesterRegistration;
    }

    public SubjectModule getSubjectModule() {
        return subjectModule;
    }

    public void setSubjectModule(SubjectModule subjectModule) {
        this.subjectModule = subjectModule;
    }

    public AcademicLevel getOriginAcademicLevel() {
        return originAcademicLevel;
    }

    public void setOriginAcademicLevel(AcademicLevel originAcademicLevel) {
        this.originAcademicLevel = originAcademicLevel;
    }

    public int getInscriptionNumber() {
        return inscriptionNumber;
    }

    public void setInscriptionNumber(int inscriptionNumber) {
        this.inscriptionNumber = inscriptionNumber;
    }

    public ModuleRegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(ModuleRegistrationStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }








}
