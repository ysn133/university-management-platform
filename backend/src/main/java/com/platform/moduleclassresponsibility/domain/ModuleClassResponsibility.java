package com.platform.moduleclassresponsibility.domain;

import com.platform.identityaccess.domain.Professor;
import com.platform.universitygovernance.academicyear.domain.AcademicYear;
import com.platform.universitygovernance.classgroup.domain.ClassGroup;
import com.platform.universitygovernance.semester.domain.Semester;
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
@Table(name = "module_class_responsibility")
public class ModuleClassResponsibility {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_module_id", nullable = false)
    private SubjectModule subjectModule;

    @ManyToOne(optional = false)
    @JoinColumn(name = "class_group_id", nullable = false)
    private ClassGroup classGroup;

    @ManyToOne(optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @ManyToOne(optional = false)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ModuleClassResponsibilityStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ModuleClassResponsibility() {
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

    public Professor getProfessor() {
        return professor;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }

    public SubjectModule getSubjectModule() {
        return subjectModule;
    }

    public void setSubjectModule(SubjectModule subjectModule) {
        this.subjectModule = subjectModule;
    }

    public ClassGroup getClassGroup() {
        return classGroup;
    }

    public void setClassGroup(ClassGroup classGroup) {
        this.classGroup = classGroup;
    }

    public AcademicYear getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(AcademicYear academicYear) {
        this.academicYear = academicYear;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public ModuleClassResponsibilityStatus getStatus() {
        return status;
    }

    public void setStatus(ModuleClassResponsibilityStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
