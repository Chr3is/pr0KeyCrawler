package com.pr0gramm.keycrawler.config;

import com.pr0gramm.keycrawler.config.properties.Pr0grammImageClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(Pr0grammImageClientProperties.class)
public class Pr0grammImageClientConfig {

    @Bean(name = "pr0ImageClient")
    public WebClient imageClient(Pr0grammImageClientProperties properties, WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(properties.getUrl())
                .build();
    }

}
