package com.platform.scheduling.examgroup.presentation.dto;

import java.util.UUID;

public record ExamRoomAllocationResponse(
    UUID id,
    UUID examGroupId,
    String examGroupLabel,
    long studentCount,
    UUID roomId,
    String roomCode,
    String roomName,
    int roomCapacity
) {}
