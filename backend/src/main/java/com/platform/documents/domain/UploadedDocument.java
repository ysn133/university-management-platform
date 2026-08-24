package com.platform.documents.domain;

import com.platform.identityaccess.domain.UserAccount;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "uploaded_document")
public class UploadedDocument {
    @Id private UUID id;
    @ManyToOne(optional = false) @JoinColumn(name = "owner_user_account_id", nullable = false) private UserAccount owner;
    @Column(name = "storage_key", nullable = false, unique = true, length = 500) private String storageKey;
    @Column(name = "original_filename", nullable = false) private String originalFilename;
    @Column(name = "content_type", nullable = false, length = 100) private String contentType;
    @Column(name = "size_bytes", nullable = false) private long sizeBytes;
    @Enumerated(EnumType.STRING) @Column(name = "purpose", nullable = false, length = 60) private DocumentPurpose purpose;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 30) private DocumentStatus status;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @PrePersist void onCreate() { Instant now = Instant.now(); if (id == null) id = UUID.randomUUID(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public UserAccount getOwner() { return owner; }
    public void setOwner(UserAccount owner) { this.owner = owner; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public DocumentPurpose getPurpose() { return purpose; }
    public void setPurpose(DocumentPurpose purpose) { this.purpose = purpose; }
    public DocumentStatus getStatus() { return status; }
    public void setStatus(DocumentStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
