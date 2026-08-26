package com.platform.ai.navigation.domain;

public record NavigationApiCall(
    String path,
    String queryParameters,
    int status,
    String responsePreview
) {
}
