package com.platform.identityaccess.domain;

import java.time.Instant;
import java.util.UUID;

public class RefreshTokenSession {

    private String tokenValue;
    private UUID userAccountId;
    private AccountRoleType role;
    private UUID roleEntityId;
    private UUID establishmentId;
    private String universityEmail;
    private Instant expiresAt;

    public RefreshTokenSession() {
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public UUID getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(UUID userAccountId) {
        this.userAccountId = userAccountId;
    }

    public AccountRoleType getRole() {
        return role;
    }

    public void setRole(AccountRoleType role) {
        this.role = role;
    }

    public UUID getRoleEntityId() {
        return roleEntityId;
    }

    public void setRoleEntityId(UUID roleEntityId) {
        this.roleEntityId = roleEntityId;
    }

    public UUID getEstablishmentId() {
        return establishmentId;
    }

    public void setEstablishmentId(UUID establishmentId) {
        this.establishmentId = establishmentId;
    }

    public String getUniversityEmail() {
        return universityEmail;
    }

    public void setUniversityEmail(String universityEmail) {
        this.universityEmail = universityEmail;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
