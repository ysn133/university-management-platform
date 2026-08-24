package com.platform.documents.presentation.dto;

import com.platform.documents.domain.DocumentPurpose;
import com.platform.documents.domain.DocumentStatus;
import java.time.Instant;
import java.util.UUID;

public record UploadedDocumentResponse(UUID documentId, String fileName, String contentType, long sizeBytes, DocumentPurpose purpose, DocumentStatus status, Instant uploadedAt) {
}
