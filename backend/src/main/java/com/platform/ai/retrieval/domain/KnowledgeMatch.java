package com.platform.ai.retrieval.domain;

public record KnowledgeMatch(
    KnowledgeChunk chunk,
    double score
) {
}
