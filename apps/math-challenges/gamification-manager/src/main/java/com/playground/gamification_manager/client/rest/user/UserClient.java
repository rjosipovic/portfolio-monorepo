package com.playground.gamification_manager.client.rest.user;

import com.playground.gamification_manager.auth.AuthenticatedUserPrincipalProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserClient {

    private final WebClient webClient;
    private final UserManagerClientConfig userManagerClientConfig;

    public UserClient(UserManagerClientConfig userManagerClientConfig, WebClient.Builder loadBalancedWebClientBuilder) {
        this.userManagerClientConfig = userManagerClientConfig;
        var baseUrl = String.format("http://%s/users", userManagerClientConfig.getServiceName());
        this.webClient = loadBalancedWebClientBuilder
                .baseUrl(baseUrl)
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    public List<UserAlias> getUserAlias(Set<UUID> userIds) {
        final var userIdsParam = userIds.stream()
                .map(UUID::toString)
                .collect(Collectors.joining(","));

        final var token = extractToken();

        var retrySpec = Retry.backoff(
                userManagerClientConfig.getRetry().getMaxAttempts(),
                userManagerClientConfig.getRetry().getWaitDuration()
        ).jitter(0.75);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("ids", userIdsParam)
                        .build())
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .bodyToFlux(UserAlias.class)
                .collectList()
                .retryWhen(retrySpec)
                .block();
    }

    private static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            var clientRequestMethod = clientRequest.method();
            var clientRequestURI = clientRequest.url();
            log.info("Request: {} {}", clientRequestMethod, clientRequestURI);
            return Mono.just(clientRequest);
        });
    }

    private static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            var responseStatusCode = clientResponse.statusCode();
            log.info("Response Status Code: {}", responseStatusCode);
            return Mono.just(clientResponse);
        });
    }

    private String extractToken() {
        return Optional.ofNullable(AuthenticatedUserPrincipalProvider.get())
                .map(UsernamePasswordAuthenticationToken::getCredentials)
                .map(Object::toString)
                .orElseThrow();
    }
}
