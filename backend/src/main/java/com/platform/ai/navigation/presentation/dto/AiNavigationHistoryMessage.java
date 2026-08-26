package com.platform.ai.navigation.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AiNavigationHistoryMessage(
    @NotBlank
    @Pattern(regexp = "USER|ASSISTANT")
    String role,

    @NotBlank
    @Size(max = 2000)
    String content
) {
}
