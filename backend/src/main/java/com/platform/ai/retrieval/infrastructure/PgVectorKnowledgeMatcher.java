package com.platform.ai.retrieval.infrastructure;

import com.platform.ai.retrieval.domain.KnowledgeChunk;
import com.platform.ai.retrieval.domain.KnowledgeCorpus;
import com.platform.ai.retrieval.domain.KnowledgeMatch;
import com.platform.ai.retrieval.domain.KnowledgeSource;
import com.platform.ai.retrieval.domain.SemanticKnowledgeMatcher;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    prefix = "app.ai.retrieval",
    name = "semantic-enabled",
    havingValue = "true"
)
public class PgVectorKnowledgeMatcher implements SemanticKnowledgeMatcher, ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PgVectorKnowledgeMatcher.class);

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingModel embeddingModel;
    private final KnowledgeCorpus corpus;
    private volatile boolean ready;

    public PgVectorKnowledgeMatcher(
        JdbcTemplate jdbcTemplate,
        EmbeddingModel embeddingModel,
        KnowledgeCorpus corpus
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingModel = embeddingModel;
        this.corpus = corpus;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        try {
            synchronizeIndex();
            ready = true;
        } catch (RuntimeException exception) {
            ready = false;
            log.error("Semantic knowledge index is unavailable; lexical retrieval remains active", exception);
        }
    }

    @Override
    public List<KnowledgeMatch> match(String query, KnowledgeSource source, int limit) {
        if (!ready || limit <= 0) {
            return List.of();
        }

        String queryVector = vectorLiteral(embeddingModel.embed("query: " + query.trim()));
        String sql = """
            WITH query_vector AS (SELECT CAST(? AS vector) AS embedding)
            SELECT chunk_id, source, title, content,
                   1 - (stored.embedding <=> query_vector.embedding) AS similarity
            FROM ai_knowledge_embedding stored
            CROSS JOIN query_vector
            WHERE stored.source = ?
            ORDER BY stored.embedding <=> query_vector.embedding
            LIMIT ?
            """;
        return jdbcTemplate.query(
            sql,
            (resultSet, rowNumber) -> new KnowledgeMatch(
                new KnowledgeChunk(
                    resultSet.getString("chunk_id"),
                    KnowledgeSource.valueOf(resultSet.getString("source")),
                    resultSet.getString("title"),
                    resultSet.getString("content")
                ),
                resultSet.getDouble("similarity")
            ),
            queryVector,
            source.name(),
            limit
        );
    }

    private void synchronizeIndex() {
        Map<String, String> storedHashes = new HashMap<>();
        jdbcTemplate.query(
            "SELECT chunk_id, content_hash FROM ai_knowledge_embedding",
            (RowCallbackHandler) resultSet -> storedHashes.put(
                resultSet.getString("chunk_id"),
                resultSet.getString("content_hash")
            )
        );

        List<IndexedContent> changed = corpus.chunks().stream()
            .map(chunk -> new IndexedContent(chunk, hash(chunk)))
            .filter(indexed -> !indexed.hash().equals(storedHashes.get(indexed.chunk().id())))
            .toList();

        if (!changed.isEmpty()) {
            List<String> passages = changed.stream()
                .map(indexed -> passage(indexed.chunk()))
                .toList();
            List<float[]> embeddings = embeddingModel.embed(passages);
            for (int index = 0; index < changed.size(); index++) {
                upsert(changed.get(index), embeddings.get(index));
            }
        }

        Set<String> currentIds = corpus.chunks().stream()
            .map(KnowledgeChunk::id)
            .collect(java.util.stream.Collectors.toCollection(HashSet::new));
        storedHashes.keySet().stream()
            .filter(storedId -> !currentIds.contains(storedId))
            .forEach(storedId -> jdbcTemplate.update(
                "DELETE FROM ai_knowledge_embedding WHERE chunk_id = ?",
                storedId
            ));

        log.info(
            "Semantic knowledge index ready with {} chunks ({} embedded or updated)",
            corpus.chunks().size(),
            changed.size()
        );
    }

    private void upsert(IndexedContent indexed, float[] embedding) {
        KnowledgeChunk chunk = indexed.chunk();
        jdbcTemplate.update(
            """
                INSERT INTO ai_knowledge_embedding
                    (chunk_id, source, title, content, content_hash, embedding, updated_at)
                VALUES (?, ?, ?, ?, ?, CAST(? AS vector), CURRENT_TIMESTAMP)
                ON CONFLICT (chunk_id) DO UPDATE SET
                    source = EXCLUDED.source,
                    title = EXCLUDED.title,
                    content = EXCLUDED.content,
                    content_hash = EXCLUDED.content_hash,
                    embedding = EXCLUDED.embedding,
                    updated_at = CURRENT_TIMESTAMP
                """,
            chunk.id(),
            chunk.source().name(),
            chunk.title(),
            chunk.content(),
            indexed.hash(),
            vectorLiteral(embedding)
        );
    }

    private String passage(KnowledgeChunk chunk) {
        return "passage: " + chunk.title() + "\n" + chunk.content();
    }

    private String hash(KnowledgeChunk chunk) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] value = digest.digest(passage(chunk).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(value);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private String vectorLiteral(float[] vector) {
        StringBuilder result = new StringBuilder(vector.length * 12).append('[');
        for (int index = 0; index < vector.length; index++) {
            if (index > 0) {
                result.append(',');
            }
            result.append(vector[index]);
        }
        return result.append(']').toString();
    }

    private record IndexedContent(KnowledgeChunk chunk, String hash) {
    }
}
