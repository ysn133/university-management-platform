package com.platform.ai.retrieval;

import static org.assertj.core.api.Assertions.assertThat;

import com.platform.ai.retrieval.domain.KnowledgeChunk;
import com.platform.ai.retrieval.domain.KnowledgeCorpus;
import com.platform.ai.retrieval.domain.KnowledgeMatch;
import com.platform.ai.retrieval.domain.KnowledgeSource;
import com.platform.ai.retrieval.infrastructure.HybridLexicalKnowledgeMatcher;
import com.platform.ai.retrieval.infrastructure.RetrievalTextAnalyzer;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class HybridKnowledgeMatcherTest {

    @Test
    void combinesSemanticMeaningWithExactLexicalMatches() {
        KnowledgeChunk lexicalChunk = new KnowledgeChunk(
            "lexical",
            KnowledgeSource.UI,
            "Grade page",
            "Open the grade page"
        );
        KnowledgeChunk semanticChunk = new KnowledgeChunk(
            "semantic",
            KnowledgeSource.UI,
            "Academic outcomes",
            "Review a learner's assessment history"
        );
        KnowledgeCorpus corpus = () -> List.of(lexicalChunk, semanticChunk);
        HybridLexicalKnowledgeMatcher matcher = new HybridLexicalKnowledgeMatcher(
            corpus,
            new RetrievalTextAnalyzer(),
            Optional.of((query, source, limit) -> List.of(
                new KnowledgeMatch(semanticChunk, 0.98),
                new KnowledgeMatch(lexicalChunk, 0.40)
            )),
            0.35,
            0.65
        );

        List<KnowledgeMatch> matches = matcher.match(
            "show grades",
            KnowledgeSource.UI,
            2
        );

        assertThat(matches).extracting(match -> match.chunk().id())
            .containsExactly("semantic", "lexical");
        assertThat(matches).allMatch(match -> match.score() > 0);
    }
}
