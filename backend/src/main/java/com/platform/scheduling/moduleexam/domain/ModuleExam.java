package com.platform.scheduling.moduleexam.domain;

import com.platform.scheduling.examschedule.domain.ExamSchedule;
import com.platform.teachingassignment.domain.TeachingAssignment;
import com.platform.universitygovernance.classgroup.domain.ClassGroup;
import com.platform.universitygovernance.subjectmodules.domain.SubjectModule;
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
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "module_exam")
public class ModuleExam {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exam_schedule_id", nullable = false)
    private ExamSchedule examSchedule;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_module_id", nullable = false)
    private SubjectModule subjectModule;

    @ManyToOne(optional = false)
    @JoinColumn(name = "class_group_id", nullable = false)
    private ClassGroup classGroup;

    @ManyToOne
    @JoinColumn(name = "teaching_assignment_id")
    private TeachingAssignment teachingAssignment;

    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "location")
    private String location;

    @Column(name = "candidate_list_generated_at")
    private Instant candidateListGeneratedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ModuleExam() {
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

    public ExamSchedule getExamSchedule() {
        return examSchedule;
    }

    public void setExamSchedule(ExamSchedule examSchedule) {
        this.examSchedule = examSchedule;
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

    public TeachingAssignment getTeachingAssignment() {
        return teachingAssignment;
    }

    public void setTeachingAssignment(TeachingAssignment teachingAssignment) {
        this.teachingAssignment = teachingAssignment;
    }

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Instant getCandidateListGeneratedAt() {
        return candidateListGeneratedAt;
    }

    public void setCandidateListGeneratedAt(Instant candidateListGeneratedAt) {
        this.candidateListGeneratedAt = candidateListGeneratedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
