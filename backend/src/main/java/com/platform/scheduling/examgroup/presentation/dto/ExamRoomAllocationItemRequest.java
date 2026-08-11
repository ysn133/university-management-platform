package com.platform.scheduling.examgroup.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ExamRoomAllocationItemRequest(
    @NotNull UUID examGroupId,
    @NotNull UUID roomId
) {}
