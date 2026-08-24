package com.platform.documents.application;

import java.time.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TemporaryDocumentCleanup {
    private final DocumentService service;
    public TemporaryDocumentCleanup(DocumentService service) { this.service = service; }

    @Scheduled(cron = "${app.storage.cleanup-cron:0 0 * * * *}")
    public void removeExpiredUploads() {
        service.deleteExpiredTemporaryDocuments(Instant.now().minus(Duration.ofHours(24)));
    }
}
