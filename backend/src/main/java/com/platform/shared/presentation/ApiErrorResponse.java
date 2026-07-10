package com.platform.shared.presentation;

public record ApiErrorResponse(
    int error,
    String message
) {
}
