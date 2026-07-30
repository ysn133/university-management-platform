package com.platform.universitygovernance.moduleteachingcomponent.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ReplaceModuleTeachingComponentsRequest(
    @NotNull @Size(max = 3) List<@Valid ModuleTeachingComponentItemRequest> components
) {
}
