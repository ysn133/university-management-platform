package com.platform.ai.navigation.presentation.dto;

import com.platform.ai.navigation.domain.NavigationDebugTrace;

public record AiNavigationErrorResponse(
    int error,
    String message,
    NavigationDebugTrace diagnostics
) {
}
