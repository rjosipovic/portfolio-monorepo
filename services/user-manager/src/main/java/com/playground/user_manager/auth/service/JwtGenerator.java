package com.playground.user_manager.auth.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.playground.user_manager.auth.api.dto.RegisteredUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtGenerator {

    private final AuthConfig authConfig;

    public String generate(RegisteredUser registeredUser) throws JOSEException {
        var email = registeredUser.getEmail();
        var alias = registeredUser.getAlias();
        var userId = registeredUser.getUserId();
        var claimSet = createClaimSet(userId, email, alias);
        var signedJwt = signJwt(claimSet);
        return signedJwt.serialize();
    }

    private JWTClaimsSet createClaimSet(String userId, String email, String alias) {
        var expiration = authConfig.getExpirationTime().toMillis();
        var currentTimeInMillis = System.currentTimeMillis();

        var issueTime = new Date(currentTimeInMillis);
        var expirationTime = new Date(currentTimeInMillis + expiration);

        return new JWTClaimsSet.Builder()
                .subject(email)
                .claim("alias", alias)
                .claim("userId", userId)
                .issueTime(issueTime)
                .expirationTime(expirationTime)
                .build();
    }

    private SignedJWT signJwt(JWTClaimsSet claimSet) throws JOSEException {
        var secret = authConfig.getSecret();
        var signer = new MACSigner(secret);
        var signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimSet);
        signedJwt.sign(signer);
        return signedJwt;
    }
}
