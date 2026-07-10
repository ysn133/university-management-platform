package com.platform.identityaccess.presentation;

import com.platform.identityaccess.application.AuthService;
import com.platform.identityaccess.presentation.dto.AuthResponse;
import com.platform.identityaccess.presentation.dto.ChangePasswordRequest;
import com.platform.identityaccess.presentation.dto.CurrentUserResponse;
import com.platform.identityaccess.presentation.dto.LoginRequest;
import com.platform.identityaccess.presentation.dto.RefreshRequest;
import com.platform.shared.presentation.ActionResponse;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<ActionResponse> changePassword(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(principal, request);
        return ResponseEntity.ok(new ActionResponse(true, "Password changed successfully"));
    }

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return authService.currentUser(principal);
    }
}
