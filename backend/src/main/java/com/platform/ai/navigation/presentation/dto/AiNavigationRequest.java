package com.platform.ai.navigation.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AiNavigationRequest(
    @NotBlank
    @Size(max = 500)
    String query,

    @Size(max = 1500)
    String currentRoute,

    @Valid
    @Size(max = 5)
    List<AiNavigationHistoryMessage> history
) {
}
