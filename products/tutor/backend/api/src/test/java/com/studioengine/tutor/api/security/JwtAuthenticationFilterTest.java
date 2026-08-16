package com.studioengine.tutor.api.security;

import com.studioengine.tutor.auth.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthorizeRequest() throws Exception {
        // given
        var email = "tutor.tutic@gmail.com";
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var filterChain = mock(FilterChain.class);
        var tokenValue = "valid-token";
        var bearerToken = "Bearer %s".formatted(tokenValue);

        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtProvider.validateTokenAndGetEmail(tokenValue)).thenReturn(email);

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtProvider).validateTokenAndGetEmail(tokenValue);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(email);
        assertThat(auth.getAuthorities()).extracting("authority").contains("ROLE_TUTOR");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthorizeRequestWhenAuthorizationHeaderMissing() throws Exception {
        // given
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var filterChain = mock(FilterChain.class);

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtProvider, never()).validateTokenAndGetEmail(any());

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthorizeRequestWhenMalformedHeader() throws Exception {
        // given
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var filterChain = mock(FilterChain.class);
        var tokenValue = "invalid-token";
        var notBearerToken = "NotBearer %s".formatted(tokenValue);

        when(request.getHeader("Authorization")).thenReturn(notBearerToken);

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtProvider, never()).validateTokenAndGetEmail(any());

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthorizeRequestWhenInvalidToken() throws Exception {
        // given
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var filterChain = mock(FilterChain.class);
        var tokenValue = "invalid-token";
        var bearerToken = "Bearer %s".formatted(tokenValue);

        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtProvider.validateTokenAndGetEmail(tokenValue)).thenReturn(null);

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtProvider).validateTokenAndGetEmail(tokenValue);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();

        verify(filterChain).doFilter(request, response);
    }
}