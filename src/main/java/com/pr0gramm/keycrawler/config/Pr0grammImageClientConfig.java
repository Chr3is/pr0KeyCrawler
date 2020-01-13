package com.pr0gramm.keycrawler.config;

import com.pr0gramm.keycrawler.client.Pr0grammImageClient;
import com.pr0gramm.keycrawler.config.properties.Pr0grammImageClientProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(Pr0grammImageClientProperties.class)
public class Pr0grammImageClientConfig {

    private final Pr0grammImageClientProperties properties;

    @Bean
    public Pr0grammImageClient pr0grammImageClient(WebClient.Builder webClientBuilder) {
        WebClient pr0ImageClient = createImageClient(webClientBuilder);
        return new Pr0grammImageClient(pr0ImageClient);
    }

    private WebClient createImageClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(properties.getUrl())
                .build();
    }
}
