package com.platform.academicregistration.semesterregistration.domain;

import java.time.Instant;
import java.util.UUID;

import com.platform.academicregistration.registration.domain.AcademicRegistration;
import com.platform.universitygovernance.semester.domain.Semester;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "semester_registration")
public class SemesterRegistration {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name="academic_registration_id")
    private AcademicRegistration academicRegistration;

    @ManyToOne(optional = false)
    @JoinColumn(name="semester_id")
    private Semester semester;



    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public SemesterRegistration(){}


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

    public AcademicRegistration getAcademicRegistration() {
        return academicRegistration;
    }

    public void setAcademicRegistration(AcademicRegistration academicRegistration) {
        this.academicRegistration = academicRegistration;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
