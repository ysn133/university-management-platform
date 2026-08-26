package com.platform.ai.retrieval.presentation.dto;

import com.platform.ai.retrieval.domain.KnowledgeSource;

public record KnowledgeMatchResponse(
    String id,
    KnowledgeSource source,
    String title,
    String content,
    double score
) {
}
