package com.studioengine.tutor.auth;

import com.studioengine.tutor.config.AuthProperties;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtProviderTest {

    @Mock
    private AuthProperties authProperties;

    @InjectMocks
    private JwtProvider jwtProvider;

    @Nested
    class GenerateTokenTests {

        @Test
        void shouldGenerateToken() {
            // given
            var email = "tutor.tutic@gmail.com";
            var jwtExpiration = Duration.ofHours(1);
            var jwtSecret = "this-secret-must-be-at-least-32-bytes-long-for-hs256";

            when(authProperties.getJwtExpiration()).thenReturn(jwtExpiration);
            when(authProperties.getJwtSecret()).thenReturn(jwtSecret);

            // when
            var token = jwtProvider.generateToken(email);

            // then
            verify(authProperties).getJwtExpiration();
            verify(authProperties).getJwtSecret();

            assertThat(token).isNotBlank();
            var extractedMail = jwtProvider.validateTokenAndGetEmail(token);
            assertThat(extractedMail).isEqualTo(email);
        }
    }

    @Nested
    class ValidateTokenTests {

        private static final String SECRET = "this-secret-must-be-at-least-32-bytes-long-for-hs256";

        @Test
        void shouldReturnNullForExpiredToken() {
            // given
            var email = "tutor.tutic@gmail.com";
            when(authProperties.getJwtExpiration()).thenReturn(Duration.ofSeconds(-1)); // already expired
            when(authProperties.getJwtSecret()).thenReturn(SECRET);
            var token = jwtProvider.generateToken(email);
            // when
            var result = jwtProvider.validateTokenAndGetEmail(token);

            // then
            assertThat(result).isNull();
        }

        @Test
        void shouldReturnNullForTamperedToken() {
            // given
            when(authProperties.getJwtExpiration()).thenReturn(Duration.ofHours(1));
            when(authProperties.getJwtSecret()).thenReturn(SECRET);

            var token = jwtProvider.generateToken("tutor@test.com");
            var tamperedToken = token + "tampered";

            // when
            var result = jwtProvider.validateTokenAndGetEmail(tamperedToken);

            // then
            assertThat(result).isNull();
        }

        @Test
        void shouldReturnNullForInvalidFormat() {
            // when
            var result = jwtProvider.validateTokenAndGetEmail("not-a-jwt-token");

            // then
            assertThat(result).isNull();
        }
    }
}