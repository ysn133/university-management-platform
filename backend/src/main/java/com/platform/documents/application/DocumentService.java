package com.platform.documents.application;

import com.platform.documents.domain.*;
import com.platform.documents.infrastructure.UploadedDocumentRepository;
import com.platform.documents.presentation.dto.UploadedDocumentResponse;
import com.platform.identityaccess.infrastructure.UserAccountRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DocumentService {
    private static final Set<String> ALLOWED_TYPES = Set.of("application/pdf", "image/jpeg", "image/png");
    private final UploadedDocumentRepository repository;
    private final UserAccountRepository accountRepository;
    private final DocumentStorage storage;
    private final long maximumFileSize;

    public DocumentService(UploadedDocumentRepository repository, UserAccountRepository accountRepository, DocumentStorage storage,
                           @Value("${app.storage.maximum-file-size}") long maximumFileSize) {
        this.repository = repository;
        this.accountRepository = accountRepository;
        this.storage = storage;
        this.maximumFileSize = maximumFileSize;
    }

    @Transactional
    public UploadedDocumentResponse upload(AuthenticatedUserPrincipal principal, DocumentPurpose purpose, MultipartFile file) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        byte[] content;
        try { content = file.getBytes(); }
        catch (java.io.IOException exception) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document could not be read"); }
        validate(file, content);
        var owner = accountRepository.findById(principal.userAccountId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
        String extension = switch (file.getContentType()) { case "application/pdf" -> ".pdf"; case "image/jpeg" -> ".jpg"; case "image/png" -> ".png"; default -> ""; };
        String key = purpose.name().toLowerCase(Locale.ROOT).replace('_', '-') + "/" + UUID.randomUUID() + extension;
        storage.store(key, content, file.getContentType());
        UploadedDocument document = new UploadedDocument();
        document.setOwner(owner); document.setStorageKey(key); document.setOriginalFilename(safeFilename(file.getOriginalFilename()));
        document.setContentType(file.getContentType()); document.setSizeBytes(file.getSize()); document.setPurpose(purpose); document.setStatus(DocumentStatus.TEMPORARY);
        try { return toResponse(repository.save(document)); }
        catch (RuntimeException exception) { storage.delete(key); throw exception; }
    }

    @Transactional(readOnly = true)
    public UploadedDocument requireOwnedTemporary(UUID documentId, AuthenticatedUserPrincipal principal, DocumentPurpose purpose) {
        UploadedDocument document = find(documentId);
        if (!document.getOwner().getId().equals(principal.userAccountId()) || document.getPurpose() != purpose || document.getStatus() != DocumentStatus.TEMPORARY)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document is not available for this request");
        return document;
    }

    @Transactional public void markAttached(UploadedDocument document) { document.setStatus(DocumentStatus.ATTACHED); repository.save(document); }
    @Transactional(readOnly = true) public UploadedDocument find(UUID id) { return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found")); }
    public byte[] load(UploadedDocument document) { return storage.load(document.getStorageKey()); }

    @Transactional
    public void deleteExpiredTemporaryDocuments(java.time.Instant cutoff) {
        repository.findByStatusAndCreatedAtBefore(DocumentStatus.TEMPORARY, cutoff).forEach(document -> {
            storage.delete(document.getStorageKey());
            document.setStatus(DocumentStatus.DELETED);
            repository.save(document);
        });
    }

    private void validate(MultipartFile file, byte[] content) {
        if (file == null || file.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A document is required");
        if (file.getSize() > maximumFileSize) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document exceeds the maximum size");
        if (!ALLOWED_TYPES.contains(file.getContentType())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF, JPG, and PNG documents are accepted");
        boolean validSignature = switch (file.getContentType()) {
            case "application/pdf" -> startsWith(content, new byte[]{'%', 'P', 'D', 'F'});
            case "image/jpeg" -> startsWith(content, new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff});
            case "image/png" -> startsWith(content, new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0d, 0x0a, 0x1a, 0x0a});
            default -> false;
        };
        if (!validSignature) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document content does not match its file type");
    }
    private boolean startsWith(byte[] content, byte[] signature) { if (content.length < signature.length) return false; for (int i = 0; i < signature.length; i++) if (content[i] != signature[i]) return false; return true; }
    private String safeFilename(String value) { String name = value == null ? "document" : value.replace('\\', '/'); return name.substring(name.lastIndexOf('/') + 1); }
    private UploadedDocumentResponse toResponse(UploadedDocument d) { return new UploadedDocumentResponse(d.getId(), d.getOriginalFilename(), d.getContentType(), d.getSizeBytes(), d.getPurpose(), d.getStatus(), d.getCreatedAt()); }
}
