package com.platform.platform.infrastructure.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.platform.identityaccess.domain.AccountRoleType;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class JwtTokenService {

    private final String jwtSecret;
    private final long accessTokenTtlSeconds;

    public JwtTokenService(
        @Value("${app.security.jwt.secret}") String jwtSecret,
        @Value("${app.security.jwt.access-token-ttl-seconds}") long accessTokenTtlSeconds
    ) {
        this.jwtSecret = jwtSecret;
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    }

    public String generateAccessToken(
        UUID userAccountId,
        AccountRoleType role,
        UUID roleEntityId,
        UUID establishmentId,
        String universityEmail
    ) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenTtlSeconds);

        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
            .subject(userAccountId.toString())
            .issueTime(Date.from(now))
            .expirationTime(Date.from(expiresAt))
            .claim("role", role.name())
            .claim("roleEntityId", roleEntityId.toString())
            .claim("email", universityEmail);

        if (establishmentId != null) {
            claimsBuilder.claim("establishmentId", establishmentId.toString());
        }

        JWTClaimsSet claims = claimsBuilder.build();

        SignedJWT signedJwt = new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.HS256)
                .type(JOSEObjectType.JWT)
                .build(),
            claims
        );

        try {
            signedJwt.sign(new MACSigner(signingKey()));
            return signedJwt.serialize();
        } catch (JOSEException exception) {
            throw new IllegalStateException("Failed to sign access token", exception);
        }
    }

    public AuthenticatedUserPrincipal parseAccessToken(String tokenValue) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(tokenValue);

            if (!signedJwt.verify(new MACVerifier(signingKey()))) {
                throw new ResponseStatusException(UNAUTHORIZED, "Invalid access token");
            }

            JWTClaimsSet claims = signedJwt.getJWTClaimsSet();
            Instant expiresAt = claims.getExpirationTime().toInstant();
            if (expiresAt.isBefore(Instant.now())) {
                throw new ResponseStatusException(UNAUTHORIZED, "Access token expired");
            }

            return new AuthenticatedUserPrincipal(
                UUID.fromString(claims.getSubject()),
                AccountRoleType.valueOf(claims.getStringClaim("role")),
                UUID.fromString(claims.getStringClaim("roleEntityId")),
                claims.getStringClaim("establishmentId") == null
                    ? null
                    : UUID.fromString(claims.getStringClaim("establishmentId")),
                claims.getStringClaim("email")
            );
        } catch (ParseException | JOSEException | IllegalArgumentException exception) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid access token");
        }
    }

    private byte[] signingKey() {
        try {
            return MessageDigest.getInstance("SHA-256")
                .digest(jwtSecret.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
