package com.platform.universitygovernance.subjectmodules.domain;

import com.platform.universitygovernance.academicdomain.domain.AcademicDomain;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "subject_module_domain")
public class SubjectModuleDomain {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_module_id", nullable = false)
    private SubjectModule subjectModule;

    @ManyToOne(optional = false)
    @JoinColumn(name = "academic_domain_id", nullable = false)
    private AcademicDomain academicDomain;

    public SubjectModuleDomain() {
    }

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
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

    public AcademicDomain getAcademicDomain() {
        return academicDomain;
    }

    public void setAcademicDomain(AcademicDomain academicDomain) {
        this.academicDomain = academicDomain;
    }
}
