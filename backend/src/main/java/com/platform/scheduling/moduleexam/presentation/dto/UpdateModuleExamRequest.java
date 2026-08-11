package com.platform.scheduling.moduleexam.presentation.dto;

import com.platform.scheduling.examgroup.presentation.dto.ExamRoomAllocationItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record UpdateModuleExamRequest(
    @NotNull UUID subjectModuleId,
    @NotNull UUID classGroupId,
    @NotNull LocalDate examDate,
    @NotNull LocalTime startTime,
    LocalTime endTime,
    @Size(max = 255) String location,
    UUID roomId,
    List<@Valid ExamRoomAllocationItemRequest> roomAllocations
) {
    public UpdateModuleExamRequest(UUID subjectModuleId, UUID classGroupId, LocalDate examDate, LocalTime startTime, LocalTime endTime, String location) {
        this(subjectModuleId, classGroupId, examDate, startTime, endTime, location, null, null);
    }

    public UpdateModuleExamRequest(UUID subjectModuleId, UUID classGroupId, LocalDate examDate, LocalTime startTime, LocalTime endTime, String location, UUID roomId) {
        this(subjectModuleId, classGroupId, examDate, startTime, endTime, location, roomId, null);
    }
}
