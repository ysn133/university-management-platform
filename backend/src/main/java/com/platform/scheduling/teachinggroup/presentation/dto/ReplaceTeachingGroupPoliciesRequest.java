package com.platform.scheduling.teachinggroup.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReplaceTeachingGroupPoliciesRequest(
    @NotNull List<@Valid TeachingGroupPolicyItemRequest> policies
) {
}
