package com.platform.documents.presentation;

import com.platform.documents.application.DocumentService;
import com.platform.documents.domain.DocumentPurpose;
import com.platform.documents.presentation.dto.UploadedDocumentResponse;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {
    private final DocumentService service;
    public DocumentController(DocumentService service) { this.service = service; }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    public UploadedDocumentResponse upload(@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
                                           @RequestParam DocumentPurpose purpose,
                                           @RequestPart("file") MultipartFile file) {
        return service.upload(principal, purpose, file);
    }
}
