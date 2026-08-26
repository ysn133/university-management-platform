package com.platform.ai.retrieval;

import static org.assertj.core.api.Assertions.assertThat;

import com.platform.ai.retrieval.application.KnowledgeRetrievalService;
import com.platform.ai.retrieval.domain.KnowledgeChunk;
import com.platform.ai.retrieval.domain.KnowledgeMatch;
import com.platform.ai.retrieval.domain.KnowledgeMatcher;
import com.platform.ai.retrieval.domain.KnowledgeSource;
import java.util.List;
import org.junit.jupiter.api.Test;

class KnowledgeRetrievalServiceTest {

    @Test
    void retrievesApiAndUiKnowledgeForEveryQuestion() {
        KnowledgeMatcher matcher = (query, source, limit) -> List.of(new KnowledgeMatch(
            new KnowledgeChunk(
                source.name().toLowerCase(),
                source,
                source + " result",
                source + " content"
            ),
            1.0
        ));
        KnowledgeRetrievalService service = new KnowledgeRetrievalService(matcher);

        KnowledgeRetrievalService.RetrievalResult result = service.retrieve(
            "show student grades",
            3
        );

        assertThat(result.apiMatches()).hasSize(1);
        assertThat(result.uiMatches()).hasSize(1);
        assertThat(result.context()).contains("[API]", "[UI]");
    }
}
