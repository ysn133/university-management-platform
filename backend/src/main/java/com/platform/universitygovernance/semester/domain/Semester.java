package com.platform.universitygovernance.semester.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.platform.universitygovernance.academiclevel.domain.AcademicLevel;
import com.platform.universitygovernance.academicyear.domain.AcademicYear;

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
@Table(name = "semester")
public class Semester {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "academic_level_id", nullable = false)
    private AcademicLevel academicLevel;

    @ManyToOne(optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "semester_order", nullable = false)
    private int semesterOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "term_type", nullable = false)
    private SemesterTermType termType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Semester() {
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (termType == null) {
            termType = semesterOrder % 2 == 1
                ? SemesterTermType.AUTUMN
                : SemesterTermType.SPRING;
        }
        if (startDate == null || endDate == null) {
            boolean autumn = termType == SemesterTermType.AUTUMN;
            startDate = LocalDate.of(autumn ? academicYear.getStartYear() : academicYear.getEndYear(), autumn ? 9 : 2, 1);
            endDate = LocalDate.of(academicYear.getEndYear(), autumn ? 1 : 6, autumn ? 31 : 30);
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

    public AcademicLevel getAcademicLevel() {
        return academicLevel;
    }

    public void setAcademicLevel(AcademicLevel academicLevel) {
        this.academicLevel = academicLevel;
    }

    public AcademicYear getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(AcademicYear academicYear) {
        this.academicYear = academicYear;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSemesterOrder() {
        return semesterOrder;
    }

    public void setSemesterOrder(int semesterOrder) {
        this.semesterOrder = semesterOrder;
    }

    public SemesterTermType getTermType() {
        return termType;
    }

    public void setTermType(SemesterTermType termType) {
        this.termType = termType;
    }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
