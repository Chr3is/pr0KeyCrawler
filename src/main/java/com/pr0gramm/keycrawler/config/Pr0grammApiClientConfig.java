package com.pr0gramm.keycrawler.config;

import com.pr0gramm.keycrawler.client.Pr0grammClient;
import com.pr0gramm.keycrawler.client.exception.UnexpectedStatusCodeException;
import com.pr0gramm.keycrawler.config.properties.Pr0grammApiClientProperties;
import com.pr0gramm.keycrawler.model.Nonce;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.Map;

@Configuration
@EnableConfigurationProperties(Pr0grammApiClientProperties.class)
@RequiredArgsConstructor
@Slf4j
public class Pr0grammApiClientConfig {

    private final Pr0grammApiClientProperties properties;

    @Bean
    public Pr0grammClient pr0grammClient(WebClient.Builder webClientBuilder, Nonce nonce) {
        WebClient pr0ApiClient = createPr0grammApiClient(webClientBuilder);
        Pr0grammClient client = new Pr0grammClient(properties, pr0ApiClient, nonce);
        determineAndSetAuthenticationStatus(client);
        return client;
    }

    private WebClient createPr0grammApiClient(WebClient.Builder webclientBuilder) {
        return webclientBuilder
                .filter((request, next) -> {
                    ClientRequest.Builder builder = ClientRequest.from(request);
                    for (Map.Entry<String, String> cookie : properties.getCookies().entrySet()) {
                        builder = builder.cookie(cookie.getKey(), cookie.getValue());
                    }
                    return next.exchange(builder.build());
                })
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
                .baseUrl(properties.getUrl())
                .build();
    }

    private void determineAndSetAuthenticationStatus(Pr0grammClient client) {
        client.fetchNewContent().onErrorResume(throwable -> {
            if (throwable instanceof UnexpectedStatusCodeException) {
                UnexpectedStatusCodeException e = (UnexpectedStatusCodeException) throwable;
                if (e.getHttpStatus() == HttpStatus.FORBIDDEN || e.getHttpStatus() == HttpStatus.UNAUTHORIZED) {
                    log.info("Crawler is not authenticated");
                    client.setAuthenticated(false);
                }
            }
            return Mono.empty();
        }).block();
    }
}
