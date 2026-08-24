package com.platform.documents.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.platform.documents.domain.*;
import com.platform.documents.infrastructure.UploadedDocumentRepository;
import com.platform.identityaccess.domain.*;
import com.platform.identityaccess.infrastructure.UserAccountRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {
    @Mock UploadedDocumentRepository repository;
    @Mock UserAccountRepository accountRepository;
    @Mock DocumentStorage storage;

    @Test
    void uploadsValidatedPrivateDocumentForAuthenticatedOwner() {
        UserAccount account = mock(UserAccount.class);
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        DocumentService service = new DocumentService(repository, accountRepository, storage, 5_242_880);
        var file = new MockMultipartFile("file", "certificate.pdf", "application/pdf", "%PDF-test".getBytes());

        var response = service.upload(new AuthenticatedUserPrincipal(accountId, AccountRoleType.STUDENT, UUID.randomUUID(), UUID.randomUUID(), "student@uiz.ac.ma"), DocumentPurpose.ABSENCE_JUSTIFICATION, file);

        assertThat(response.fileName()).isEqualTo("certificate.pdf");
        assertThat(response.status()).isEqualTo(DocumentStatus.TEMPORARY);
        verify(storage).store(contains("absence-justification/"), eq("%PDF-test".getBytes()), eq("application/pdf"));
    }

    @Test
    void rejectsUnsupportedDocumentType() {
        DocumentService service = new DocumentService(repository, accountRepository, storage, 5_242_880);
        var file = new MockMultipartFile("file", "script.exe", "application/octet-stream", new byte[]{1});
        assertThatThrownBy(() -> service.upload(new AuthenticatedUserPrincipal(UUID.randomUUID(), AccountRoleType.STUDENT, UUID.randomUUID(), null, null), DocumentPurpose.ABSENCE_JUSTIFICATION, file))
            .isInstanceOf(ResponseStatusException.class).hasMessageContaining("Only PDF, JPG, and PNG");
        verifyNoInteractions(storage);
    }
}
