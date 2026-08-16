package com.studioengine.tutor.auth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.studioengine.tutor.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtProvider {

    private final AuthProperties authProperties;

    public String generateToken(String email) {
        try {
            var now = Instant.now();
            var expiry = now.plus(authProperties.getJwtExpiration());

            var claims = new JWTClaimsSet.Builder()
                    .subject(email)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiry))
                    .build();

            var signer = new MACSigner(authProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
            var signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            signedJWT.sign(signer);
            return signedJWT.serialize();

        } catch (JOSEException ex) {
            log.error("Failed to generate JWT: {}", ex.getMessage());
            throw new RuntimeException("JWT generation failed", ex);
        }
    }

    public String validateTokenAndGetEmail(String token) {
        try {
            var signedJWT = SignedJWT.parse(token);
            var verifier = new MACVerifier(authProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8));

            if (!signedJWT.verify(verifier)) {
                return null;
            }

            var claims = signedJWT.getJWTClaimsSet();
            if (claims.getExpirationTime().before(new Date())) {
                return null;
            }

            return claims.getSubject();

        } catch (ParseException | JOSEException ex) {
            log.warn("JWT validation failed: {}", ex.getMessage());
            return null;
        }
    }
}
