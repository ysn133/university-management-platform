package com.platform.scheduling.examgroup.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ReplaceExamRoomAllocationsRequest(
    @NotEmpty List<@Valid ExamRoomAllocationItemRequest> allocations
) {}
