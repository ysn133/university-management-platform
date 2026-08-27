package com.platform.ai.navigation.domain;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;

public record NavigationContext(
    String userQuery,
    String retrievalQuery,
    String knowledgeContext,
    String currentRoute,
    String conversationContext,
    AuthenticatedUserPrincipal principal
) {
}
