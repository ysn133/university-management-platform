package com.platform.universitygovernance.room.presentation.dto;

import com.platform.scheduling.domain.RoomType;
import com.platform.universitygovernance.room.domain.RoomStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateRoomRequest(
    UUID blockId,
    @NotBlank @Size(max = 50) String code,
    @NotBlank @Size(max = 255) String name,
    @NotNull RoomType roomType,
    @Positive int capacity,
    @NotNull RoomStatus status
) {
}
