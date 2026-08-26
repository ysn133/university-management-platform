package com.platform.ai.retrieval;

import static org.assertj.core.api.Assertions.assertThat;

import com.platform.ai.retrieval.domain.KnowledgeChunk;
import com.platform.ai.retrieval.domain.KnowledgeSource;
import com.platform.ai.retrieval.infrastructure.MarkdownKnowledgeChunker;
import java.util.List;
import org.junit.jupiter.api.Test;

class MarkdownKnowledgeChunkerTest {

    private final MarkdownKnowledgeChunker chunker = new MarkdownKnowledgeChunker();

    @Test
    void keepsApiEndpointsInIndependentChunksWithHeadingContext() {
        String markdown = """
            # API Knowledge

            ## Students

            Student introduction.

            - `GET /api/v1/students/{studentId}` returns one student.
            - `GET /api/v1/students/{studentId}/grades` returns grades.
            """;

        List<KnowledgeChunk> chunks = chunker.chunk("api-knowledge.md", markdown, 1800);

        assertThat(chunks).hasSize(3);
        assertThat(chunks).allMatch(chunk -> chunk.source() == KnowledgeSource.API);
        assertThat(chunks).allMatch(chunk -> chunk.title().equals("API Knowledge > Students"));
        assertThat(chunks.get(1).content()).contains("GET /api/v1/students/{studentId}");
        assertThat(chunks.get(2).content()).contains("GET /api/v1/students/{studentId}/grades");
    }

    @Test
    void splitsLargeSectionsWithoutDroppingText() {
        String markdown = "# UI\n\n## Routes\n\nFirst paragraph.\n\nSecond paragraph.";

        List<KnowledgeChunk> chunks = chunker.chunk("ui-navigation-knowledge.md", markdown, 20);

        assertThat(chunks).hasSizeGreaterThanOrEqualTo(2);
        assertThat(chunks.stream().map(KnowledgeChunk::content).toList())
            .anyMatch(content -> content.contains("First paragraph"))
            .anyMatch(content -> content.contains("Second paragraph"));
    }
}
