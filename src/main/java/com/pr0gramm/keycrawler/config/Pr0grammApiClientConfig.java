package com.pr0gramm.keycrawler.config;

import com.pr0gramm.keycrawler.config.properties.Pr0grammApiClientProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.Map;

@Configuration
@EnableConfigurationProperties(Pr0grammApiClientProperties.class)
@RequiredArgsConstructor
@Slf4j
public class Pr0grammApiClientConfig {

    @Bean(name = "pr0ApiClient")
    public WebClient pr0grammApiClient(Pr0grammApiClientProperties properties, WebClient.Builder webclientBuilder) {
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

}
