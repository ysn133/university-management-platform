package com.platform.ai.navigation.presentation;

import com.platform.ai.navigation.application.AiNavigationService;
import com.platform.ai.navigation.domain.AiNavigationFailureException;
import com.platform.ai.navigation.domain.NavigationResult;
import com.platform.ai.navigation.presentation.dto.AiNavigationErrorResponse;
import com.platform.ai.navigation.presentation.dto.AiNavigationRequest;
import com.platform.ai.navigation.presentation.dto.AiNavigationResponse;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/navigation")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
public class AiNavigationController {

    private final AiNavigationService navigationService;
    private final boolean diagnosticsEnabled;

    public AiNavigationController(
        AiNavigationService navigationService,
        @Value("${app.ai.agent.diagnostics-enabled:false}") boolean diagnosticsEnabled
    ) {
        this.navigationService = navigationService;
        this.diagnosticsEnabled = diagnosticsEnabled;
    }

    @PostMapping
    public AiNavigationResponse navigate(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
        @Valid @RequestBody AiNavigationRequest request
    ) {
        NavigationResult result = navigationService.navigate(
            principal,
            authorizationHeader,
            request.query(),
            request.currentRoute(),
            formatHistory(request)
        );
        return new AiNavigationResponse(
            result.mode(),
            result.route(),
            result.message(),
            diagnosticsEnabled ? result.diagnostics() : null
        );
    }

    @ExceptionHandler(AiNavigationFailureException.class)
    public ResponseEntity<AiNavigationErrorResponse> handleNavigationFailure(
        AiNavigationFailureException exception
    ) {
        return ResponseEntity.status(exception.status()).body(
            new AiNavigationErrorResponse(
                exception.status(),
                exception.getMessage(),
                diagnosticsEnabled ? exception.diagnostics() : null
            )
        );
    }

    private String formatHistory(AiNavigationRequest request) {
        if (request.history() == null || request.history().isEmpty()) return "none";
        return request.history().stream()
            .map(message -> message.role() + ": " + message.content())
            .reduce((left, right) -> left + "\n" + right)
            .orElse("none");
    }
}
