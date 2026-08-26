package com.platform.ai.retrieval.presentation.dto;

import java.util.List;

public record KnowledgeRetrievalResponse(
    String query,
    int matchCount,
    List<KnowledgeMatchResponse> apiMatches,
    List<KnowledgeMatchResponse> uiMatches,
    String context
) {
}
