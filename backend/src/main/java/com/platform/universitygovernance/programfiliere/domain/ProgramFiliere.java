package com.platform.universitygovernance.programfiliere.domain;

import com.platform.universitygovernance.degreecycle.domain.DegreeCycle;
import com.platform.universitygovernance.department.domain.Department;
import com.platform.universitygovernance.programpath.domain.ProgramPath;
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
@Table(name = "program_filiere")
public class ProgramFiliere {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(optional = false)
    @JoinColumn(name = "degree_cycle_id", nullable = false)
    private DegreeCycle degreeCycle;

    @ManyToOne(optional = false)
    @JoinColumn(name = "program_path_id", nullable = false)
    private ProgramPath programPath;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ProgramFiliere() {
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

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public DegreeCycle getDegreeCycle() {
        return degreeCycle;
    }

    public void setDegreeCycle(DegreeCycle degreeCycle) {
        this.degreeCycle = degreeCycle;
    }

    public ProgramPath getProgramPath() {
        return programPath;
    }

    public void setProgramPath(ProgramPath programPath) {
        this.programPath = programPath;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
