package com.platform.ai.retrieval.domain;

public record KnowledgeChunk(
    String id,
    KnowledgeSource source,
    String title,
    String content
) {
}
