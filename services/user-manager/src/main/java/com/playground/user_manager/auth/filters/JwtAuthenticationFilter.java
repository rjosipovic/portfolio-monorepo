package com.playground.user_manager.auth.filters;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final String secret;

    public JwtAuthenticationFilter(String secret) {
        this.secret = secret;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        var header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            var token = header.substring(7);
            try {
                var signedJwt = SignedJWT.parse(token);
                var valid = signedJwt.verify(new MACVerifier(secret));
                if (valid && notExpired(signedJwt)) {
                    var authentication = getUsernamePasswordAuthenticationToken(signedJwt);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    var securityContext = SecurityContextHolder.getContext();
                    securityContext.setAuthentication(authentication);
                }
            } catch (ParseException | JOSEException e) {
                log.error("Failed to parse JWT token", e);
            }
        }
        filterChain.doFilter(request, response);
    }

    private static boolean notExpired(SignedJWT signedJwt) throws ParseException {
        return signedJwt.getJWTClaimsSet().getExpirationTime().after(new Date());
    }

    private static UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(SignedJWT signedJwt) throws ParseException {
        var userPrincipal = JwtUserPrincipal.builder()
                .email(signedJwt.getJWTClaimsSet().getSubject())
                .claim("alias", signedJwt.getJWTClaimsSet().getStringClaim("alias"))
                .claim("userId", signedJwt.getJWTClaimsSet().getStringClaim("userId"))
                .build();
        return new UsernamePasswordAuthenticationToken(userPrincipal, null, Collections.emptyList());
    }
}
