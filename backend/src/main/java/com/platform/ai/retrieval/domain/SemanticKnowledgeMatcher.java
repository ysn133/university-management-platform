package com.platform.ai.retrieval.domain;

import java.util.List;

public interface SemanticKnowledgeMatcher {

    List<KnowledgeMatch> match(String query, KnowledgeSource source, int limit);
}
