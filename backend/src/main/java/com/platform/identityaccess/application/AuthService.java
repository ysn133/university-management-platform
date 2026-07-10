package com.platform.identityaccess.application;


import com.platform.identityaccess.domain.AccountStatus;
import com.platform.identityaccess.domain.RefreshTokenSession;
import com.platform.identityaccess.domain.UserAccount;
import com.platform.identityaccess.domain.UserProfile;
import com.platform.identityaccess.infrastructure.RefreshTokenSessionStore;
import com.platform.identityaccess.infrastructure.UserAccountRepository;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.identityaccess.presentation.dto.AuthResponse;
import com.platform.identityaccess.presentation.dto.ChangePasswordRequest;
import com.platform.identityaccess.presentation.dto.CurrentUserResponse;
import com.platform.identityaccess.presentation.dto.LoginRequest;
import com.platform.identityaccess.presentation.dto.RefreshRequest;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.platform.infrastructure.security.JwtTokenService;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final RoleContextService roleContextService;
    private final RefreshTokenSessionStore refreshTokenSessionStore;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final long refreshTokenTtlSeconds;

    public AuthService(
        UserAccountRepository userAccountRepository,
        UserProfileRepository userProfileRepository,
        RoleContextService roleContextService,
        RefreshTokenSessionStore refreshTokenSessionStore,
        PasswordEncoder passwordEncoder,
        JwtTokenService jwtTokenService,
        @Value("${app.security.jwt.refresh-token-ttl-seconds}") long refreshTokenTtlSeconds
    ) {
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.roleContextService = roleContextService;
        this.refreshTokenSessionStore = refreshTokenSessionStore;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserAccount account = userAccountRepository.findByUniversityEmail(request.universityEmail().trim())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        ensureAccountIsActive(account);

        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        UserProfile profile = userProfileRepository.findByUserAccountId(account.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User profile not found"));

        RoleContext roleContext = roleContextService.loadRoleContext(account, account.getRole());

        account.setLastLoginAt(Instant.now());
        userAccountRepository.save(account);

        return buildAuthResponse(account, profile, roleContext, null);
    }

    

    @Transactional(readOnly = true)
    public CurrentUserResponse currentUser(AuthenticatedUserPrincipal principal) {
        UserAccount account = userAccountRepository.findById(principal.userAccountId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));

        UserProfile profile = userProfileRepository.findByUserAccountId(account.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User profile not found"));

        RoleContext roleContext = roleContextService.loadRoleContext(account, principal.role());

        return new CurrentUserResponse(
            account.getId(),
            account.getRole().name(),
            roleContext.roleEntityId(),
            roleContext.establishmentId(),
            account.getUniversityEmail(),
            profile.getFirstName(),
            profile.getLastName(),
            account.getAccountStatus().name()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshRequest request) {
        RefreshTokenSession session = refreshTokenSessionStore.findByToken(request.refreshToken())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        UserAccount account = userAccountRepository.findById(session.getUserAccountId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));

        ensureAccountIsActive(account);

        UserProfile profile = userProfileRepository.findByUserAccountId(account.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User profile not found"));

        RoleContext roleContext = roleContextService.loadRoleContext(account, session.getRole());

        refreshTokenSessionStore.delete(request.refreshToken());

        return buildAuthResponse(account, profile, roleContext, request.refreshToken());
    }

    public void logout(RefreshRequest request) {
        refreshTokenSessionStore.delete(request.refreshToken());
    }

    @Transactional
    public void changePassword(AuthenticatedUserPrincipal principal, ChangePasswordRequest request) {
        UserAccount account = userAccountRepository.findById(principal.userAccountId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));

        ensureAccountIsActive(account);

        if (!passwordEncoder.matches(request.currentPassword(), account.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        if (passwordEncoder.matches(request.newPassword(), account.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different from the current password");
        }

        account.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userAccountRepository.save(account);
    }

    private AuthResponse buildAuthResponse(
        UserAccount account,
        UserProfile profile,
        RoleContext roleContext,
        String oldRefreshTokenToDelete
    ) {
        if (oldRefreshTokenToDelete != null) {
            refreshTokenSessionStore.delete(oldRefreshTokenToDelete);
        }

        String accessToken = jwtTokenService.generateAccessToken(
            account.getId(),
            account.getRole(),
            roleContext.roleEntityId(),
            roleContext.establishmentId(),
            account.getUniversityEmail()
        );

        String refreshToken = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(refreshTokenTtlSeconds);

        RefreshTokenSession session = new RefreshTokenSession();
        session.setTokenValue(refreshToken);
        session.setUserAccountId(account.getId());
        session.setRole(account.getRole());
        session.setRoleEntityId(roleContext.roleEntityId());
        session.setEstablishmentId(roleContext.establishmentId());
        session.setUniversityEmail(account.getUniversityEmail());
        session.setExpiresAt(expiresAt);

        refreshTokenSessionStore.save(session, Duration.ofSeconds(refreshTokenTtlSeconds));

        return new AuthResponse(
            account.getId(),
            account.getRole().name(),
            roleContext.roleEntityId(),
            roleContext.establishmentId(),
            account.getUniversityEmail(),
            profile.getFirstName(),
            profile.getLastName(),
            account.getAccountStatus().name(),
            accessToken,
            refreshToken
        );
    }

    private void ensureAccountIsActive(UserAccount account) {
        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not active");
        }
    }
}
