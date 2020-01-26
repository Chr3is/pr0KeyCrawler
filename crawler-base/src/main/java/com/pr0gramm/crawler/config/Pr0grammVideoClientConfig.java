package com.pr0gramm.crawler.config;

import com.pr0gramm.crawler.client.Pr0grammVideoClient;
import com.pr0gramm.crawler.config.properties.Pr0grammVideoClientProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(Pr0grammVideoClientProperties.class)
public class Pr0grammVideoClientConfig {

    private final Pr0grammVideoClientProperties properties;

    @Bean
    public Pr0grammVideoClient pr0grammVideoClient(WebClient.Builder webClientBuilder) {
        WebClient pr0VideoClient = createVideoClient(webClientBuilder);
        return new Pr0grammVideoClient(pr0VideoClient);
    }

    private WebClient createVideoClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(properties.getUrl())
                .build();
    }
}
