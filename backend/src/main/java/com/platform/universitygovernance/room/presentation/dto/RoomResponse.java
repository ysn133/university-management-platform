package com.platform.universitygovernance.room.presentation.dto;

import com.platform.scheduling.domain.RoomType;
import com.platform.universitygovernance.room.domain.RoomStatus;
import java.time.Instant;
import java.util.UUID;

public record RoomResponse(
    UUID id,
    UUID establishmentId,
    UUID blockId,
    String blockCode,
    String code,
    String name,
    RoomType roomType,
    int capacity,
    RoomStatus status,
    Instant createdAt,
    Instant updatedAt
) {
}
