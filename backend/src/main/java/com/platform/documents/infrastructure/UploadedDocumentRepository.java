package com.platform.documents.infrastructure;

import com.platform.documents.domain.UploadedDocument;
import java.util.UUID;
import java.time.Instant;
import java.util.List;
import com.platform.documents.domain.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedDocumentRepository extends JpaRepository<UploadedDocument, UUID> {
    List<UploadedDocument> findByStatusAndCreatedAtBefore(DocumentStatus status, Instant cutoff);
}
