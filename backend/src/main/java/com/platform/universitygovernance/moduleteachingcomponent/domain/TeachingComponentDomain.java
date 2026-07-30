package com.platform.universitygovernance.moduleteachingcomponent.domain;

import com.platform.universitygovernance.academicdomain.domain.AcademicDomain;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "teaching_component_domain")
public class TeachingComponentDomain {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "module_teaching_component_id", nullable = false)
    private ModuleTeachingComponent moduleTeachingComponent;

    @ManyToOne(optional = false)
    @JoinColumn(name = "academic_domain_id", nullable = false)
    private AcademicDomain academicDomain;

    public TeachingComponentDomain() {
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

    public ModuleTeachingComponent getModuleTeachingComponent() {
        return moduleTeachingComponent;
    }

    public void setModuleTeachingComponent(ModuleTeachingComponent moduleTeachingComponent) {
        this.moduleTeachingComponent = moduleTeachingComponent;
    }

    public AcademicDomain getAcademicDomain() {
        return academicDomain;
    }

    public void setAcademicDomain(AcademicDomain academicDomain) {
        this.academicDomain = academicDomain;
    }
}
