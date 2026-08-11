package com.platform.scheduling.examgroup.domain;

import com.platform.scheduling.examschedule.domain.ExamSchedule;
import com.platform.universitygovernance.classgroup.domain.ClassGroup;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "exam_group")
public class ExamGroup {
    @Id private UUID id;
    @ManyToOne(optional = false) @JoinColumn(name = "exam_schedule_id", nullable = false) private ExamSchedule examSchedule;
    @ManyToOne(optional = false) @JoinColumn(name = "class_group_id", nullable = false) private ClassGroup classGroup;
    @Column(nullable = false, length = 50) private String label;
    @Column(name = "group_order", nullable = false) private int groupOrder;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public ExamGroup() {}
    @PrePersist void onCreate() { Instant now = Instant.now(); if (id == null) id = UUID.randomUUID(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public ExamSchedule getExamSchedule() { return examSchedule; }
    public void setExamSchedule(ExamSchedule examSchedule) { this.examSchedule = examSchedule; }
    public ClassGroup getClassGroup() { return classGroup; }
    public void setClassGroup(ClassGroup classGroup) { this.classGroup = classGroup; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public int getGroupOrder() { return groupOrder; }
    public void setGroupOrder(int groupOrder) { this.groupOrder = groupOrder; }
}
