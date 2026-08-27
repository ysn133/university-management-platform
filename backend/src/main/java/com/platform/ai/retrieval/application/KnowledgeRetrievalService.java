package com.platform.ai.retrieval.application;

import com.platform.ai.retrieval.domain.KnowledgeMatch;
import com.platform.ai.retrieval.domain.KnowledgeMatcher;
import com.platform.ai.retrieval.domain.KnowledgeSource;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeRetrievalService {

    private final KnowledgeMatcher matcher;

    public KnowledgeRetrievalService(KnowledgeMatcher matcher) {
        this.matcher = matcher;
    }

    public RetrievalResult retrieve(String query, int limitPerSource) {
        String normalizedQuery = query.trim();
        List<KnowledgeMatch> apiMatches = matcher.match(
            normalizedQuery,
            KnowledgeSource.API,
            limitPerSource
        );
        List<KnowledgeMatch> uiMatches = matcher.match(
            normalizedQuery,
            KnowledgeSource.UI,
            limitPerSource
        );
        List<KnowledgeMatch> matches = java.util.stream.Stream
            .concat(apiMatches.stream(), uiMatches.stream())
            .toList();
        String context = matches.stream()
            .map(match -> "[" + match.chunk().source() + "] " + match.chunk().title()
                + "\n" + match.chunk().content())
            .reduce((left, right) -> left + "\n\n---\n\n" + right)
            .orElse("");
        return new RetrievalResult(normalizedQuery, apiMatches, uiMatches, context);
    }

    public record RetrievalResult(
        String query,
        List<KnowledgeMatch> apiMatches,
        List<KnowledgeMatch> uiMatches,
        String context
    ) {
        public List<KnowledgeMatch> matches() {
            return java.util.stream.Stream.concat(apiMatches.stream(), uiMatches.stream()).toList();
        }
    }
}
