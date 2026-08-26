CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE ai_knowledge_embedding (
    chunk_id VARCHAR(36) PRIMARY KEY,
    source VARCHAR(20) NOT NULL,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    embedding vector(384) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ai_knowledge_embedding_source
    ON ai_knowledge_embedding (source);

CREATE INDEX idx_ai_knowledge_embedding_cosine
    ON ai_knowledge_embedding
    USING hnsw (embedding vector_cosine_ops);
