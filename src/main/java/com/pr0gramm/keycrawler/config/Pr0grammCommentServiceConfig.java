package com.pr0gramm.keycrawler.config;

import com.pr0gramm.keycrawler.client.Pr0grammClient;
import com.pr0gramm.keycrawler.config.properties.NotificationProperties;
import com.pr0gramm.keycrawler.service.Pr0grammCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(prefix = "pr0gramm.notifications", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(NotificationProperties.class)
@Configuration
@Slf4j
public class Pr0grammCommentServiceConfig {

    @Bean
    public Pr0grammCommentService pr0grammCommentService(NotificationProperties properties, Pr0grammClient client) {
        log.info("Pr0gramm notifications are enabled");
        return new Pr0grammCommentService(properties, client);
    }

}
