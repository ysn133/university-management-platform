package com.platform.ai.retrieval.infrastructure;

import com.platform.ai.retrieval.domain.KnowledgeChunk;
import com.platform.ai.retrieval.domain.KnowledgeCorpus;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
public class ClasspathKnowledgeCorpus implements KnowledgeCorpus {

    private static final Logger log = LoggerFactory.getLogger(ClasspathKnowledgeCorpus.class);

    private final List<KnowledgeChunk> chunks;

    public ClasspathKnowledgeCorpus(
        ResourcePatternResolver resourcePatternResolver,
        MarkdownKnowledgeChunker chunker,
        @Value("${app.ai.retrieval.knowledge-pattern}") String knowledgePattern,
        @Value("${app.ai.retrieval.maximum-chunk-characters}") int maximumChunkCharacters
    ) {
        this.chunks = load(
            resourcePatternResolver,
            chunker,
            knowledgePattern,
            maximumChunkCharacters
        );
        log.info("Loaded {} AI knowledge chunks", chunks.size());
    }

    @Override
    public List<KnowledgeChunk> chunks() {
        return chunks;
    }

    private List<KnowledgeChunk> load(
        ResourcePatternResolver resolver,
        MarkdownKnowledgeChunker chunker,
        String pattern,
        int maximumChunkCharacters
    ) {
        try {
            Resource[] resources = resolver.getResources(pattern);
            if (resources.length == 0) {
                throw new IllegalStateException("No AI knowledge documents matched " + pattern);
            }

            List<KnowledgeChunk> loaded = new ArrayList<>();
            Arrays.sort(resources, Comparator.comparing(resource ->
                resource.getFilename() == null ? "" : resource.getFilename()
            ));
            for (Resource resource : resources) {
                loaded.addAll(chunker.chunk(
                    resource.getFilename(),
                    resource.getContentAsString(StandardCharsets.UTF_8),
                    maximumChunkCharacters
                ));
            }
            if (loaded.isEmpty()) {
                throw new IllegalStateException("AI knowledge documents produced no chunks");
            }
            return List.copyOf(loaded);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load AI knowledge documents", exception);
        }
    }

}
