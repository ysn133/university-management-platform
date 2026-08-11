package com.platform.scheduling.examgroup.domain;

import com.platform.scheduling.moduleexam.domain.ModuleExam;
import com.platform.universitygovernance.room.domain.Room;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "exam_room_allocation")
public class ExamRoomAllocation {
    @Id private UUID id;
    @ManyToOne(optional = false) @JoinColumn(name = "module_exam_id", nullable = false) private ModuleExam moduleExam;
    @ManyToOne(optional = false) @JoinColumn(name = "exam_group_id", nullable = false) private ExamGroup examGroup;
    @ManyToOne(optional = false) @JoinColumn(name = "room_id", nullable = false) private Room room;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    public ExamRoomAllocation() {}
    @PrePersist void onCreate() { Instant now = Instant.now(); if (id == null) id = UUID.randomUUID(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public ModuleExam getModuleExam() { return moduleExam; }
    public void setModuleExam(ModuleExam moduleExam) { this.moduleExam = moduleExam; }
    public ExamGroup getExamGroup() { return examGroup; }
    public void setExamGroup(ExamGroup examGroup) { this.examGroup = examGroup; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
}
