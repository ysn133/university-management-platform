package com.platform.ai.navigation.presentation.dto;

import com.platform.ai.navigation.domain.AiInteractionMode;
import com.platform.ai.navigation.domain.NavigationDebugTrace;

public record AiNavigationResponse(
    AiInteractionMode mode,
    String route,
    String message,
    NavigationDebugTrace diagnostics
) {
}
