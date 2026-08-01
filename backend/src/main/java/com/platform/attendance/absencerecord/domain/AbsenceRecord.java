package com.platform.attendance.absencerecord.domain;

import com.platform.academicregistration.moduleregistration.domain.ModuleRegistration;
import com.platform.teachingassignment.domain.TeachingAssignment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "absence_record")
public class AbsenceRecord {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "module_registration_id", nullable = false)
    private ModuleRegistration moduleRegistration;

    @ManyToOne(optional = false)
    @JoinColumn(name = "teaching_assignment_id", nullable = false)
    private TeachingAssignment teachingAssignment;

    @Column(name = "absence_date", nullable = false)
    private LocalDate absenceDate;

    @Column(name = "justified", nullable = false)
    private boolean justified;

    @Column(name = "justification_note", length = 1000)
    private String justificationNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AbsenceRecord() {
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

    public ModuleRegistration getModuleRegistration() {
        return moduleRegistration;
    }

    public void setModuleRegistration(ModuleRegistration moduleRegistration) {
        this.moduleRegistration = moduleRegistration;
    }

    public TeachingAssignment getTeachingAssignment() {
        return teachingAssignment;
    }

    public void setTeachingAssignment(TeachingAssignment teachingAssignment) {
        this.teachingAssignment = teachingAssignment;
    }

    public LocalDate getAbsenceDate() {
        return absenceDate;
    }

    public void setAbsenceDate(LocalDate absenceDate) {
        this.absenceDate = absenceDate;
    }

    public boolean isJustified() {
        return justified;
    }

    public void setJustified(boolean justified) {
        this.justified = justified;
    }

    public String getJustificationNote() {
        return justificationNote;
    }

    public void setJustificationNote(String justificationNote) {
        this.justificationNote = justificationNote;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
